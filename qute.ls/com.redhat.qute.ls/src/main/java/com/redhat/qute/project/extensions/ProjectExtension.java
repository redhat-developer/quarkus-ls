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

import com.redhat.qute.project.datamodel.ExtendedDataModelProject;

/**
 * Extension point for adding custom functionality to the Qute language server.
 * 
 * <p>
 * Uses a composite participant pattern to allow extensions to participate in
 * completion, diagnostics, hover, definition navigation, inlay hints, and file
 * changes.
 * </p>
 * 
 * <p>
 * Implementations are discovered via {@link java.util.ServiceLoader} from:
 * <code>META-INF/services/com.redhat.qute.project.extensions.ProjectExtension</code>
 * </p>
 * 
 * <p>
 * <b>Lifecycle:</b> init() → isEnabled() → participant methods
 * </p>
 * 
 * @author Red Hat Inc.
 * @since 1.0
 */
public interface ProjectExtension extends CompletionParticipant, DefinitionParticipant, DiagnosticsParticipant,
		DidChangeWatchedFilesParticipant, HoverParticipant, InlayHintParticipant {

	/**
	 * Initializes this extension for the given project.
	 * 
	 * <p>
	 * Use this to check if the extension applies (e.g., namespace resolver exists),
	 * load resources, and perform lightweight setup.
	 * </p>
	 * 
	 * @param project the project containing metadata, source paths, and namespace
	 *                resolvers
	 */
	void init(ExtendedDataModelProject project);

	/**
	 * Checks if this extension is enabled for the current project.
	 * 
	 * <p>
	 * Called before each participant method. Return false to skip processing.
	 * </p>
	 * 
	 * @return true if this extension should participate in language server
	 *         operations
	 */
	boolean isEnabled();
}