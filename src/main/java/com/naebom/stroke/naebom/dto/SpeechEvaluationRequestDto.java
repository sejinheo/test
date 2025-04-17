package com.naebom.stroke.naebom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeechEvaluationRequestDto {
    @JsonProperty("memberId")
    private Long memberId;

    @JsonProperty("expectedText")
    private String expectedText;

    @JsonProperty("base64Audio")
    private String base64Audio;
}
