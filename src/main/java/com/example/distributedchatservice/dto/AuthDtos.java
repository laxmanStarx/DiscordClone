package com.example.distributedchatservice.dto;

import java.util.UUID;

public class AuthDtos {


    public record RegisterRequest(String email, String username, String password){}

    public record LoginRequest(String email, String password){}

    public record AuthResponse(String token, UUID userId, String username){}
}
