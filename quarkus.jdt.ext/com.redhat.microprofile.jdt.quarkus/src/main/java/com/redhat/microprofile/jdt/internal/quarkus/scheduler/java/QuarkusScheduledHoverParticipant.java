/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.scheduler.java;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.lsp4mp.jdt.core.java.hover.PropertiesHoverParticipant;

import com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants;

public class QuarkusScheduledHoverParticipant extends PropertiesHoverParticipant {

	public QuarkusScheduledHoverParticipant() {
		super(QuarkusConstants.SCHEDULED_ANNOTATION,
				new String [] { "cron", "every", "delay", "delayed", "delayUnit"}, null);
	}

	@Override
	protected boolean isAdaptableFor(IJavaElement hoverElement) {
		return hoverElement.getElementType() == IJavaElement.METHOD;
	}
}
