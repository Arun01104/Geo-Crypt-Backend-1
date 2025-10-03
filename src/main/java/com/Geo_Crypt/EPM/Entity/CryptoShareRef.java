package com.Geo_Crypt.EPM.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "crypto_share_ref")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CryptoShareRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_paper_id", nullable = false)
    private ExamPaper examPaper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_id", nullable = false)
    private Vertex vertex;
    @Column(name = "crypto_share_id", nullable = false)
    private Long cryptoShareId;


}
