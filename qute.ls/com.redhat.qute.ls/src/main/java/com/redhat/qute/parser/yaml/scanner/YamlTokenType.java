package com.redhat.qute.parser.yaml.scanner;

/**
 * YAML token types.
 */
public enum YamlTokenType {

	// Content tokens
    Content,
    Whitespace,
    Newline,
    
    // Key-Value tokens
    Key,
    Colon,
    Value,
    
    // Scalar value types
    ScalarString,
    ScalarNumber,
    ScalarBoolean,
    ScalarNull,
    
    // String tokens
    StartString,
    String,
    EndString,
    
    // List tokens
    Dash,
    
    // Flow collection tokens (JSON-style)
    ArrayOpen,      // [
    ArrayClose,     // ]
    ObjectOpen,     // {
    ObjectClose,    // }
    Comma,
    
    // Comment tokens
    StartComment,
    Comment,
    EndComment,
    
    // Other
    Unknown,
    EOS;
}
