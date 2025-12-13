package com.globalcluster.gateway.repository;

import com.globalcluster.gateway.model.NodeRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeRegistrationRepository extends JpaRepository<NodeRegistrationEntity, String> {
    // JpaRepository fornece métodos CRUD básicos (save, findById, findAll, delete, etc.)
}
