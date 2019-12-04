package org.acme.config;

import java.util.List;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "greetingPublicFields")
public class GreetingPublicFieldsConfiguration {
    public String message;
    public HiddenConfig hidden;

    public static class HiddenConfig {
        public List<String> recipients;
    }
}