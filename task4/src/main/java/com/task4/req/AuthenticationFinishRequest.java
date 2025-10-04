package com.task4.req;

import lombok.Data;

@Data
public class AuthenticationFinishRequest {
    private String username;
    private String credential;
}
