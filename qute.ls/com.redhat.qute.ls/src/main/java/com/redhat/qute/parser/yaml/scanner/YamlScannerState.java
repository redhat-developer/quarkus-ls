package com.redhat.qute.parser.yaml.scanner;

/**
 * YAML scanner states.
 * Represents the current scanning context.
 */
public enum YamlScannerState {
	    WithinContent,
	    AfterKey,
	    AfterColon,
	    AfterDash,
	    WithinValue,
	    WithinString,
	    WithinComment,
	    WithinFlowSequence,    // Inside [...]
	    WithinFlowMapping;     // Inside {...}
}
