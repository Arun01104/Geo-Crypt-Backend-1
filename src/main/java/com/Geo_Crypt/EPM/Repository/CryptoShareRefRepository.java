package com.Geo_Crypt.EPM.Repository;

import com.Geo_Crypt.EPM.Entity.CryptoShareRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CryptoShareRefRepository extends JpaRepository<CryptoShareRef,Long> {
    List<CryptoShareRef> findByExamPaperId(Long examPaperId);
}
