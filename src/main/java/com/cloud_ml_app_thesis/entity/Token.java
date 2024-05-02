package com.cloud_ml_app_thesis.entity;

import jakarta.persistence.*;

import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "tokens")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String token;

    @OneToOne(targetEntity = com.cloud_ml_app_thesis
.entity.AppUser.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "id")
    private com.cloud_ml_app_thesis
.entity.AppUser user;

    @Column
    private Date expiryDate;

}
