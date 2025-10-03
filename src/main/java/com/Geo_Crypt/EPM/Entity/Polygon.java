package com.Geo_Crypt.EPM.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "polygons")
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class Polygon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany(mappedBy = "polygon", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Vertex> vertices;
}
