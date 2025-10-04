package com.task4.req;

import lombok.Data;

@Data
public class RegistrationFinishRequest {
    private String username;
    private String email;
    private String credential;
    private String deviceName;
}
