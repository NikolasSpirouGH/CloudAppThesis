package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.dto.request.algorithm.AlgorithmCreateRequest;
import com.cloud_ml_app_thesis.dto.request.algorithm.AlgorithmUpdateRequest;
import com.cloud_ml_app_thesis.entity.Algorithm;
import com.cloud_ml_app_thesis.service.AlgorithmService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/algorithms")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AlgorithmController {

    private final AlgorithmService algorithmService;

    @GetMapping("/get-algorithms")
    public ResponseEntity<List<Algorithm>> getAlgorithms() {
        return ResponseEntity.ok(algorithmService.getAlgorithms());
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAlgorithm(@AuthenticationPrincipal UserDetails userDetails, @PathVariable @Positive Integer id) {
        boolean deleted = algorithmService.deleteAlgorithm(id);
        if(!deleted){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Algorithm> createAlgorithm(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody AlgorithmCreateRequest request) {
        return ResponseEntity.ok(algorithmService.createAlgorithm(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ALGORITHM_MANAGER', 'ADMIN')")
    public ResponseEntity<Algorithm> updateAlgorithm(@AuthenticationPrincipal UserDetails userDetails, @PathVariable @Positive Integer id, @Valid  @RequestBody AlgorithmUpdateRequest request) {
        List<String> userRoles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if(userRoles.contains("ALGORITHM_MANAGER") && request.getNewId()!= null){
            throw new AccessDeniedException("Unauthorized: You don't have access to modify the id of the algorithm.");
        }
        return ResponseEntity.ok(algorithmService.updateAlgorithm(id, request));
    }


    //TODO check if this endpoint needs to exist && DELETE THE PARAMS because it is POST
    @PostMapping("/choose-algorithm")
    public ResponseEntity<String> chooseAlgorithm(@RequestParam Integer id, @RequestParam(required= false) String options) {
        algorithmService.chooseAlgorithm(id, options);
        return new ResponseEntity<>("Algorithm configuration created.", HttpStatus.CREATED);
    }

}
