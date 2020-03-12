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
package com.redhat.microprofile.jdt.internal.core.java.codelens;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.lsp4j.CodeLens;

import com.redhat.microprofile.jdt.core.java.codelens.IJavaCodeLensParticipant;
import com.redhat.microprofile.jdt.core.java.codelens.JavaCodeLensContext;
import com.redhat.microprofile.jdt.internal.core.java.AbstractJavaFeatureDefinition;

/**
 * Wrapper class around java participants {@link IJavaCodeLensParticipant}.
 */
public class JavaCodeLensDefinition extends AbstractJavaFeatureDefinition<IJavaCodeLensParticipant>
		implements IJavaCodeLensParticipant {
	private static final Logger LOGGER = Logger.getLogger(JavaCodeLensDefinition.class.getName());

	public JavaCodeLensDefinition(IConfigurationElement element) {
		super(element);
	}

	// -------------- CodeLens

	@Override
	public boolean isAdaptedForCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) {
		try {
			return getParticipant().isAdaptedForCodeLens(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling isAdaptedForCodeLens", e);
			return false;
		}
	}

	@Override
	public void beginCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) {
		try {
			getParticipant().beginCodeLens(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling beginCodeLens", e);
		}
	}

	@Override
	public List<CodeLens> collectCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) {
		try {
			return getParticipant().collectCodeLens(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while collecting codeLens", e);
			return null;
		}
	}

	@Override
	public void endCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) {
		try {
			getParticipant().endCodeLens(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling endCodeLens", e);
		}
	}

}
