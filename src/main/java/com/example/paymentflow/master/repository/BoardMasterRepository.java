package com.example.paymentflow.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.paymentflow.master.entity.BoardMaster;

@Repository
public interface BoardMasterRepository extends JpaRepository<BoardMaster, Long> {
}
