package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.dto.response.MyResponse;
import com.cloud_ml_app_thesis.service.ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("api/models")
public class ModelController {

    @Autowired
    private ModelService modelService;


    @Operation(
            summary = "Returns the status of a Model.",
            description = "Giving the modelId, the endpoint will return the status of the Model.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Raw JSON string representing the insured details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(type = "int"),
                            examples = @ExampleObject(
                                    name = "Request Body Example",
                                    value = ""
                            )
                    )
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "A Generic Response object in JSON format.",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))
    )
    @ResponseBody
    @GetMapping(path = "/status/{modelId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<MyResponse<?>> getModelStatus(@PathVariable int modelId){
        return ResponseEntity.ok(new MyResponse<>(null, null, null, null));
    }
    @GetMapping("/status/models-by-status")
    public ResponseEntity<MyResponse<?>> getModelsByStatus(@PathVariable int statusId){
        return ResponseEntity.ok(new MyResponse<>(null, null, null, null));

    }
    @GetMapping("/accessibility/model/{modelId}")
    public ResponseEntity<MyResponse<?>> getModelAccessibilityByModelId(@PathVariable int modelId){
        return ResponseEntity.ok(new MyResponse<>(null, null, null, null));
    }

    @GetMapping("/accessibility/{accessibilityId}")
    public ResponseEntity<MyResponse<?>> getModelsByAccessibility(@PathVariable int accessibilityId){
        //TODO Must check if the type is "SHARED" to find all the "shared" models that Belong, or have been shared
        // to the user Requesting him
        return ResponseEntity.ok(new MyResponse<>(null, null, null, null));
    }

    @GetMapping("/evaluation/model/{modelId}")
    public ResponseEntity<MyResponse<?>> getEvaluationByModelId(@PathVariable int modelId){
        //TODO consider the shared problem
        return ResponseEntity.ok(new MyResponse<>(null, null, null, null));
    }

    @GetMapping("/train/{trainingId}")
    public ResponseEntity<MyResponse<?>> getModels(@PathVariable int trainingId){
        return ResponseEntity.ok(new MyResponse<>(null, null, null, null));
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<MyResponse<?>> getModelsByUser(@PathVariable String userId){
        return ResponseEntity.ok(new MyResponse<>(null, null, null, null));
    }
    @GetMapping("/executions/model/number-of-executions/{modelId}")
    public ResponseEntity<MyResponse<?>> getNumberOfExecutionsByModelId(@PathVariable int modelId){
        return ResponseEntity.ok(new MyResponse<>(null, null, null, null));
    }
    @GetMapping("/executions/model/{modelId}")
    public ResponseEntity<MyResponse<?>> getExecutionsByModelId(@PathVariable int modelId){
        //TODO consider the model access if its is shared, otherwise only the admin and the model owner can see
        return ResponseEntity.ok(new MyResponse<>(null, null, null, null));
    }   
    public ResponseEntity<MyResponse<?>> getModels(){
        return ResponseEntity.ok(new MyResponse<>(null, null, null, null));
    }



}
