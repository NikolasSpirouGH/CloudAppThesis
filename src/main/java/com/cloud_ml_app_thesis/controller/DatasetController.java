package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.dto.request.dataset.*;
import com.cloud_ml_app_thesis.dto.response.MyResponse;
import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.entity.dataset.Dataset;
import com.cloud_ml_app_thesis.enumeration.UserRoleEnum;

import com.cloud_ml_app_thesis.repository.UserRepository;
import com.cloud_ml_app_thesis.service.DatasetService;
import com.cloud_ml_app_thesis.service.DatasetSharingService;
import jakarta.persistence.EntityNotFoundException;
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
    private final UserRepository userRepository;

    @PostMapping("/search")
    public ResponseEntity<Page<Dataset>> searchDatasets(
            @RequestBody DatasetSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadDate") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        return ResponseEntity.ok(datasetService.searchDatasets(request, page, size, sortBy, sortDirection));
    }


    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MyResponse<?>> uploadDataset(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute DatasetUploadRequest request) {
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        MyResponse<?> datasetResponse = datasetService.uploadDataset(request.getFile(), user);
        if (datasetResponse.getErrorCode() != null && !datasetResponse.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(datasetResponse);
        }
        return ResponseEntity.ok().body(datasetResponse);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'DATASET_MANAGER')")
    public ResponseEntity<MyResponse<?>> createDataset(@ModelAttribute DatasetCreateRequest request) {

        MyResponse<?> response = datasetService.createDataset(request);
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MyResponse<?>> updateDataset(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute DatasetUpdateRequest request) {
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        MyResponse<?> response = datasetService.uploadDataset(request.getFile(), user);
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<MyResponse<?>> getDatasets(@AuthenticationPrincipal UserDetails userDetails) {
        String username = null;
        if(userDetails != null){
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            if(roles.contains(UserRoleEnum.DATASET_MANAGER.toString()) || roles.contains(UserRoleEnum.ADMIN.toString())){

            }
        }
        MyResponse<?> response = datasetService.getDatasets(username);
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/infos/{id}")
    public ResponseEntity<MyResponse<?>> getDatasetsInfo(@AuthenticationPrincipal UserDetails userDetails) {
        String username = null;
        if(userDetails != null){
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            if(roles.contains(UserRoleEnum.DATASET_MANAGER.toString()) || roles.contains(UserRoleEnum.ADMIN.toString())){

            }
        }
        MyResponse<?> response = datasetService.getDatasets(username);
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MyResponse<?>> getDataset(@PathVariable String id) {

        MyResponse<?> response = null;
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }
    @GetMapping("/info/{id}")
    public ResponseEntity<MyResponse<?>> getDatasetInfo(@PathVariable String id) {

        MyResponse<?> response = null;
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<MyResponse<?>> downloadDataset(@PathVariable String id) {

        MyResponse<?> response = null;
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok().body(response);
    }
    @GetMapping("/{id}/category")
    public ResponseEntity<MyResponse<?>> getDatasetsUrls(@RequestParam String email){
        MyResponse<?> response = datasetService.getDatasetUrls(email);
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
