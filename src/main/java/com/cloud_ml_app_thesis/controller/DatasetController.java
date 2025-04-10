package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.dto.request.dataset.*;
import com.cloud_ml_app_thesis.dto.response.ApiResponse;
import com.cloud_ml_app_thesis.dto.response.Metadata;
import com.cloud_ml_app_thesis.entity.dataset.Dataset;
import com.cloud_ml_app_thesis.enumeration.UserRoleEnum;

import com.cloud_ml_app_thesis.service.DatasetService;
import com.cloud_ml_app_thesis.service.DatasetSharingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//TODO Apply Auth logic in the endpoint and decide either to pass the roles and username from Controller to Service either get them in the Service

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/datasets")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService datasetService;
    private final DatasetSharingService datasetSharingService;

    @PostMapping("/search")
    public ResponseEntity<Page<Dataset>> searchDatasets(
            @RequestBody DatasetSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadDate") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        return ResponseEntity.ok(datasetService.searchDatasets(request, page, size, sortBy, sortDirection));
    }


    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> uploadDataset(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute DatasetUploadRequest request) {
        String username = userDetails.getUsername();

        ApiResponse<?> response = datasetService.uploadDataset(file, request);
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DATASET_MANAGER')")
    public ResponseEntity<ApiResponse<?>> createDataset(@ModelAttribute DatasetCreateRequest request) {

        ApiResponse<?> response = datasetService.createDataset(request);
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> updateDataset(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute DatasetUpdateRequest request) {
        String username = userDetails.getUsername();

        ApiResponse<?> response = datasetService.uploadDataset(file, username);
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getDatasets(@AuthenticationPrincipal UserDetails userDetails) {
        String username = null;
        if(userDetails != null){
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            if(roles.contains(UserRoleEnum.DATASET_MANAGER.toString()) || roles.contains(UserRoleEnum.ADMIN.toString())){

            }
        }
        ApiResponse<?> response = datasetService.getDatasets(username);
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<ApiResponse<?>> getDatasetsInfo(@AuthenticationPrincipal UserDetails userDetails) {
        String username = null;
        if(userDetails != null){
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            if(roles.contains(UserRoleEnum.DATASET_MANAGER.toString()) || roles.contains(UserRoleEnum.ADMIN.toString())){

            }
        }
        ApiResponse<?> response = datasetService.getDatasets(username);
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getDataset(@PathVariable String id) {

        ApiResponse<?> response = datasetService.getDataset(id);
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }
    @GetMapping("/info/{id}")
    public ResponseEntity<ApiResponse<?>> getDatasetInfo(@PathVariable String id) {

        ApiResponse<?> response = datasetService.getDatasetInfo(id);
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<ApiResponse<?>> downloadDataset(@PathVariable String id) {

        ApiResponse<?> response = datasetService.downloadDataset(id);
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }
    @GetMapping("/{id}/category")
    public ResponseEntity<ApiResponse<?>> getDatasetsUrls(@RequestParam String email){
        ApiResponse<?> response = datasetService.getDatasetUrls(email);
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }
//    @GetMapping("/urls")
//    public ResponseEntity<ApiResponse<?>> getDatasetsUrls(@RequestParam String email){
//        ApiResponse<?> response = datasetService.getDatasetUrls(email);
//        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
//            return ResponseEntity.internalServerError().body(response);
//        }
//        return ResponseEntity.ok().body(response);
//    }



}
