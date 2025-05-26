package com.cloud_ml_app_thesis.dto.response;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class MyResponse<T> {
    private T dataHeader;
    private String errorCode;
    private String message;
    private Metadata metadata;


}