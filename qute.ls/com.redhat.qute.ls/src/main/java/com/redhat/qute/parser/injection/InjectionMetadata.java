package com.redhat.qute.parser.injection;
public class InjectionMetadata {
    private final String languageId;
    private final InjectionMode mode;
    
    public InjectionMetadata(String languageId, InjectionMode mode) {
        this.languageId = languageId;
        this.mode = mode;
    }
    
    public String getLanguageId() {
        return languageId;
    }
    
    public InjectionMode getMode() {
        return mode;
    }
    
    /**
     * Returns true if this is an embedded injection (no Qute parsing)
     */
    public boolean isEmbedded() {
        return mode == InjectionMode.EMBEDDED;
    }
    
    /**
     * Returns true if this is a templated injection (Qute parsing allowed)
     */
    public boolean isTemplated() {
        return mode == InjectionMode.TEMPLATED;
    }
}