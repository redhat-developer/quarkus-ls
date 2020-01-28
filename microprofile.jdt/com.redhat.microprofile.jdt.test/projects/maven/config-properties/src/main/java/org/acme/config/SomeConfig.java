package org.acme.config;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties
public class SomeConfig {
    public String fooBar;
}