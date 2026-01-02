/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions;

import java.util.List;

import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.parser.template.Expression;

/**
 * Participant for providing inlay hints in Qute templates.
 * 
 * <p>
 * Inlay hints are annotations displayed inline in the editor to show additional
 * information (e.g., parameter names, type information, resolved values).
 * </p>
 * 
 * @author Angelo ZERR
 */
public interface InlayHintParticipant {

	/**
	 * Checks if this participant is enabled.
	 * 
	 * @return true if this participant should provide inlay hints
	 */
	boolean isEnabled();

	/**
	 * Provides inlay hints for the given expression.
	 * 
	 * <p>
	 * Called for each expression in the template. Use the complete expression to
	 * determine where to place hints (typically at the end of the expression).
	 * </p>
	 * 
	 * @param node          the expression node to provide hints for
	 * @param inlayHints    accumulator for inlay hints (modified in-place)
	 * @param cancelChecker used to check if the operation was cancelled
	 */
	void inlayHint(Expression node, List<InlayHint> inlayHints, CancelChecker cancelChecker);

}