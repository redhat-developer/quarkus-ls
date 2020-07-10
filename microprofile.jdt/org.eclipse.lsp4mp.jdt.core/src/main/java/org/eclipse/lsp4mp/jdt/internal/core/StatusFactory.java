/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;

public final class StatusFactory {

	public static final IStatus UNSUPPORTED_PROJECT = newErrorStatus("Unsupported Java project");

	private StatusFactory() {
	}

	public static IStatus newErrorStatus(String message) {
		return newErrorStatus(message, null);
	}

	public static IStatus newErrorStatus(String message, Throwable exception) {
		return new Status(IStatus.ERROR, MicroProfileCorePlugin.PLUGIN_ID, message, exception);
	}
}
