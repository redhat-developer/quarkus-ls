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
package com.redhat.qute.project.roq;

import java.util.Set;

import com.redhat.qute.commons.config.roq.RoqConfig;
import com.redhat.qute.project.MockProjectQuteLanguageServer;

public class RoqProjectQuteLanguageServer extends MockProjectQuteLanguageServer {

	public RoqProjectQuteLanguageServer() {
		super(RoqProject.PROJECT_URI, Set.of(RoqConfig.PROJECT_FEATURE));
	}

}
