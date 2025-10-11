package com.task10.repository;

import com.task10.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByName(String name);

    Optional<Tenant> findByDatabaseName(String databaseName);

    boolean existsByName(String name);
}