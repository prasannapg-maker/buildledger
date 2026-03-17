package com.example.contractcreation.Repository;

import com.example.contractcreation.model.ContractTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractTermRepository extends JpaRepository<ContractTerm,Long> {
}
