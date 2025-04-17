package com.naebom.stroke.naebom.dto;

public class SettingDto {

    private Long memberId;
    private String language;
    private boolean screenMode;
    private boolean notificationsEnabled;
    private String fontSize;
    private String profileImage;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public boolean getScreenMode() { return screenMode; }
    public void setScreenMode(boolean screenMode) { this.screenMode = screenMode; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public String getFontSize() { return fontSize; }
    public void setFontSize(String fontSize) { this.fontSize = fontSize; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}
