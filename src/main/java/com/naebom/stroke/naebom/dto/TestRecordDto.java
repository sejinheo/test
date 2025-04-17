package com.naebom.stroke.naebom.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRecordDto {
    private Long id;
    private Long memberId;
    private LocalDate testDate;
    private Double faceTestScore;
    private Double speechTestScore;
    private Double fingerTestScore;
    private Double armTestScore;
    private Boolean strokeRisk;
    private Integer testCount;
    private String feedback;
    private Double avgRiskScore;
    private Double prevAvgRiskScore;

}
