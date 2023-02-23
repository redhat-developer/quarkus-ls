/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.tags;

import java.nio.file.Path;

import com.redhat.qute.project.QuteProject;
import com.redhat.qute.utils.IOUtils;

/**
 * Source user tag.
 * 
 * @author Angelo ZERR
 *
 */
public class SourceUserTag extends UserTag {

	private final Path path;

	public SourceUserTag(String fileName, Path path, QuteProject project) {
		super(fileName, project);
		this.path = path;
	}

	@Override
	public String getUri() {
		return path.toUri().toString();
	}

	/**
	 * Returns the Qute template file path.
	 * 
	 * @return the Qute template file path.
	 */
	public Path getPath() {
		return path;
	}

	@Override
	public String getContent() {
		try  {
			return IOUtils.getContent(path);
		} catch (Exception e) {
			// Do nothing
		}
		return null;
	}
}
