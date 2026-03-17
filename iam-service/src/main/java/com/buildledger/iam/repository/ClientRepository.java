package com.buildledger.iam.repository;

import com.buildledger.iam.entity.Client;
import com.buildledger.iam.entity.ClientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Client> findByStatus(ClientStatus status, Pageable pageable);

    @Query("SELECT c FROM Client c WHERE " +
           "(:search IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Client> searchClients(@Param("search") String search, Pageable pageable);
}
