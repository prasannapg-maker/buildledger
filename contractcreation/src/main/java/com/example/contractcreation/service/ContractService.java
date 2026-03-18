package com.example.contractcreation.service;

import com.example.contractcreation.Repository.ContractRepository;
import com.example.contractcreation.model.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Service
public class ContractService {
    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String VENDOR_SERVICE_URL = "http://vendor-service/internal/vendors/";

    private void validateVendor(Long vendorId) {
        if (vendorId == null) {
            throw new IllegalArgumentException("Vendor ID must not be null");
        }
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(VENDOR_SERVICE_URL + vendorId + "/status", String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalArgumentException("Invalid Vendor ID: " + vendorId);
            }
        } catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("Vendor not found with ID: " + vendorId);
        } catch (Exception e) {
            throw new RuntimeException("Error communicating with Vendor Service: " + e.getMessage());
        }
    }

    public Contract createContract(Contract contract) {
        validateVendor(contract.getVendorId());
        return contractRepository.save(contract);
    }

    public List<Contract> getAllContracts() {
        return contractRepository.findAll();
    }

    public Contract getContractById(Long id) {
        return contractRepository.findById(id).orElse(null);
    }

    public Contract updateContract(Long id, Contract contract) {
        validateVendor(contract.getVendorId());
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
