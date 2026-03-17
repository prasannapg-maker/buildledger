package com.example.contractcreation.service;

import com.example.contractcreation.Repository.ContractTermRepository;
import com.example.contractcreation.model.ContractTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractTermService {
    @Autowired
    private ContractTermRepository contractTermRepository;

    public ContractTerm createContractTerm(ContractTerm contractTerm) {
        return contractTermRepository.save(contractTerm);
    }

    public List<ContractTerm> getAllContractTerms() {
        return contractTermRepository.findAll();
    }

    public ContractTerm getContractTermById(Long id) {
        return contractTermRepository.findById(id).orElse(null);
    }

    public ContractTerm updateContractTerm(Long id, ContractTerm contractTerm) {
        ContractTerm existingTerm = contractTermRepository.findById(id).orElse(null);

        if (existingTerm != null) {
            existingTerm.setDescription(contractTerm.getDescription());
            existingTerm.setContract(contractTerm.getContract());
            return contractTermRepository.save(existingTerm);
        }

        return null;
    }

    public void deleteContractTerm(Long id) {
        contractTermRepository.deleteById(id);
    }
}
