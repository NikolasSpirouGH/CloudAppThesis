package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.dto.request.category.CategoryCreateRequest;
import com.cloud_ml_app_thesis.dto.request.category.CategoryUpdateRequest;
import com.cloud_ml_app_thesis.entity.*;
import com.cloud_ml_app_thesis.entity.dataset.Dataset;
import com.cloud_ml_app_thesis.entity.status.CategoryRequestStatus;
import com.cloud_ml_app_thesis.enumeration.status.CategoryRequestStatusEnum;
import com.cloud_ml_app_thesis.repository.CategoryHistoryRepository;
import com.cloud_ml_app_thesis.repository.CategoryRepository;
import com.cloud_ml_app_thesis.repository.CategoryRequestRepository;
import com.cloud_ml_app_thesis.repository.UserRepository;
import com.cloud_ml_app_thesis.repository.status.CategoryRequestStatusRepository;
import com.cloud_ml_app_thesis.util.ValidationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRequestRepository categoryRequestRepository;
    private final CategoryHistoryRepository categoryHistoryRepository;
    private final CategoryRequestStatusRepository categoryRequestStatusRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;


    @Transactional
    public void deleteCategory(String username, Integer categoryId) {

        //TODO add user also for logging the deletion. Maybe the deletion if is being by a ROLE_USER, then add it as CategoryDeletionRequest
        Category categoryToDelete = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        // 1. Find the immediate parent category (closest parent)
        Category closestParent = findClosestParent(categoryToDelete);

        if (closestParent == null) {
            throw new IllegalStateException("Cannot delete category: No valid parent category found.");
        }

        // 2. Move all child categories to the closest parent category
        for (Category childCategory : new HashSet<>(categoryToDelete.getChildCategories())) {
            childCategory.getParentCategories().remove(categoryToDelete);
            childCategory.getParentCategories().add(closestParent);
        }

        // 3. Reassign models and datasets to the closest parent
        for (Model model : categoryToDelete.getModels()) {
            model.getCategories().remove(categoryToDelete);
            model.getCategories().add(closestParent);
        }

        for (Dataset dataset : categoryToDelete.getDatasets()) {
            dataset.getCategories().remove(categoryToDelete);
            dataset.getCategories().add(closestParent);
        }

        // 4. Remove category from all parent references before deletion
        for (Category parent : categoryToDelete.getParentCategories()) {
            parent.getChildCategories().remove(categoryToDelete);
        }

        // 5. Delete the category
        categoryRepository.delete(categoryToDelete);
    }
    @Transactional
    public CategoryRequest createCategory(String username, CategoryCreateRequest request){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User could not be found"));

        Optional<Category> existingCategory = categoryRepository.findByName(request.getName());
        if(existingCategory.isPresent()){
            throw new EntityExistsException("The category you requested for creation already exists.");
        }
        boolean isForce = request.isForce();

        Set<Category> parentCategories = new HashSet<>();

        Set<Integer> parentCategoryIds =  request.getParentCategoryIds();
        if(parentCategoryIds != null){
            for(Integer parentId : parentCategoryIds){
                Category parent = categoryRepository.findById(parentId)
                        .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + parentId));
                parentCategories.add(parent);
            }
        }
        //We have already checked if admin pr CATEGORY_MANAGER requested with force to directly write on Categories Table
        CategoryRequestStatusEnum statusEnum = CategoryRequestStatusEnum.PENDING;
        User processedBy = null;
        LocalDateTime requestedAt = LocalDateTime.now();
        LocalDateTime processedAt = null;

        if(isForce){
            Category category = new Category();
            category.setName(request.getName());
            category.setDescription(request.getDescription());
            category.setCreatedBy(user);
            category.setParentCategories(parentCategories);
            Integer id = request.getId();
            if(id != null){
                category.setId(id);
            }
            categoryRepository.save(category);
            statusEnum = CategoryRequestStatusEnum.APPROVED;
            processedAt = requestedAt;
            processedBy = user;
        }

        CategoryRequestStatus categoryRequestStatus = categoryRequestStatusRepository.findByName(statusEnum)
                .orElseThrow(() -> new EntityNotFoundException("Category status could not be found"));

        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName(request.getName());
        categoryRequest.setDescription(request.getDescription());
        categoryRequest.setStatus(categoryRequestStatus);
        categoryRequest.setRequestedBy(user);
        categoryRequest.setProcessedBy(processedBy);
        categoryRequest.setRequestedAt(requestedAt);
        categoryRequest.setProcessedAt(processedAt);
        categoryRequest.setParentCategories(parentCategories);

        return categoryRequestRepository.save(categoryRequest);
    }

    @Transactional
    public Category approveCategoryRequest(String username, Integer requestId) {
        return processCategoryRequest(username, requestId,  CategoryRequestStatusEnum.APPROVED, null);
    }

    @Transactional
    public Category rejectCategoryRequest(String username, Integer requestId, String rejectionReason) {
        return processCategoryRequest(username, requestId,  CategoryRequestStatusEnum.REJECTED, rejectionReason);
    }

    @Transactional
    public Category updateCategory(String username, Integer categoryId,CategoryUpdateRequest request){
        User editor = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        String oldValues = convertCategoryToJson(category);
        boolean dataExist = false;
        Integer newId = request.getNewId();
        if(newId != null){
            dataExist = true;
            Optional<Category> tempCategory = categoryRepository.findById(newId);
            if(tempCategory.isPresent()){
                //TODO I can return the found category
                throw new RuntimeException("Category with id: " + newId + " already exist. Category name: "+ tempCategory.get().getName()+".");
            }
            category.setId(newId);
        }

        if(ValidationUtil.stringExists(request.getName())){
            dataExist = true;
            category.setName(request.getName());
        }

        if(ValidationUtil.stringExists(request.getDescription())){
            dataExist = true;
            category.setDescription(request.getDescription());
        }

        Set<Integer> newParentIds = request.getNewParentCategoryIds();
        if(newParentIds != null && !newParentIds.isEmpty()){
            dataExist = true;
            List<Category> newParentCategories =  findCategoriesByIdsStrict(request.getNewParentCategoryIds());
            category.setParentCategories(new HashSet<>(newParentCategories));
        }

        Set<Integer> parentIdsToRemove = request.getParentCategoryIdsToRemove();
        if(parentIdsToRemove != null && !parentIdsToRemove.isEmpty()){
            dataExist = true;
            category = removeParentCategoriesStrict(category, parentIdsToRemove);
        }

        if(!dataExist){
            throw new IllegalArgumentException("At least one field must be provided for update.");
        }

        category =  categoryRepository.save(category);
        String newValues = convertCategoryToJson(category);

        CategoryHistory history = CategoryHistory.builder()
                .category(category)
                .editedBy(editor)
                .editedAt(LocalDateTime.now())
                .oldValues(oldValues)
                .newValues(newValues)
                .build();

        categoryHistoryRepository.save(history);

        return category;
    }

    private Category processCategoryRequest(String username, Integer requestId, CategoryRequestStatusEnum categoryRequestStatusEnum, String rejectionReason){
        CategoryRequest request = categoryRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Category request not found"));

        if (!request.getStatus().getName().equals(CategoryRequestStatusEnum.PENDING)) {
            throw new IllegalArgumentException("This request has already been processed.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        CategoryRequestStatus status = null;
        Category category = null;
        final LocalDateTime localDateTimeNow = LocalDateTime.now();

        if(categoryRequestStatusEnum == CategoryRequestStatusEnum.APPROVED) {
            status = categoryRequestStatusRepository.findByName(categoryRequestStatusEnum)
                    .orElseThrow(() -> new EntityNotFoundException("Status APPROVED could not be found"));

            // Create the new category and assign parent categories
            category = new Category();
            category.setName(request.getName());
            category.setDescription(request.getDescription());
            category.setCreatedBy(request.getRequestedBy());

            // Assign parent categories
            if (request.getParentCategories() != null && !request.getParentCategories().isEmpty()) {
                category.setParentCategories(request.getParentCategories());
            }
            category = categoryRepository.save(category);

            CategoryHistory history = CategoryHistory.builder()
                    .category(category)
                    .editedBy(user)
                    .editedAt(localDateTimeNow)
                    .oldValues(null)
                    .newValues(convertCategoryToJson(category))
                    .comments("Approved by user: " + username)
                    .initial(true)
                    .build();

            categoryHistoryRepository.save(history);

            request.setApprovedCategory(category);

        } else {
            status = categoryRequestStatusRepository.findByName(categoryRequestStatusEnum)
                    .orElseThrow(() -> new EntityNotFoundException("Status APPROVED could not be found"));
            request.setRejectionReason(rejectionReason);
        }

        // Update request status and store the approved category

        request.setStatus(status);
        request.setProcessedBy(user);
        request.setProcessedAt(localDateTimeNow);

        categoryRequestRepository.save(request);

        return category;
    }

    public Set<Integer> getChildCategoryIds(Integer categoryId, boolean deepSearch) {
        if(deepSearch) {
            Set<Integer> childCategoryIds = new HashSet<>();
            findChildCategories(categoryId, childCategoryIds);
            return childCategoryIds; // The same Set is updated recursively
        } else {
            return getDirectChildCategoryIds(categoryId);
        }
    }

    public Set<Integer> getDirectChildCategoryIds(Integer categoryId) {
        return categoryRepository.findByParentCategoriesId(categoryId)
                .stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
    }

    private void findChildCategories(Integer categoryId, Set<Integer> childCategoryIds) {
        childCategoryIds.add(categoryId);
        Set<Category> children = categoryRepository.findByParentCategoriesId(categoryId);
        // Recursively find deeper child categories
        for (Category child : children) {
            findChildCategories(child.getId(), childCategoryIds);
        }
    }

    private int getCategoryLevel(Category category) {
        int level = 0;
        while (!category.getParentCategories().isEmpty()) {
            category = category.getParentCategories().iterator().next();
            level++;
        }
        return level;
    }

    private Category findClosestParent(Category category) {
        Set<Category> parentCategories = category.getParentCategories();

        if (parentCategories.isEmpty()) {
            return null; // No parent category available
        }

        // If there's only one parent, it's the closest one
        if (parentCategories.size() == 1) {
            return parentCategories.iterator().next();
        }

        // If multiple parents exist, find the one closest in hierarchy
        Category closestParent = null;
        int highestLevel = -1;

        for (Category parent : parentCategories) {
            int parentLevel = getCategoryLevel(parent);
            if (parentLevel > highestLevel) {
                highestLevel = parentLevel;
                closestParent = parent;
            }
        }

        return closestParent;
    }


    private List<Category> findCategoriesByIdsStrict(Set<Integer> categoryIds){
        List<Category> foundCategories = categoryRepository.findAllById(categoryIds);

        Set<Integer> foundCategoryIds = foundCategories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        Set<Integer> missingIds = categoryIds.stream()
                .filter(id -> !foundCategoryIds.contains(id))
                .collect(Collectors.toSet());

        if (!missingIds.isEmpty()) {
            throw new EntityNotFoundException("Categories not found for IDs: " + missingIds);
        }
        return foundCategories;
    }

    private Category removeParentCategoriesStrict(Category category, Set<Integer> parentIdsToRemove){
        if(category == null){
            throw new RuntimeException("Category cannot be null");
        }

        if(parentIdsToRemove == null || parentIdsToRemove.isEmpty()){
            throw new IllegalArgumentException("At least one parent category ID must be provided.");
        }

        Set<Category> parentsToRemove = category.getParentCategories().stream()
                .filter(parent -> parentIdsToRemove.contains(parent.getId()))
                .collect(Collectors.toSet());

        if (parentsToRemove.isEmpty()) {
            throw new IllegalArgumentException("No matching parent categories found to remove.");
        }

        //Child to Parent Relationship remove
        category.getParentCategories().removeAll(parentsToRemove);

        //Parent to Child Relationship remove
        for(Category parent : parentsToRemove){
            parent.getChildCategories().remove(category);
            categoryRepository.save(parent);
        }

        return category;
    }

    private String convertCategoryToJson(Category category) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(category);
        } catch (JsonProcessingException e) {
            return "{}"; // Default empty JSON
        }
    }
}
