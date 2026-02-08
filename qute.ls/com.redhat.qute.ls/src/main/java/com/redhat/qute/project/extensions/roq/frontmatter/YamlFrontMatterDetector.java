package com.redhat.qute.project.extensions.roq.frontmatter;

import com.redhat.qute.parser.injection.InjectionDetector;
import com.redhat.qute.parser.injection.InjectionMetadata;
import com.redhat.qute.parser.injection.InjectionMode;
import com.redhat.qute.parser.scanner.MultiLineStream;

public class YamlFrontMatterDetector implements InjectionDetector {

	public static final String YAML_FRONT_MATTER_LANGUAGE_ID = "yaml-frontmatter";

	private static final int[] FRONT_MATTER_DELIMITER = new int[] { '-', '-', '-' };

	private static final InjectionMetadata YAML_METADATA = new InjectionMetadata(YAML_FRONT_MATTER_LANGUAGE_ID,
			InjectionMode.EMBEDDED);

	@Override
	public InjectionMetadata detectInjection(MultiLineStream stream) {
		// Must be at the beginning of the file
		if (stream.pos() != 0) {
			return null;
		}

		// Must start with ---
		if (stream.peekChar(0) == '-' && stream.peekChar(1) == '-' && stream.peekChar(2) == '-') {

			// Check that it's followed by a newline or EOF
			int charAfter = stream.peekChar(3);
			if (charAfter == -1 || charAfter == '\n' || charAfter == '\r') {
				return YAML_METADATA;
			}
		}

		return null;
	}

	@Override
	public boolean scanStartDelimiter(MultiLineStream stream) {
		// Scan ---
		if (stream.advanceIfChars(FRONT_MATTER_DELIMITER)) {
			// Scan the newline after ---
			if (stream.peekChar() == '\r') {
				stream.advance(1);
				if (stream.peekChar() == '\n') {
					stream.advance(1);
				}
			} else if (stream.peekChar() == '\n') {
				stream.advance(1);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean scanToInjectionEnd(MultiLineStream stream) {
		// Look for the pattern "\n---" or "\r\n---"
		while (!stream.eos()) {
			int ch = stream.peekChar();

			if (ch == '\n') {
				// Check if it's followed by ---
				if (stream.peekChar(1) == '-' && stream.peekChar(2) == '-' && stream.peekChar(3) == '-') {
					// Check that --- is followed by a newline or EOF
					int charAfter = stream.peekChar(4);
					if (charAfter == -1 || charAfter == '\n' || charAfter == '\r') {
						// Found! Advance PAST the \n to include it in content
						stream.advance(1);
						return true;
					}
				}
			} else if (ch == '\r') {
				// Check if it's \r\n followed by ---
				if (stream.peekChar(1) == '\n' && stream.peekChar(2) == '-' && stream.peekChar(3) == '-'
						&& stream.peekChar(4) == '-') {
					int charAfter = stream.peekChar(5);
					if (charAfter == -1 || charAfter == '\n' || charAfter == '\r') {
						// Found! Advance PAST the \r\n to include it in content
						stream.advance(2);
						return true;
					}
				}
			}

			stream.advance(1);
		}

		// No end found, we're at EOF
		return false;
	}

	@Override
	public boolean scanEndDelimiter(MultiLineStream stream) {
		// We're now positioned right at the start of ---
		// The \r\n before --- is already included in the content

		// Scan ---
		boolean success = stream.advanceIfChars(FRONT_MATTER_DELIMITER);
		if (success) {
			// Scan the newline after --- (optional)
			if (stream.peekChar() == '\r') {
				stream.advance(1);
				if (stream.peekChar() == '\n') {
					stream.advance(1);
				}
			} else if (stream.peekChar() == '\n') {
				stream.advance(1);
			}
			return true;
		}

		return false;
	}
}