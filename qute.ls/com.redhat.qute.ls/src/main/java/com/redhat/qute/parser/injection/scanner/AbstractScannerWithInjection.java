package com.redhat.qute.parser.injection.scanner;

import java.util.Collection;

import com.redhat.qute.parser.injection.InjectionDetector;
import com.redhat.qute.parser.injection.InjectionMetadata;
import com.redhat.qute.parser.scanner.AbstractScanner;

public abstract class AbstractScannerWithInjection<T, S> extends AbstractScanner<T, S>
		implements ScannerWithInjection<T, S> {

	private final T languageInjectionStartToken;
	private final T languageInjectionContentToken;
	private final T languageInjectionEndToken;

	private final Collection<InjectionDetector> injectionDetectors;

	// Injection
	private InjectionMetadata currentInjectionMetadata;

	// State for managing injections
	private boolean inInjection = false;
	private int injectionContentStart = -1;
	private int injectionContentEnd = -1;

	protected AbstractScannerWithInjection(String input, int initialOffset, S initialState, T unknownTokenType,
			T eosTokenType, T languageInjectionStartToken, T languageInjectionContentToken, T languageInjectionEndToken,
			Collection<InjectionDetector> injectionDetectors) {
		super(input, initialOffset, initialState, unknownTokenType, eosTokenType);
		this.languageInjectionStartToken = languageInjectionStartToken;
		this.languageInjectionContentToken = languageInjectionContentToken;
		this.languageInjectionEndToken = languageInjectionEndToken;
		this.injectionDetectors = injectionDetectors;
	}

	protected AbstractScannerWithInjection(String input, int initialOffset, int endOffset, S initialState,
			T unknownTokenType, T eosTokenType, T languageInjectionStartToken, T languageInjectionContentToken,
			T languageInjectionEndToken, Collection<InjectionDetector> injectionDetectors) {
		super(input, initialOffset, endOffset, initialState, unknownTokenType, eosTokenType);
		this.languageInjectionStartToken = languageInjectionStartToken;
		this.languageInjectionContentToken = languageInjectionContentToken;
		this.languageInjectionEndToken = languageInjectionEndToken;
		this.injectionDetectors = injectionDetectors;
	}

	@Override
	protected final T internalScan() {
		int offset = stream.pos();
		if (stream.eos()) {
			return finishToken(offset, eosTokenType);
		}
		if (!injectionDetectors.isEmpty()) {
			// Check if we should detect an injection
			if (!inInjection) {
				InjectionMetadata injection = detectInjectionAtCurrentPosition();
				if (injection != null) {
					return scanInjection(offset, injection);
				}
			}

			// If we're in an injection, continue scanning it
			if (inInjection) {
				return scanInjectionContent(offset);
			}
		}

		// Normal scanning
		return scanNormal();
	}

	/**
	 * Detect if an injection starts at the current position
	 */
	private InjectionMetadata detectInjectionAtCurrentPosition() {
		for (InjectionDetector detector : injectionDetectors) {
			// Save the current position
			int savedPos = stream.pos();

			InjectionMetadata metadata = detector.detectInjection(stream);

			// Restore the position (detectInjection should not modify the stream)
			stream.goBackTo(savedPos);

			if (metadata != null) {
				return metadata;
			}
		}

		return null;
	}

	/**
	 * Scan the start of an injection
	 * 
	 * @param offset
	 */
	private T scanInjection(int offset, InjectionMetadata metadata) {
		this.currentInjectionMetadata = metadata;

		// Find the appropriate detector
		InjectionDetector detector = findDetectorForMetadata(metadata);
		if (detector == null) {
			return eosTokenType;
		}

		// Scan the start delimiter (e.g., ---)
		if (!detector.scanStartDelimiter(stream)) {
			return eosTokenType;
		}

		// Now we're at the beginning of the content
		this.injectionContentStart = stream.pos();

		// Find the end of the injection
		int savedPos = stream.pos();
		boolean hasEnd = detector.scanToInjectionEnd(stream);
		this.injectionContentEnd = stream.pos();

		// Restore the position to the beginning of the content
		stream.goBackTo(savedPos);

		if (!hasEnd) {
			// No end found, take until EOF
			this.injectionContentEnd = stream.getSource().length();
		}

		this.inInjection = true;
		return finishToken(offset, languageInjectionStartToken);
	}

	/**
	 * Scan the content of the injection
	 * 
	 * @param offset
	 */
	private T scanInjectionContent(int offset) {
		if (stream.pos() < injectionContentEnd) {
			// Return all the content at once
			stream.goBackTo(injectionContentEnd);
			return finishToken(offset, languageInjectionContentToken);
		} else {
			// We've reached the end, scan the end delimiter
			InjectionDetector detector = findDetectorForMetadata(currentInjectionMetadata);
			if (detector != null) {
				detector.scanEndDelimiter(stream);
			}

			this.inInjection = false;
			this.currentInjectionMetadata = null;
			return finishToken(offset, languageInjectionEndToken);
		}
	}

	/**
	 * Find the detector corresponding to the metadata
	 */
	private InjectionDetector findDetectorForMetadata(InjectionMetadata metadata) {
		if (metadata == null) {
			return null;
		}

		for (InjectionDetector detector : injectionDetectors) {
			int savedPos = stream.pos();
			InjectionMetadata detectorMetadata = detector.detectInjection(stream);
			stream.goBackTo(savedPos);

			if (detectorMetadata != null && detectorMetadata.getLanguageId().equals(metadata.getLanguageId())) {
				return detector;
			}
		}

		return null;
	}

	/**
	 * Normal scanning (existing code)
	 */
	protected abstract T scanNormal();

	@Override
	public InjectionMetadata getInjectionMetadata() {
		return currentInjectionMetadata;
	}
}
