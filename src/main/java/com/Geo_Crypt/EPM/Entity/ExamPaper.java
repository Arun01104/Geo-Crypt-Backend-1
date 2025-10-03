package com.Geo_Crypt.EPM.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;


@Entity
@Table(name= "exam_paper")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamPaper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String subject;
    private LocalDateTime examStart;
    private LocalDateTime examEnd;
    private int threshold;

    @ManyToOne
    private Polygon polygon;

    // private Long cryptoPaperId;
}
