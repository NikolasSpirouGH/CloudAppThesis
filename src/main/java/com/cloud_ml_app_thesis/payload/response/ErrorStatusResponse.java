package com.cloud_ml_app_thesis.payload.response;

import lombok.*;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ErrorStatusResponse extends ErrorResponse{
    private HttpStatus httpStatus;

    public ErrorStatusResponse(String message, HttpStatus httpStatus){
        super(message);
        this.httpStatus = httpStatus;
    }
}
