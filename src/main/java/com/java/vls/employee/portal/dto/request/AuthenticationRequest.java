package com.java.vls.employee.portal.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class AuthenticationRequest {

    private String username;
    private String password;

}
