/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.validator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.ls.commons.CodeActionFactory;

import io.quarkus.qute.ErrorCode;
import io.quarkus.qute.ParserError;
import io.quarkus.qute.TemplateException;

public enum QuteSyntaxErrorCode implements IQuteErrorCode {

	/**
	 * <code>{# foo=1 /}</code>
	 */
	NO_SECTION_NAME("Parser error: no section name declared for {0}", ParserError.NO_SECTION_NAME),

	/**
	 * <code>{#if test}Hello {name}!{/for}</code>
	 */
	SECTION_END_DOES_NOT_MATCH_START("Parser error: section end tag [{0}] does not match the start tag [{1}]",
			ParserError.SECTION_END_DOES_NOT_MATCH_START),

	/**
	 * <code>{#if test}Hello{#else}Hi{/elsa}{/if}</code>
	 */
	SECTION_BLOCK_END_DOES_NOT_MATCH_START("Parser error: section block end tag [{0}] does not match the start tag [{1}]", ParserError.SECTION_BLOCK_END_DOES_NOT_MATCH_START),
	
	 /**
     * <code>{#if true}Bye...{/if} Hello {/if}</code>
     */
    SECTION_START_NOT_FOUND("Parser error: section start tag not found for {0}", ParserError.SECTION_START_NOT_FOUND),

	/**
	 * <code>{#if test}Hello {name}</code>
	 */
	UNTERMINATED_SECTION("Parser error: unterminated section [{0}] detected", ParserError.UNTERMINATED_SECTION),

	UNEXPECTED_TOKEN("Syntax error: `Unexpected ''{0}'' token`.", null);
	
	private static List<ErrorCode> supportedErrorCodes;

	private final String rawMessage;

	private final ErrorCode override;

	QuteSyntaxErrorCode(String rawMessage, ErrorCode override) {
		this.rawMessage = rawMessage;
		this.override = override;
	}

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public String toString() {
		return getCode();
	}

	@Override
	public String getRawMessage() {
		return rawMessage;
	}

	public boolean isQuteErrorCode(Either<String, Integer> code) {
		return CodeActionFactory.isDiagnosticCode(code, name());
	}

	public static QuteSyntaxErrorCode getErrorCode(Either<String, Integer> diagnosticCode) {
		if (diagnosticCode == null || diagnosticCode.isRight()) {
			return null;
		}
		String code = diagnosticCode.getLeft();
		try {
			return valueOf(code);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isSupported(TemplateException e) {
		ErrorCode errorCode = e.getCode();
		if (supportedErrorCodes == null) {
			supportedErrorCodes = new ArrayList<>();
			supportedErrorCodes.add(ParserError.NO_SECTION_HELPER_FOUND); // supported with
																			// QuteErrorCode.UndefinedSectionTag.
			QuteSyntaxErrorCode[] codes = values();
			for (QuteSyntaxErrorCode code : codes) {
				if (code.override != null) {
					supportedErrorCodes.add(code.override);
				}
			}
		}
		return supportedErrorCodes.contains(errorCode);
	}
}
