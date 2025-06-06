package me.sathish.event_service.security;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationRequest {

    @NotNull @Size(max = 50)
    private String username;

    @NotNull @Size(max = 50)
    private String password;
}
