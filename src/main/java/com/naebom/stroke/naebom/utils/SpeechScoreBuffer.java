package com.naebom.stroke.naebom.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SpeechScoreBuffer {

    private final Map<Long, List<Double>> buffer = new HashMap<>();

    public void addScore(Long memberId, double score) {
        buffer.computeIfAbsent(memberId, k -> new ArrayList<>()).add(score);
    }

    public List<Double> getScores(Long memberId) {
        return buffer.getOrDefault(memberId, new ArrayList<>());
    }

    public void clear(Long memberId) {
        buffer.remove(memberId);
    }

    public boolean isReady(Long memberId) {
        return buffer.containsKey(memberId) && buffer.get(memberId).size() == 3;
    }
}

