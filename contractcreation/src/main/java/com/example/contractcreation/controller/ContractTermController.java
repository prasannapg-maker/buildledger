package com.example.contractcreation.controller;

import com.example.contractcreation.model.ContractTerm;
import com.example.contractcreation.service.ContractTermService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/contract-terms")
public class ContractTermController {
    @Autowired
    private ContractTermService contractTermService;

    @PostMapping
    public ResponseEntity<ContractTerm> createContractTerm(@Valid @RequestBody ContractTerm contractTerm) {
        ContractTerm savedTerm = contractTermService.createContractTerm(contractTerm);
        URI location = URI.create("/contract-terms/" + savedTerm.getTermId());
        return ResponseEntity.created(location).body(savedTerm);
    }

    @GetMapping
    public ResponseEntity<List<ContractTerm>> getAllContractTerms() {
        return ResponseEntity.ok(contractTermService.getAllContractTerms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractTerm> getContractTermById(@PathVariable Long id) {
        ContractTerm term = contractTermService.getContractTermById(id);
        if (term == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(term);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContractTerm> updateContractTerm(@PathVariable Long id,@Valid @RequestBody ContractTerm contractTerm) {
        ContractTerm updatedTerm = contractTermService.updateContractTerm(id, contractTerm);
        if (updatedTerm == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedTerm);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContractTerm(@PathVariable Long id) {
        contractTermService.deleteContractTerm(id);
        return ResponseEntity.ok("Contract term deleted successfully");
    }
}
