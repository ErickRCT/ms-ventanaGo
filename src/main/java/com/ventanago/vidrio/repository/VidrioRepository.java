package com.ventanago.vidrio.repository;

import com.ventanago.vidrio.repository.entity.Vidrio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VidrioRepository extends JpaRepository<Vidrio, Long> {

}
