/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.ls;

/**
 *
 * LSP Telemetry support API
 */
public interface TelemetrySupport {

	/**
	 * Send telemetry to the client
	 * @param object the telemetry data to send
	 */
	public void telemetryEvent (Object object);

}
