package com.bci.reto.service.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Table("phones")
public class Phone {
    @Id
    private Long id;

    @Column
    private Long number;

    @Column
    private Integer citycode;

    @Column
    private String contrycode;

    @Column("user_id")
    private UUID userId;
}
