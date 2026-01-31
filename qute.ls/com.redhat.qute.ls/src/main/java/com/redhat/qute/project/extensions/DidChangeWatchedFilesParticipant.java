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

import java.nio.file.Path;
import java.util.Set;

import org.eclipse.lsp4j.FileChangeType;

/**
 * Participant for handling file system changes relevant to the extension.
 * 
 * @author Angelo ZERR
 */
public interface DidChangeWatchedFilesParticipant extends BaseParticpant {

	/**
	 * Handles a file system change event.
	 * 
	 * <p>
	 * Called when a watched file is created, modified, or deleted. Use this to
	 * reload resources, update caches, or trigger re-validation.
	 * </p>
	 * 
	 * @param filePath    the path of the changed file
	 * @param changeTypes the file change event (type: Created, Changed, or Deleted)
	 * @return true if this participant handled the file change, false otherwise
	 */
	boolean didChangeWatchedFile(Path filePath, Set<FileChangeType> changeTypes);

}