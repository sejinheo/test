package com.naebom.stroke.naebom.entity;

import jakarta.persistence.*;

@Entity
public class Setting {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private boolean screenMode;

    @Column(nullable = false)
    private boolean notificationsEnabled;

    @Column(nullable = false)
    private String fontSize; // small, medium, large

    @Column(columnDefinition = "LONGTEXT")
    private String profileImage;

    public Setting() {}

    public Setting(Long memberId, String language, boolean screenMode, boolean notificationsEnabled, String fontSize, String profileImage) {
        this.memberId = memberId;
        this.language = language;
        this.screenMode = screenMode;
        this.notificationsEnabled = notificationsEnabled;
        this.fontSize = fontSize;
        this.profileImage = profileImage;
    }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public boolean isScreenMode() { return screenMode; }
    public void setScreenMode(boolean screenMode) { this.screenMode = screenMode; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public String getFontSize() { return fontSize; }
    public void setFontSize(String fontSize) { this.fontSize = fontSize; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}
