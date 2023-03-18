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

import org.eclipse.lsp4j.Range;

import com.redhat.qute.parser.template.Node;

/**
 * Syntax validator reporter.
 * 
 * @author Angelo ZERR
 *
 */
public interface IQuteSyntaxValidatorReporter {

	/**
	 * Report a syntax error.
	 * 
	 * @param errorRange the error range (start/end position) where the error
	 *                   occurs.
	 * @param node       the node which have a problem.
	 * @param errorCode  the error code.
	 * @param arguments  the arguments used to generate a proper message.
	 */
	void reportError(Range errorRange, Node node, IQuteErrorCode errorCode, Object... arguments);

}
