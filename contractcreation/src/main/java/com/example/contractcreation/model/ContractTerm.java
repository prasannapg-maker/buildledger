package com.example.contractcreation.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

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

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Compliance flag is required")
    private Boolean complianceFlag;

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    @NotNull(message = "Contract is required")
    @JsonBackReference
    private Contract contract;
}