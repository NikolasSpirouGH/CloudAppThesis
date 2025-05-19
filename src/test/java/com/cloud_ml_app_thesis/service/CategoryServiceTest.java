package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.dto.request.category.CategoryCreateRequest;
import com.cloud_ml_app_thesis.dto.category.CategoryRequestDTO;
import com.cloud_ml_app_thesis.entity.*;
import com.cloud_ml_app_thesis.entity.status.CategoryRequestStatus;
import com.cloud_ml_app_thesis.enumeration.status.CategoryRequestStatusEnum;
import com.cloud_ml_app_thesis.repository.*;
import com.cloud_ml_app_thesis.repository.status.CategoryRequestStatusRepository;
import com.cloud_ml_app_thesis.dto.response.MyResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryRequestRepository categoryRequestRepository;
    @Mock private CategoryRequestStatusRepository categoryRequestStatusRepository;
    @Mock ModelMapper modelMapper;

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
        when(categoryRequestStatusRepository.findByName(CategoryRequestStatusEnum.PENDING)).thenReturn(Optional.of(pendingStatus));
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
    void shouldApproveCategoryRequest_WhenValidAndUnique() {
        CategoryRequest request = new CategoryRequest();
        request.setId(1);
        request.setName("AI");
        request.setDescription("Artificial Intelligence");
        request.setStatus(CategoryRequestStatus.builder().name(CategoryRequestStatusEnum.PENDING).build());
        request.setRequestedBy(user);
        request.setParentCategories(Set.of());

        CategoryRequestDTO mockDto = new CategoryRequestDTO();
        mockDto.setName("AI");
        mockDto.setDescription("Artificial Intelligence");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(categoryRequestRepository.findById(1)).thenReturn(Optional.of(request));
        when(categoryRepository.findByName("AI")).thenReturn(Optional.empty());
        when(categoryRequestStatusRepository.findByName(CategoryRequestStatusEnum.APPROVED))
                .thenReturn(Optional.of(CategoryRequestStatus.builder().name(CategoryRequestStatusEnum.APPROVED).build()));
        when(modelMapper.map(any(CategoryRequest.class), eq(CategoryRequestDTO.class))).thenReturn(mockDto);

        MyResponse<CategoryRequestDTO> response = categoryService.approveCategoryRequest("john", 1);

        assertThat(response).isNotNull();
        assertThat(response.getDataHeader().getName()).isEqualTo("AI");
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(categoryRequestRepository, times(1)).save(any(CategoryRequest.class));
    }

    @Test
    void shouldThrow_WhenApprovingNonPendingRequest() {
        CategoryRequest request = new CategoryRequest();
        request.setId(1);
        request.setName("AI");
        request.setStatus(CategoryRequestStatus.builder().name(CategoryRequestStatusEnum.APPROVED).build());

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(categoryRequestRepository.findById(1)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> categoryService.approveCategoryRequest("john", 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending requests can be approved");
    }

    @Test
    void shouldThrow_WhenCategoryNameAlreadyExists() {
        CategoryRequest request = new CategoryRequest();
        request.setId(1);
        request.setName("AI");
        request.setStatus(CategoryRequestStatus.builder().name(CategoryRequestStatusEnum.PENDING).build());
        request.setRequestedBy(user);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(categoryRequestRepository.findById(1)).thenReturn(Optional.of(request));
        when(categoryRepository.findByName("AI")).thenReturn(Optional.of(new Category()));

        assertThatThrownBy(() -> categoryService.approveCategoryRequest("john", 1))
                .isInstanceOf(EntityExistsException.class);
    }

    @Test
    void shouldThrow_WhenUserNotFound() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.approveCategoryRequest("john", 1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void shouldThrow_WhenRequestNotFound() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(categoryRequestRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.approveCategoryRequest("john", 1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category request not found");
    }

}
