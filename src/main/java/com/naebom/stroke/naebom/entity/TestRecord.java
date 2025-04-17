package com.naebom.stroke.naebom.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "test_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate testDate;

    @Column(nullable = false)
    private Integer testCount;  // 추가됨

    @Column(columnDefinition = "TEXT")
    private String feedback;    // 추가됨

    @Column
    private Double avgRiskScore; // 추가됨

    private Double faceTestScore;
    private Double speechTestScore;
    private Double fingerTestScore;
    private Double armTestScore;

    @Column(nullable = false)
    private Boolean strokeRisk;
}
