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

import java.util.Set;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;

/**
 * Participant for providing custom completion items in Qute templates.
 * 
 * @author Angelo ZERR
 */
public interface CompletionParticipant {

	/**
	 * Checks if this participant is enabled.
	 * 
	 * @return true if this participant should provide completions
	 */
	boolean isEnabled();

	/**
	 * Provides completion items for the given template expression.
	 * 
	 * <p>
	 * Both {@code part} and {@code parts} may be provided depending on context:
	 * <ul>
	 * <li><code>{|}</code> → part is null, parts contains empty expression</li>
	 * <li><code>{m:L|ogin}</code> → part is "L", parts contains "m:Login"</li>
	 * </ul>
	 * </p>
	 * 
	 * @param completionRequest  the completion request context
	 * @param part               the current part being edited (may be null)
	 * @param parts              the complete parts of the expression
	 * @param completionSettings completion preferences
	 * @param formattingSettings formatting preferences
	 * @param completionItems    accumulator for completion items (modified
	 *                           in-place)
	 * @param cancelChecker      the cancel checker.
	 */
	void doComplete(CompletionRequest completionRequest, Part part, Parts parts,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			Set<CompletionItem> completionItems, CancelChecker cancelChecker);

}