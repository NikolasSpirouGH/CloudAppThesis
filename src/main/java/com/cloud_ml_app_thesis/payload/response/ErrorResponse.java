package com.cloud_ml_app_thesis.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse implements CustomResponse{
    //TODO maybe need to be protected
    private String ERROR_MESSAGE;
    private String exceptionMessage;

    public ErrorResponse(String error){
        this.ERROR_MESSAGE = error;
    }
}
