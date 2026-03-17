package com.buildledger.iam.service;

import com.buildledger.iam.dto.request.ClientRegistrationRequest;
import com.buildledger.iam.dto.response.ClientResponse;
import com.buildledger.iam.entity.Client;
import com.buildledger.iam.entity.ClientStatus;
import com.buildledger.iam.exception.DuplicateResourceException;
import com.buildledger.iam.exception.ResourceNotFoundException;
import com.buildledger.iam.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;
    private final AuditService auditService;
    private final EmailService emailService;

    /**
     * External client self-registration.
     */
    @Transactional
    public ClientResponse registerClient(ClientRegistrationRequest request, String ipAddress) {
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Client with email '" + request.getEmail() + "' already exists");
        }

        Client client = Client.builder()
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .projectDescription(request.getProjectDescription())
                .status(ClientStatus.PENDING_APPROVAL)
                .build();

        client = clientRepository.save(client);

        auditService.logSuccess(null, "CLIENT_REGISTERED", "clients/" + client.getId(), ipAddress);
        log.info("Client registered: id={}, email={}", client.getId(), client.getEmail());
        return ClientResponse.from(client);
    }

    /**
     * Get client by ID.
     */
    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long clientId) {
        return clientRepository.findById(clientId)
                .map(ClientResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.client(clientId));
    }

    /**
     * List clients (admin).
     */
    @Transactional(readOnly = true)
    public Page<ClientResponse> getAllClients(String search, ClientStatus status, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return clientRepository.searchClients(search.trim(), pageable).map(ClientResponse::from);
        }
        if (status != null) {
            return clientRepository.findByStatus(status, pageable).map(ClientResponse::from);
        }
        return clientRepository.findAll(pageable).map(ClientResponse::from);
    }

    /**
     * Admin approves a client.
     */
    @Transactional
    public ClientResponse approveClient(Long clientId, String approvedByEmail, String ipAddress) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> ResourceNotFoundException.client(clientId));

        client.setStatus(ClientStatus.APPROVED);
        client.setApprovedAt(LocalDateTime.now());
        client.setApprovedBy(approvedByEmail);
        client.setRejectionReason(null);
        client = clientRepository.save(client);

        auditService.logSuccess(null, "CLIENT_APPROVED", "clients/" + clientId, ipAddress);
        return ClientResponse.from(client);
    }

    /**
     * Admin rejects a client.
     */
    @Transactional
    public ClientResponse rejectClient(Long clientId, String reason,
                                        String rejectedByEmail, String ipAddress) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> ResourceNotFoundException.client(clientId));

        client.setStatus(ClientStatus.REJECTED);
        client.setRejectionReason(reason);
        client = clientRepository.save(client);

        auditService.logSuccess(null, "CLIENT_REJECTED", "clients/" + clientId, ipAddress);
        return ClientResponse.from(client);
    }
}
