package com.example.paymentflow.utilities.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
    // READ operations - to be moved to UploadedFileQueryDao in future
    Optional<UploadedFile> findByFileHash(String fileHash);
    Optional<UploadedFile> findByFilename(String filename);
    Optional<UploadedFile> findByStoredPath(String storedPath);
    List<UploadedFile> findByFileType(String fileType);
    List<UploadedFile> findByStatus(String status);
    List<UploadedFile> findByFileTypeOrderByUploadDateDesc(String fileType);
    List<UploadedFile> findByStatusOrderByUploadDateDesc(String status);

    @Query("SELECT uf FROM UploadedFile uf WHERE uf.filename = :filename AND (uf.status IS NULL OR uf.status <> 'SUPERSEDED')")
    Optional<UploadedFile> findActiveByFilename(@Param("filename") String filename);

    @Query("SELECT uf FROM UploadedFile uf WHERE uf.fileHash = :fileHash AND (uf.status IS NULL OR uf.status <> 'SUPERSEDED')")
    Optional<UploadedFile> findActiveByFileHash(@Param("fileHash") String fileHash);
    
    @Query("SELECT uf FROM UploadedFile uf WHERE uf.uploadDate BETWEEN :startDate AND :endDate ORDER BY uf.uploadDate DESC")
    List<UploadedFile> findByUploadDateBetweenOrderByUploadDateDesc(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT uf FROM UploadedFile uf WHERE uf.uploadDate >= :date ORDER BY uf.uploadDate DESC")
    List<UploadedFile> findByUploadDateGreaterThanEqualOrderByUploadDateDesc(@Param("date") LocalDateTime date);
    
    @Query("SELECT uf FROM UploadedFile uf WHERE uf.uploadDate <= :date ORDER BY uf.uploadDate DESC")
    List<UploadedFile> findByUploadDateLessThanEqualOrderByUploadDateDesc(@Param("date") LocalDateTime date);
    
    @Query("SELECT uf FROM UploadedFile uf WHERE uf.uploadDate >= :startOfDay AND uf.uploadDate < :startOfNextDay ORDER BY uf.uploadDate DESC")
    List<UploadedFile> findByUploadDateOnly(@Param("startOfDay") LocalDateTime startOfDay, @Param("startOfNextDay") LocalDateTime startOfNextDay);
    
    Page<UploadedFile> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT uf FROM UploadedFile uf WHERE uf.uploadDate BETWEEN :startDate AND :endDate")
    Page<UploadedFile> findByUploadDateBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate, 
                                               Pageable pageable);
    
    @Query("SELECT uf FROM UploadedFile uf WHERE uf.status = :status AND uf.uploadDate BETWEEN :startDate AND :endDate")
    Page<UploadedFile> findByStatusAndUploadDateBetween(@Param("status") String status,
                                                        @Param("startDate") LocalDateTime startDate, 
                                                        @Param("endDate") LocalDateTime endDate, 
                                                        Pageable pageable);
}
