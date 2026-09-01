package com.example.parameterapproval.change;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long> {

    Page<ChangeRequest> findByStatusOrderByCreatedAtDesc(ChangeRequestStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    @Query("select r from ChangeRequest r where r.id = :id")
    Optional<ChangeRequest> findDetailedById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "items")
    @Query("select r from ChangeRequest r where r.id = :id")
    Optional<ChangeRequest> findForUpdate(@Param("id") Long id);
}

