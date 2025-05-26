package com.cloud_ml_app_thesis.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GenericResponse<T> {

    private T dataHeader;
    private String errorCode;
    private String message;
    private Metadata metadata;
}