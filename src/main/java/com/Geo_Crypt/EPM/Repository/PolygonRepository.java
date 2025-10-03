package com.Geo_Crypt.EPM.Repository;

import com.Geo_Crypt.EPM.Entity.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolygonRepository extends JpaRepository<Polygon,Long> {
}
