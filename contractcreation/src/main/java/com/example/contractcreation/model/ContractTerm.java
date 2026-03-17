package com.example.contractcreation.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contract_term")
public class ContractTerm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long termId;

    private String description;

    private Boolean complianceFlag;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    @JsonBackReference
    private Contract contract;
}
