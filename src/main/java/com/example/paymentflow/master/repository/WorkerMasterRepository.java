package com.example.paymentflow.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.paymentflow.master.entity.WorkerMaster;

@Repository
public interface WorkerMasterRepository extends JpaRepository<WorkerMaster, Long> {
}
