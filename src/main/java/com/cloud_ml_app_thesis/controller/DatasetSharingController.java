package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.dto.request.dataset.DatasetRemoveSharedUsersRequest;
import com.cloud_ml_app_thesis.dto.request.dataset.DatasetShareRequest;
import com.cloud_ml_app_thesis.entity.dataset.Dataset;
import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.enumeration.UserRoleEnum;
import com.cloud_ml_app_thesis.service.DatasetSharingService;
import com.cloud_ml_app_thesis.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/datasets")
@RequiredArgsConstructor
public class DatasetSharingController {

    private final DatasetSharingService datasetSharingService;

    @PostMapping("/{datasetId}/share")
    public ResponseEntity<Void> shareDataset(
            @PathVariable Integer datasetId,
            @RequestBody DatasetShareRequest request,
            Authentication authentication
    ) {
        String sharedByUsername = authentication.getName();
        datasetSharingService.shareDatasetWithUsers(datasetId, request.getUsernames(), sharedByUsername, dto.getComment());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{datasetId}/share")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeSharedUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer datasetId,
            @RequestBody DatasetRemoveSharedUsersRequest request
    ) {
        datasetSharingService.removeUsersFromSharedDataset(userDetails, datasetId, request.getUsernames());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{datasetId}/copy")
    public ResponseEntity<Dataset> copyDataset(
            @PathVariable Integer datasetId,
            @RequestParam(required = false) String targetUsername,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();

        Dataset copied = datasetSharingService.copySharedDataset(
                datasetId,
                currentUser,
                targetUsername
        );

        return ResponseEntity.ok(copied);
    }

    @PostMapping("/{datasetId}/decline")
    public ResponseEntity<Void> declineDatasetShare(
            @PathVariable Integer datasetId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        datasetSharingService.declineDatasetShare(datasetId, user);
        return ResponseEntity.ok().build();
    }
}
