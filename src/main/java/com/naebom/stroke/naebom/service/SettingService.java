package com.naebom.stroke.naebom.service;

import com.naebom.stroke.naebom.dto.SettingDto;
import com.naebom.stroke.naebom.entity.Setting;
import com.naebom.stroke.naebom.repository.SettingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SettingService {

    private final SettingRepository settingRepository;

    public SettingService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    // 설정 정보 조회
    public Optional<Setting> getSettingByMemberId(Long memberId) {
        return settingRepository.findById(memberId);
    }

    // **DTO를 엔티티로 변환하여 저장**
    public Setting saveOrUpdateSetting(SettingDto settingDto) {
        Setting setting = settingRepository.findById(settingDto.getMemberId())
                .orElse(new Setting());

        // DTO → 엔티티 변환
        setting.setMemberId(settingDto.getMemberId());
        setting.setLanguage(settingDto.getLanguage());
        setting.setScreenMode(settingDto.getScreenMode());
        setting.setNotificationsEnabled(settingDto.isNotificationsEnabled());
        setting.setFontSize(settingDto.getFontSize());
        setting.setProfileImage(settingDto.getProfileImage()); // ✅ Base64 이미지 저장

        return settingRepository.save(setting);
    }
}
