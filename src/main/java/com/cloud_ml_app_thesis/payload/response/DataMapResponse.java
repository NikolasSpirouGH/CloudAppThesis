package com.cloud_ml_app_thesis.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class DataMapResponse implements CustomResponse{
        private String message;
        private Map<String, Object> DATA_RESPONSE;

}
