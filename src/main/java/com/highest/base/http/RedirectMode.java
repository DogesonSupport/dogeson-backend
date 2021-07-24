package com.highest.base.http;

public enum RedirectMode {
    FOLLOW_ALL, // Fetcher will try to follow all redirects
    FOLLOW_TEMP, // Temp redirects are automatically followed, but not
    // permanent.
    FOLLOW_NONE // No redirects are followed.
}