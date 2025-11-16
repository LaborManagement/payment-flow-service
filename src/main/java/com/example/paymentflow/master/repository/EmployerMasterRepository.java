package com.example.paymentflow.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.paymentflow.master.entity.EmployerMaster;

@Repository
public interface EmployerMasterRepository extends JpaRepository<EmployerMaster, Long> {
}
