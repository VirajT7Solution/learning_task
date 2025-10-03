package com.task1.auth.request;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthRequest {
    private String username;
    private String password;
}


