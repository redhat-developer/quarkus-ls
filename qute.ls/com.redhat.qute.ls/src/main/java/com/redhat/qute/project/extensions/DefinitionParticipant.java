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

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.parser.expression.Part;

/**
 * Participant for providing "go to definition" navigation in Qute templates.
 * 
 * @author Angelo ZERR
 */
public interface DefinitionParticipant {

	/**
	 * Checks if this participant is enabled.
	 * 
	 * @return true if this participant should provide definition links
	 */
	boolean isEnabled();

	/**
	 * Provides definition locations for the given part.
	 * 
	 * <p>
	 * Called when the user requests "go to definition" on a template expression
	 * part. The part is always non-null (user clicked on something specific).
	 * </p>
	 * 
	 * @param part          the expression part where definition was requested
	 * @param locationLinks accumulator for location links (modified in-place)
	 * @param cancelChecker used to check if the operation was cancelled
	 */
	void definition(Part part, List<LocationLink> locationLinks, CancelChecker cancelChecker);

}