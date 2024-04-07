package com.backend.mlapp.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import com.backend.mlapp.enumeration.TrainingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class TrainingStatusResponse {
    private Integer trainingId;
    private TrainingStatus status;
    private String logDetails;
    private LocalDateTime completionDate;
}
