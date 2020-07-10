/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.core.java;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Wrapper class around a java feature participant.
 */
public abstract class AbstractJavaFeatureDefinition<T> {

	private static final String CLASS_ATTR = "class";

	private final IConfigurationElement element;

	private T participant;

	public AbstractJavaFeatureDefinition(IConfigurationElement element) {
		this.element = element;
	}

	protected T getParticipant() throws CoreException {
		if (participant == null) {
			participant = (T) element.createExecutableExtension(CLASS_ATTR);
		}
		return participant;
	}

}
