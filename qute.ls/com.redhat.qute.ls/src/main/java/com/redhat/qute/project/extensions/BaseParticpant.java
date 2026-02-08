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

public interface BaseParticpant {

	/**
	 * Checks if this participant is enabled.
	 * 
	 * @return true if this participant should provide completions
	 */
	boolean isEnabled();
}
