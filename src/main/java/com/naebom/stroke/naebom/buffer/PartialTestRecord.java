package com.naebom.stroke.naebom.buffer;

import lombok.Data;

@Data
public class PartialTestRecord {
    private Double faceTestScore;
    private Double speechTestScore;
    private Double fingerTestScore;
    private Double armTestScore;
    private String feedback;

    public boolean isComplete() {
        return faceTestScore != null &&
                speechTestScore != null &&
                fingerTestScore != null &&
                armTestScore != null &&
                feedback != null;
    }
}

