package com.brightcore.suppliermanagement.repository;

import com.brightcore.suppliermanagement.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
