package com.example.smartberry;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Simple local authentication manager.
 * Stores users in SharedPreferences as JSON: {salt:"...",hash:"...",name:"...",phone:"..."}
 * Passwords are stored as SHA‑256(salt+password) with a 16‑byte random salt.
 * Greenhouse data is stored under the key "greenhouse" as JSON {name:"...",location:"..."}.
 */
public class AuthManager {
    private static final String PREFS_NAME = "smartberry_prefs";
    private static final String USER_PREFIX = "user_"; // key = user_<email>
    private static final String GREENHOUSE_KEY = "greenhouse";

    private static SharedPreferences getPrefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            md.update(password.getBytes());
            byte[] digest = md.digest();
            return Base64.encodeToString(digest, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers a user. Returns true on success, false if email already exists or parameters invalid.
     */
    public static boolean registerUser(Context ctx, String email, String password, String name, String phone) {
        if (email == null || password == null || email.trim().isEmpty() || password.isEmpty()) {
            return false;
        }
        String normalizedEmail = email.trim().toLowerCase();
        SharedPreferences prefs = getPrefs(ctx);
        if (prefs.contains(USER_PREFIX + normalizedEmail)) {
            return false; // duplicate
        }
        byte[] salt = generateSalt();
        String hash = hashPassword(password, salt);
        String saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP);
        JSONObject obj = new JSONObject();
        try {
            obj.put("salt", saltB64);
            obj.put("hash", hash);
            obj.put("name", name == null ? JSONObject.NULL : name);
            obj.put("phone", phone == null ? JSONObject.NULL : phone);
        } catch (JSONException e) {
            return false;
        }
        prefs.edit().putString(USER_PREFIX + normalizedEmail, obj.toString()).apply();
        return true;
    }

    /**
     * Attempts login. Returns true if credentials match.
     */
    public static boolean login(Context ctx, String email, String password) {
        if (email == null || password == null) return false;
        String normalizedEmail = email.trim().toLowerCase();
        SharedPreferences prefs = getPrefs(ctx);
        String stored = prefs.getString(USER_PREFIX + normalizedEmail, null);
        if (stored == null) return false;
        try {
            JSONObject obj = new JSONObject(stored);
            String saltB64 = obj.getString("salt");
            String expectedHash = obj.getString("hash");
            byte[] salt = Base64.decode(saltB64, Base64.DEFAULT);
            String computedHash = hashPassword(password, salt);
            return expectedHash.equals(computedHash);
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * Stores greenhouse data. Returns true on success.
     */
    public static boolean registerGreenhouse(Context ctx, String name, String type, String area, String location) {
        if (name == null || type == null || area == null || location == null) {
            return false;
        }
        if (name.trim().isEmpty() || type.trim().isEmpty() || area.trim().isEmpty() || location.trim().isEmpty()) {
            return false;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("name", name.trim());
            obj.put("type", type.trim());
            obj.put("area", area.trim());
            obj.put("location", location.trim());
        } catch (JSONException e) {
            return false;
        }
        SharedPreferences prefs = getPrefs(ctx);
        prefs.edit().putString(GREENHOUSE_KEY, obj.toString()).apply();
        return true;
    }

    /**
     * Retrieves stored greenhouse data (may be null if not set).
     */
    public static JSONObject getGreenhouse(Context ctx) {
        SharedPreferences prefs = getPrefs(ctx);
        String stored = prefs.getString(GREENHOUSE_KEY, null);
        if (stored == null) return null;
        try {
            return new JSONObject(stored);
        } catch (JSONException e) {
            return null;
        }
    }
}
