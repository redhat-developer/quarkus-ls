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

import com.redhat.qute.commons.usertags.UserTagInfo;
import com.redhat.qute.project.QuteProject;

/**
 * Binary user tag.
 * 
 * @author Angelo ZERR
 *
 */
public class BinaryUserTag extends UserTag {

	private final String uri;
	private final String content;

	public BinaryUserTag(UserTagInfo tagInfo, QuteProject project) {
		super(tagInfo.getFileName(), project);
		this.uri = tagInfo.getUri();
		this.content = tagInfo.getContent();
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public String getContent() {
		return content;
	}
}
