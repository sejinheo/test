package com.naebom.stroke.naebom.repository;

import com.naebom.stroke.naebom.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    // 추가적인 커스텀 쿼리를 정의할 수 있습니다.
    // Optional<Setting> findByMemberId(Long memberId);
}
