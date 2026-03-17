package com.example.contractcreation.service;

import com.example.contractcreation.Repository.ContractRepository;
import com.example.contractcreation.model.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractService {
    @Autowired
    private ContractRepository contractRepository;

    public Contract createContract(Contract contract) {
        return contractRepository.save(contract);
    }

    public List<Contract> getAllContracts() {
        return contractRepository.findAll();
    }

    public Contract getContractById(Long id) {
        return contractRepository.findById(id).orElse(null);
    }

    public Contract updateContract(Long id, Contract contract) {
        Contract existingContract = contractRepository.findById(id).orElse(null);

        if (existingContract != null) {
            existingContract.setVendorId(contract.getVendorId());
            existingContract.setProject(contract.getProject());
            existingContract.setStartDate(contract.getStartDate());
            existingContract.setEndDate(contract.getEndDate());
            existingContract.setValue(contract.getValue());
            existingContract.setStatus(contract.getStatus());
            return contractRepository.save(existingContract);
        }

        return null;
    }

    public void deleteContract(Long id) {
        contractRepository.deleteById(id);
    }
}
