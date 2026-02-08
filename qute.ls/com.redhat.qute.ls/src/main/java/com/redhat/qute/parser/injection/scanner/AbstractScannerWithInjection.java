package com.redhat.qute.parser.injection.scanner;

import java.util.Collection;

import com.redhat.qute.parser.injection.InjectionDetector;
import com.redhat.qute.parser.injection.InjectionMatch;
import com.redhat.qute.parser.injection.InjectionMetadata;
import com.redhat.qute.parser.scanner.AbstractScanner;

/**
 * Generic scanner extension that adds support for language injections.
 *
 * This class:
 * - delegates language-specific scanning to subclasses
 * - manages the lifecycle of an injection:
 *   START → CONTENT → END
 */
public abstract class AbstractScannerWithInjection<T, S>
		extends AbstractScanner<T, S>
		implements ScannerWithInjection<T, S> {

	// Token types used to represent injections in the token stream
	private final T languageInjectionStartToken;
	private final T languageInjectionContentToken;
	private final T languageInjectionEndToken;

	// All available injection detectors
	private final Collection<InjectionDetector> injectionDetectors;

	// Current injection context (null when not in an injection)
	private InjectionMetadata currentInjectionMetadata;
	private InjectionDetector currentDetector;

	// Injection state flags
	private boolean inInjection = false;

	// Cached bounds of the injected content
	// (computed once when the injection starts)
	private int injectionContentStart = -1;
	private int injectionContentEnd = -1;

	protected AbstractScannerWithInjection(
			String input,
			int initialOffset,
			S initialState,
			T unknownTokenType,
			T eosTokenType,
			T languageInjectionStartToken,
			T languageInjectionContentToken,
			T languageInjectionEndToken,
			Collection<InjectionDetector> injectionDetectors) {

		super(input, initialOffset, initialState, unknownTokenType, eosTokenType);
		this.languageInjectionStartToken = languageInjectionStartToken;
		this.languageInjectionContentToken = languageInjectionContentToken;
		this.languageInjectionEndToken = languageInjectionEndToken;
		this.injectionDetectors = injectionDetectors;
	}

	/**
	 * Main scanning entry point.
	 *
	 * The order of responsibility is important:
	 * 1. EOF
	 * 2. Injection handling
	 * 3. Normal language scanning
	 */
	@Override
	protected final T internalScan() {
		int offset = stream.pos();

		// End of stream → emit EOS token
		if (stream.eos()) {
			return finishToken(offset, eosTokenType);
		}

		// Injection logic is enabled only when the subclass allows it
		if (supportInjection() && !injectionDetectors.isEmpty()) {

			// 1) Try to detect a new injection
			// This happens ONLY when we are NOT already inside one
			if (!inInjection) {
				InjectionMatch match = detectInjectionAtCurrentPosition();
				if (match != null) {
					// Injection start is a semantic event
					return scanInjection(offset, match);
				}
			}

			// 2) If we are inside an injection, handle it before normal scanning
			if (inInjection) {
				return scanInjectionContent(offset);
			}
		}

		// 3) Fallback to normal language scanning
		return scanNormal();
	}

	/**
	 * Attempts to detect an injection at the current stream position.
	 *
	 * Important design rule:
	 * - Detection MUST NOT consume the stream.
	 * - If an injection is detected, we return both:
	 *   - the detector
	 *   - the metadata
	 */
	private InjectionMatch detectInjectionAtCurrentPosition() {
		for (InjectionDetector detector : injectionDetectors) {
			int savedPos = stream.pos();

			InjectionMetadata metadata = detector.detectInjection(stream);

			// Restore stream position after detection
			stream.goBackTo(savedPos);

			if (metadata != null) {
				return new InjectionMatch(detector, metadata);
			}
		}
		return null;
	}

	/**
	 * Handles the START of an injection.
	 *
	 * This method:
	 * - fixes the detector for the whole injection lifecycle
	 * - computes the injection content boundaries ONCE
	 */
	private T scanInjection(int offset, InjectionMatch match) {

		// Store injection context
		this.currentInjectionMetadata = match.getMetadata();
		this.currentDetector = match.getDetector();

		// Consume the injection start delimiter
		if (!currentDetector.scanStartDelimiter(stream)) {
			// Defensive fallback
			return eosTokenType;
		}

		// Content starts immediately after the start delimiter
		this.injectionContentStart = stream.pos();

		// Compute the end of the injection only once
		int savedPos = stream.pos();
		boolean hasEnd = currentDetector.scanToInjectionEnd(stream);
		this.injectionContentEnd = stream.pos();

		// Restore position to the start of content
		stream.goBackTo(savedPos);

		// If no end delimiter was found, consume until EOF
		if (!hasEnd) {
			this.injectionContentEnd = stream.getSource().length();
		}

		// Enter injection mode
		this.inInjection = true;

		return finishToken(offset, languageInjectionStartToken);
	}

	/**
	 * Handles either:
	 * - the injected content
	 * - or the injection end delimiter
	 */
	private T scanInjectionContent(int offset) {

		// If we are still inside the injected content
		if (stream.pos() < injectionContentEnd) {

			// Consume the whole content in one token
			stream.goBackTo(injectionContentEnd);
			return finishToken(offset, languageInjectionContentToken);
		}

		// We reached the end delimiter
		int endDelimiterStart = stream.pos();

		if (currentDetector != null) {
			currentDetector.scanEndDelimiter(stream);
		}

		// Reset injection state
		this.inInjection = false;
		this.currentInjectionMetadata = null;
		this.currentDetector = null;

		return finishToken(endDelimiterStart, languageInjectionEndToken);
	}

	/**
	 * Exposes metadata of the current injection (if any).
	 * Used by parsers / IDE features.
	 */
	@Override
	public InjectionMetadata getInjectionMetadata() {
		return currentInjectionMetadata;
	}

	/**
	 * Language-specific scanning (implemented by subclasses).
	 */
	protected abstract T scanNormal();

	/**
	 * Allows subclasses to enable or disable injection support
	 * depending on their current state.
	 */
	protected abstract boolean supportInjection();
}
