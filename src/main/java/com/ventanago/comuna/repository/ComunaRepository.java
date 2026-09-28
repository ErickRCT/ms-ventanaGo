package com.ventanago.comuna.repository;

import com.ventanago.comuna.repository.entity.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComunaRepository extends JpaRepository<Comuna, Long> {
    
    List<Comuna> findByRegion_RegionId(Long regionRegionId);
    
}
