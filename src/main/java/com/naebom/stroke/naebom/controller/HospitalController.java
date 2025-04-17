package com.naebom.stroke.naebom.controller;

import com.naebom.stroke.naebom.service.HospitalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hospitals")
@CrossOrigin(origins = "*")  // CORS 문제 방지
public class HospitalController {
    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @PostMapping("/nearby")
    public List<Map<String, Object>> getNearbyHospitals(@RequestBody Map<String, Double> locationData) {
        if (locationData == null || !locationData.containsKey("latitude") || !locationData.containsKey("longitude")) {
            throw new IllegalArgumentException("latitude와 longitude 값이 필요합니다.");
        }

        double latitude = locationData.get("latitude");
        double longitude = locationData.get("longitude");

        System.out.println("사용자 위치 요청 받음: latitude=" + latitude + ", longitude=" + longitude);
        return hospitalService.getNearbyHospitals(latitude, longitude);
    }
}
