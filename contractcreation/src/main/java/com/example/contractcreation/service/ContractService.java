package com.example.contractcreation.service;

import com.example.contractcreation.Repository.ContractRepository;
import com.example.contractcreation.enums.ContractStatus;
import com.example.contractcreation.model.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractService {
    @Autowired
    private ContractRepository contractRepository;

    public Contract createContract(Contract contract) {
        contract.setStatus(ContractStatus.DRAFT);
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

    public Contract updateStatus(Long id, String status) {

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        ContractStatus current = contract.getStatus();
        ContractStatus newStatus = ContractStatus.valueOf(status);

        if (!isValidTransition(current, newStatus)) {
            throw new RuntimeException(
                    "Invalid status transition from " + current + " to " + newStatus
            );
        }

        contract.setStatus(newStatus);
        return contractRepository.save(contract);
    }

    private boolean isValidTransition(ContractStatus current, ContractStatus next) {

        switch (current) {

            case DRAFT:
                return next == ContractStatus.ACTIVE || next == ContractStatus.CANCELLED;

            case ACTIVE:
                return next == ContractStatus.IN_PROGRESS || next == ContractStatus.CANCELLED;

            case IN_PROGRESS:
                return next == ContractStatus.COMPLETED || next == ContractStatus.CANCELLED;

            case COMPLETED:
                return next == ContractStatus.CLOSED;

            case CLOSED:
            case CANCELLED:
                return false; // End states

            default:
                return false;
        }
    }
}
