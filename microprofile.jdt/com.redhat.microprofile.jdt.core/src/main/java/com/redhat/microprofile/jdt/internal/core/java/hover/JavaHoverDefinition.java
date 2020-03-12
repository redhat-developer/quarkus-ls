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
package com.redhat.microprofile.jdt.internal.core.java.hover;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.lsp4j.Hover;

import com.redhat.microprofile.jdt.core.java.hover.IJavaHoverParticipant;
import com.redhat.microprofile.jdt.core.java.hover.JavaHoverContext;
import com.redhat.microprofile.jdt.internal.core.java.AbstractJavaFeatureDefinition;

/**
 * Wrapper class around java participants {@link IJavaHoverParticipant}.
 */
public class JavaHoverDefinition extends AbstractJavaFeatureDefinition<IJavaHoverParticipant>
		implements IJavaHoverParticipant {
	private static final Logger LOGGER = Logger.getLogger(JavaHoverDefinition.class.getName());

	public JavaHoverDefinition(IConfigurationElement element) {
		super(element);
	}

	// -------------- Hover

	@Override
	public boolean isAdaptedForHover(JavaHoverContext context, IProgressMonitor monitor) {
		try {
			return getParticipant().isAdaptedForHover(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling isAdaptedForHover", e);
			return false;
		}
	}

	@Override
	public void beginHover(JavaHoverContext context, IProgressMonitor monitor) {
		try {
			getParticipant().beginHover(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling beginHover", e);
		}
	}

	@Override
	public Hover collectHover(JavaHoverContext context, IProgressMonitor monitor) {
		try {
			return getParticipant().collectHover(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while collecting hover", e);
			return null;
		}
	}

	@Override
	public void endHover(JavaHoverContext context, IProgressMonitor monitor) {
		try {
			getParticipant().endHover(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling endHover", e);
		}
	}

}
