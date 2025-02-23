package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.payload.response.ErrorResponse;
import com.cloud_ml_app_thesis.payload.response.CustomResponse;
import com.cloud_ml_app_thesis.service.ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
                    array = @ArraySchema(schema = @Schema(implementation = CustomResponse.class)))
    )
    @ResponseBody
    @GetMapping(path = "/status/{modelId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CustomResponse getModelStatus(@PathVariable int modelId){
        return new ErrorResponse("Error");
    }
    @GetMapping("/status/models-by-status")
    public CustomResponse getModelsByStatus(@PathVariable int statusId){
        return new ErrorResponse("Error");
    }
    @GetMapping("/accessibility/model/{modelId}")
    public CustomResponse getModelAccessibilityByModelId(@PathVariable int modelId){
        return new ErrorResponse("Error");
    }

    @GetMapping("/accessibility/{accessibilityId}")
    public CustomResponse getModelsByAccessibility(@PathVariable int accessibilityId){
        //TODO Must check if the type is "SHARED" to find all the "shared" models that Belong, or have been shared
        // to the user Requesting him
        return new ErrorResponse("Error");
    }

    @GetMapping("/evaluation/model/{modelId}")
    public CustomResponse getEvaluationByModelId(@PathVariable int modelId){
        //TODO consider the shared problem
        return new ErrorResponse("Error");
    }

    @GetMapping("/train/{trainingId}")
    public CustomResponse getModels(@PathVariable int trainingId){
        return new ErrorResponse("Error");
    }
    @GetMapping("/user/{userId}")
    public CustomResponse getModelsByUser(@PathVariable String userId){
        return new ErrorResponse("Error");
    }
    @GetMapping("/executions/model/number-of-executions/{modelId}")
    public CustomResponse getNumberOfExecutionsByModelId(@PathVariable int modelId){
        return new ErrorResponse("Error");
    }
    @GetMapping("/executions/model/{modelId}")
    public CustomResponse getExecutionsByModelId(@PathVariable int modelId){
        //TODO consider the model access if its is shared, otherwise only the admin and the model owner can see
        return new ErrorResponse("Error");
    }   
    public CustomResponse getModels(){
        return new ErrorResponse("Error");
    }



}
