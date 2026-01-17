package com.canvify.test.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GuestCookieService {

    private static final String GUEST_COOKIE = "guest_id";

    public String getOrCreateGuestId(HttpServletRequest request, HttpServletResponse response) {

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (GUEST_COOKIE.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }

        String guestId = UUID.randomUUID().toString();
        Cookie cookie = new Cookie(GUEST_COOKIE, guestId);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true in prod (https)
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 200); // 200 days

        response.addCookie(cookie);

        return guestId;
    }

    public void clearGuestCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(GUEST_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
