package com.company.socialanalytics.realtime;

public enum RealtimeChannel {
    DASHBOARD_UPDATES("dashboard-updates"),
    NEW_POST("new-post"),
    NEW_ALERT("new-alert"),
    ANALYTICS_UPDATE("analytics-update"),
    SYSTEM_HEALTH("system-health"),
    NOTIFICATIONS("notifications");

    private final String value;

    RealtimeChannel(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
