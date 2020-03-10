/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.services;

import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.microprofile.ls.commons.CodeActionFactory;

/**
 * MicroProfile validation types.
 * 
 * @author Angelo ZERR
 *
 */
public enum ValidationType {

	syntax, unknown, duplicate, value, required, requiredValue;

	/**
	 * Returns true if the given code matches the validation type and false
	 * otherwise.
	 * 
	 * @param code the diagnostic code.
	 * @return true if the given code matches the validation type and false
	 *         otherwise.
	 */
	public boolean isValidationType(Either<String, Number> code) {
		return CodeActionFactory.isDiagnosticCode(code, name());
	}

}
