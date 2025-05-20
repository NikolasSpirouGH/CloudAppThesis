package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.dto.category.CategoryDTO;
import com.cloud_ml_app_thesis.dto.request.category.CategoryCreateRequest;
import com.cloud_ml_app_thesis.dto.category.CategoryRequestDTO;
import com.cloud_ml_app_thesis.dto.request.category.CategoryUpdateRequest;
import com.cloud_ml_app_thesis.entity.*;
import com.cloud_ml_app_thesis.entity.dataset.Dataset;
import com.cloud_ml_app_thesis.entity.status.CategoryRequestStatus;
import com.cloud_ml_app_thesis.enumeration.status.CategoryRequestStatusEnum;
import com.cloud_ml_app_thesis.repository.*;
import com.cloud_ml_app_thesis.repository.status.CategoryRequestStatusRepository;
import com.cloud_ml_app_thesis.dto.response.MyResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;
import java.util.stream.Collectors;

import static com.cloud_ml_app_thesis.enumeration.status.CategoryRequestStatusEnum.PENDING;
import static com.cloud_ml_app_thesis.enumeration.status.CategoryRequestStatusEnum.REJECTED;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


class CategoryServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryRequestRepository categoryRequestRepository;
    @Mock private CategoryRequestStatusRepository categoryRequestStatusRepository;
    @Mock private CategoryHistoryRepository categoryHistoryRepository;
    @Mock ModelMapper modelMapper;

    @Spy
    @InjectMocks private CategoryService categoryService;

    private User user;
    private CategoryRequestStatus pendingStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(UUID.randomUUID()).username("john").build();
        pendingStatus = CategoryRequestStatus.builder()
                .name(CategoryRequestStatusEnum.PENDING)
                .description("Pending")
                .build();
    }
    // CREATE CATEGORY REQUEST TEST CASES
    @Test
    void shouldCreateCategoryRequest_WhenValidDataAndForceFalse() {
        // Arrange
        CategoryCreateRequest req = new CategoryCreateRequest();
        req.setName("AI");
        req.setDescription("Artificial Intelligence");
        req.setForce(false);

        CategoryRequestDTO mockDto = new CategoryRequestDTO();
        mockDto.setName("AI");
        mockDto.setDescription("Artificial Intelligence");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(categoryRepository.findByName("AI")).thenReturn(Optional.empty());
        when(categoryRequestStatusRepository.findByName(PENDING)).thenReturn(Optional.of(pendingStatus));
        when(modelMapper.map(any(CategoryRequest.class), eq(CategoryRequestDTO.class))).thenReturn(mockDto);

        // Act
        MyResponse<CategoryRequestDTO> response = categoryService.createCategory("john", req);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDataHeader()).isNotNull();
        assertThat(response.getDataHeader().getName()).isEqualTo("AI");
        verify(categoryRequestRepository, times(1)).save(any(CategoryRequest.class));
    }

    @Test
    void shouldThrowEntityExistsException_WhenCategoryAlreadyExists() {
        CategoryCreateRequest req = new CategoryCreateRequest();
        req.setName("AI");
        req.setDescription("Artificial Intelligence");
        req.setForce(false);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(categoryRepository.findByName("AI")).thenReturn(Optional.of(new Category()));

        assertThatThrownBy(() -> categoryService.createCategory("john", req))
                .isInstanceOf(EntityExistsException.class);
    }

    @Test
    void shouldThrowEntityNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        CategoryCreateRequest req = new CategoryCreateRequest();
        req.setName("AI");
        req.setDescription("Artificial Intelligence");
        req.setForce(false);

        assertThatThrownBy(() -> categoryService.createCategory("john", req))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldCreateCategoryDirectly_WhenForceIsTrue() {
        CategoryCreateRequest req = new CategoryCreateRequest();
        req.setName("AI");
        req.setDescription("Artificial Intelligence");
        req.setForce(true);

        CategoryRequestDTO mockDto = new CategoryRequestDTO();
        mockDto.setName("AI");
        mockDto.setDescription("Artificial Intelligence");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(categoryRepository.findByName("AI")).thenReturn(Optional.empty());
        when(categoryRequestStatusRepository.findByName(CategoryRequestStatusEnum.APPROVED))
                .thenReturn(Optional.of(CategoryRequestStatus.builder()
                        .name(CategoryRequestStatusEnum.APPROVED)
                        .description("Approved")
                        .build()));

        when(modelMapper.map(any(CategoryRequest.class), eq(CategoryRequestDTO.class))).thenReturn(mockDto);

        MyResponse<CategoryRequestDTO> response = categoryService.createCategory("john", req);

        assertThat(response).isNotNull();
        assertThat(response.getDataHeader().getName()).isEqualTo("AI");

        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(categoryRequestRepository, times(1)).save(any(CategoryRequest.class));
    }

        //APPROVE CATEGORY REQUEST TEST CASE

    @Test
    void shouldUpdateNameAndDescriptionSuccessfully() {
        // Arrange
        CategoryUpdateRequest req = new CategoryUpdateRequest();
        req.setName("New Category");
        req.setDescription("Updated description");

        Category category = new Category();
        category.setId(1);
        category.setName("Old Category");
        category.setDescription("Old description");
        category.setParentCategories(new HashSet<>());
        category.setCreatedBy(user);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(new CategoryDTO());
        when(categoryHistoryRepository.save(any())).thenReturn(null);
        // Act
        MyResponse<CategoryDTO> response = categoryService.updateCategory("john", 1, req);

        // Assert
        assertThat(response).isNotNull();
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void shouldAddNewParentCategorySuccessfully() {
        CategoryUpdateRequest req = new CategoryUpdateRequest();
        req.setNewParentCategoryIds(Set.of(101));

        Category category = new Category();
        category.setId(1);
        category.setParentCategories(new HashSet<>());
        category.setCreatedBy(user);

        Category parent = new Category();
        parent.setId(101);
        parent.setChildCategories(new HashSet<>());

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.findAllById(Set.of(101))).thenReturn(List.of(parent));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(new CategoryDTO());

        MyResponse<CategoryDTO> response = categoryService.updateCategory("john", 1, req);

        assertThat(response).isNotNull();
        assertThat(category.getParentCategories()).contains(parent);
    }
    @Test
    void shouldRemoveParentCategorySuccessfully() {
        CategoryUpdateRequest req = new CategoryUpdateRequest();
        req.setParentCategoryIdsToRemove(Set.of(202));

        Category parent = new Category();
        parent.setId(202);
        parent.setChildCategories(new HashSet<>());
        parent.setCreatedBy(user);

        Category category = new Category();
        category.setId(1);
        category.setCreatedBy(user);
        category.setParentCategories(new HashSet<>(Set.of(parent)));
        parent.getChildCategories().add(category);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(new CategoryDTO());

        MyResponse<CategoryDTO> response = categoryService.updateCategory("john", 1, req);

        assertThat(response).isNotNull();
        assertThat(category.getParentCategories()).doesNotContain(parent);
    }

    @Test
    void shouldThrowException_WhenRemovingInvalidParentCategory() {
        CategoryUpdateRequest req = new CategoryUpdateRequest();
        req.setParentCategoryIdsToRemove(Set.of(404));

        Category category = new Category();
        category.setId(1);
        category.setCreatedBy(user);
        category.setParentCategories(new HashSet<>());

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.updateCategory("john", 1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No matching parent categories");
    }

    @Test
    void shouldThrowException_WhenNoUpdateFieldsProvided() {
        CategoryUpdateRequest req = new CategoryUpdateRequest(); // empty

        Category category = new Category();
        category.setId(1);
        category.setCreatedBy(user);
        category.setParentCategories(new HashSet<>());

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.updateCategory("john", 1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one field must be provided");
    }

    // Reject Category by Admin test cases

    @Test
    void shouldRejectCategoryRequest_WhenValidPendingRequest() {
        User admin = User.builder().id(UUID.randomUUID()).username("admin").build();
        CategoryRequest request = new CategoryRequest();
        request.setId(1);
        request.setName("AI");
        request.setRequestedBy(admin);
        request.setStatus(CategoryRequestStatus.builder().name(PENDING).build());

        CategoryRequestStatus rejectedStatus = CategoryRequestStatus.builder().name(REJECTED).build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(categoryRequestRepository.findById(1)).thenReturn(Optional.of(request));
        when(categoryRequestStatusRepository.findByName(REJECTED)).thenReturn(Optional.of(rejectedStatus));
        when(modelMapper.map(any(), eq(CategoryRequestDTO.class))).thenReturn(new CategoryRequestDTO());

        MyResponse<CategoryRequestDTO> response = categoryService.rejectCategoryRequest("admin", 1, "Duplicate request");

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("rejected");
        verify(categoryRequestRepository).save(request);
    }

    @Test
    void shouldThrowNotFound_WhenRequestDoesNotExist() {
        when(categoryRequestRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.rejectCategoryRequest("admin", 99, "Invalid ID"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrow_WhenRequestNotPending() {
        CategoryRequest request = new CategoryRequest();
        request.setId(2);
        request.setStatus(CategoryRequestStatus.builder().name(CategoryRequestStatusEnum.APPROVED).build());

        when(categoryRequestRepository.findById(2)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> categoryService.rejectCategoryRequest("admin", 2, "Already approved"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    //Test cases to check delete category

    @Test
    void deleteCategory_shouldDeleteSuccessfullyWhenSafe() {
        // Given
        String username = "bigspy";
        Integer categoryId = 5;

        // Create user
        User user = new User();
        user.setUsername(username);

        // Create category to delete
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Deletable");

        // Parent category
        Category parentCategory = new Category();
        parentCategory.setId(88);
        parentCategory.setChildCategories(new HashSet<>(Set.of(category)));

        category.setParentCategories(new HashSet<>(Set.of(parentCategory)));

        // Child category
        Category childCategory = new Category();
        childCategory.setId(777);
        childCategory.setParentCategories(new HashSet<>(Set.of(category)));
        category.setChildCategories(new HashSet<>(Set.of(childCategory)));

        // Dataset
        Dataset dataset = new Dataset();
        dataset.setId(101);
        dataset.setCategories(new HashSet<>(Set.of(category)));
        category.setDatasets(new HashSet<>(Set.of(dataset)));

        // Model assigned to multiple categories
        Model sharedModel = new Model();
        sharedModel.setId(22);
        sharedModel.setCategories(new HashSet<>());

        Category otherCategory = new Category();
        otherCategory.setId(999);

        sharedModel.getCategories().add(category);
        sharedModel.getCategories().add(otherCategory);
        category.setModels(new HashSet<>(Set.of(sharedModel)));

        // Mock dependencies
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        doReturn(parentCategory).when(categoryService).findClosestParent(category);

        // When
        MyResponse<Void> response = categoryService.deleteCategory(username, categoryId);

        // Then
        assertThat(response.getMessage()).isEqualTo("Category deleted successfully");

        // Verify deletion logic
        verify(categoryRepository).save(category);

        // Assert child category was moved
        assertThat(childCategory.getParentCategories()).contains(parentCategory);

        // Assert dataset was moved
        assertThat(dataset.getCategories()).contains(parentCategory);

        // Assert model categories are updated
        assertThat(sharedModel.getCategories()
                .stream()
                .map(Category::getId)
                .collect(Collectors.toSet()))
                .doesNotContain(category.getId());
    }


}
