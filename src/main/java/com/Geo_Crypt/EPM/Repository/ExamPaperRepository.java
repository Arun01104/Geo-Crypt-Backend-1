package com.Geo_Crypt.EPM.Repository;

import com.Geo_Crypt.EPM.Entity.ExamPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamPaperRepository extends JpaRepository<ExamPaper,Long> {
    List<ExamPaper> findByPolygonId(Long polygonId);
}
