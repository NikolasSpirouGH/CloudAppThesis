package com.cloud_ml_app_thesis.payload.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class SingleObjectDataResponse implements CustomResponse{

    private Object SINGLE_DATA;

}
