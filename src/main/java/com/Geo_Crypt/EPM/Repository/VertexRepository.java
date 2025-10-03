package com.Geo_Crypt.EPM.Repository;

import com.Geo_Crypt.EPM.Entity.Vertex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VertexRepository extends JpaRepository<Vertex,Long> {
    List<Vertex> findByPolygonIdOrderByIdxAsc(Long polygonId);
    Optional<Vertex> findByPolygonIdAndIdx(Long polygonId, Integer idx);
}
