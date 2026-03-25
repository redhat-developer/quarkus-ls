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

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.parser.expression.Part;

/**
 * Participant for providing hover documentation in Qute templates.
 * 
 * @author Angelo ZERR
 */
public interface HoverParticipant extends BaseParticpant {

	/**
	 * Provides hover documentation for the given part.
	 * 
	 * <p>
	 * Called when the user hovers over a template expression part. The part is
	 * always non-null (user hovered on something specific).
	 * </p>
	 * 
	 * @param part          the expression part being hovered
	 * @param hovers        accumulator for hover information (modified in-place)
	 * @param cancelChecker used to check if the operation was cancelled
	 */
	void doHover(Part part, List<Hover> hovers, CancelChecker cancelChecker);

}