package com.naebom.stroke.naebom.dto;

import lombok.Getter;

@Getter
public class SpeechEvaluationResponseDto {
    private final double score;

    public SpeechEvaluationResponseDto(double score) {
        this.score = score;
    }
}
