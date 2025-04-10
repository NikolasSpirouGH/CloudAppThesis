package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.dto.request.category.CategoryCreateRequest;
import com.cloud_ml_app_thesis.dto.request.category.CategoryRejectRequest;
import com.cloud_ml_app_thesis.dto.request.category.CategoryUpdateRequest;
import com.cloud_ml_app_thesis.entity.Category;
import com.cloud_ml_app_thesis.entity.CategoryRequest;
import com.cloud_ml_app_thesis.entity.Role;
import com.cloud_ml_app_thesis.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@AuthenticationPrincipal UserDetails userDetails, @PathVariable @Positive Integer id){
        String username = userDetails.getUsername();
        categoryService.deleteCategory(username, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CategoryRequest> createCategory(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody CategoryCreateRequest request){
            String username = userDetails.getUsername();
            return new ResponseEntity<>(categoryService.createCategory(username, request), HttpStatus.CREATED);
    }

    @PostMapping("{requestId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'CATEGORY_MANAGER')")
    public ResponseEntity<Category> approveCategoryRequest(@AuthenticationPrincipal UserDetails userDetails, @PathVariable @Positive Integer requestId){
        String username = userDetails.getUsername();
        return new ResponseEntity<>(categoryService.approveCategoryRequest(username, requestId), HttpStatus.CREATED);
    }

    //@RequestBody with only the rejection reason to prevent URL length restrictions and encoding issues using @RequestParam
    @PatchMapping("{requestId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'CATEGORY_MANAGER')")
    public ResponseEntity<Category> rejectCategoryRequest(@AuthenticationPrincipal UserDetails userDetails, @PathVariable @Positive Integer requestId, @RequestBody CategoryRejectRequest request){
        String username = userDetails.getUsername();
        return new ResponseEntity<>(categoryService.rejectCategoryRequest(username, requestId, request.getRejectionReason()), HttpStatus.CREATED);
    }


    @PatchMapping("{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CATEGORY_MANAGER')")
    public ResponseEntity<Category> updateCategory(@AuthenticationPrincipal UserDetails userDetails, @PathVariable @Positive Integer id, @RequestBody CategoryUpdateRequest request){
        String username = userDetails.getUsername();
        List<String> userRoles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if(request.getNewId() != null && !userRoles.contains("ROLE_ADMIN")){
            throw new AccessDeniedException("Unauthorized: You don't have access to modify the id of the algorithm.");
        }

        return new ResponseEntity<>(categoryService.updateCategory(username, id, request), HttpStatus.CREATED);
    }

}
