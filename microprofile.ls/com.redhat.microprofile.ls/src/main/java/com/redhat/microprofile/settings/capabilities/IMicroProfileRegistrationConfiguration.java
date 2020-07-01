/**
 *  Copyright (c) 2019-2020 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package com.redhat.microprofile.settings.capabilities;

import org.eclipse.lsp4j.Registration;

/**
 * LSP Registration configuration API.
 *
 */
public interface IMicroProfileRegistrationConfiguration {

	/**
	 * Configure the given LSP registration.
	 * 
	 * @param registration the registration to configure.
	 */
	public void configure(Registration registration);
}
