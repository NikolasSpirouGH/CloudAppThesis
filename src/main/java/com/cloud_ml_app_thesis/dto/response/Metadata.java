package com.cloud_ml_app_thesis.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;
import java.time.Instant;
@AllArgsConstructor
@Setter
@Getter
public class Metadata {
    private Instant timestamp;
    private String transactionId;
    public Metadata(){
        this.timestamp = Instant.now();
        this.transactionId = UUID.randomUUID().toString();
    }
    public void initialize(){
        this.timestamp = Instant.now();
        this.transactionId = UUID.randomUUID().toString();
    }
}

