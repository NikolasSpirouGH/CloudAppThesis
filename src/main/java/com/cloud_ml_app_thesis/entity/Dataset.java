package com.cloud_ml_app_thesis.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="datasets")
public class Dataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String fileName;

    @Column
    private String fileUrl;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    //we would keep the configuration of the dataset cause it is referred also to the Training, so and to the Model
//    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = false)
//    @JoinColumn(name = "id", referencedColumnName = "id")
//    private List<DatasetConfiguration> datasetConfiguration;


    //Or move this method to AlgorithmService cause an Algorithm configuration in general is part of an Algorithm,
    //
//    public boolean mergeAlgorithmParameters(String parametersAsJsonIthink){
//        //remove try catch if not necessary
//        try{
//            //mergeLogic here
//        } catch{
//            return false;
//        }
//    }
}
