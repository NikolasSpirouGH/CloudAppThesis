package com.cloud_ml_app_thesis.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ObjectsDataResponse implements CustomResponse  {
    private List<?> LIST_DATA;
}
