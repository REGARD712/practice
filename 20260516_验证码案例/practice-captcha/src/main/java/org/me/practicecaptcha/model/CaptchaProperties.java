package org.me.practicecaptcha.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "captcha")
@Configuration
@Data
public class CaptchaProperties {
    private Integer width;
    private Integer height;
    private Session session;
    @Data
    public static class Session {
        private String code;
        private String date;
    }
}
