package com.cs447.securefiletransfer.backend.Payload;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";

    public AuthResponse(String token) {
        this.token = token;
        this.tokenType = "Bearer";
    }
}