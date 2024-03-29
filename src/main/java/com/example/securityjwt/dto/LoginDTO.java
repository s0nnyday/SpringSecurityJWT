package com.example.securityjwt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
    private String username;
    private String password;
}
