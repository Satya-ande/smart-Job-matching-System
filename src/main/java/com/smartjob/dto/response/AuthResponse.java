package com.smartjob.dto.response;

/**
 * DTO for authentication response (login/register).
 * Contains the JWT token and user role.
 */
public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private String role;
    private String name;
    private String email;

    public AuthResponse() {}

    public AuthResponse(String accessToken, long expiresIn, String role, String name, String email) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.role = role;
        this.name = name;
        this.email = email;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
