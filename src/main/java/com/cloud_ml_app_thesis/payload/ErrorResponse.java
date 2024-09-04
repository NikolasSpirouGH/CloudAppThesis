package com.cloud_ml_app_thesis.payload;

import com.cloud_ml_app_thesis.payload.response.CustomResponse;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse implements CustomResponse {
//    private String error;
    private String message;

}