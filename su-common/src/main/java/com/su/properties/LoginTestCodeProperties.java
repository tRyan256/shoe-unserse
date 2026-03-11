package com.su.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "su.login-test")
public class LoginTestCodeProperties {

    /**
     * Enable fixed test login code in local/dev environments.
     */
    private boolean enabled = false;

    /**
     * The bound phone number for fixed test code login.
     */
    private String phone;

    /**
     * Fixed test login code.
     */
    private String code;
}
