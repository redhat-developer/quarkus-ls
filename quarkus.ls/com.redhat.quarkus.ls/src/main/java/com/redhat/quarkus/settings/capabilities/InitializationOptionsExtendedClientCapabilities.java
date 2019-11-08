/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */

package com.redhat.quarkus.settings.capabilities;

import com.redhat.quarkus.ls.commons.client.ExtendedClientCapabilities;
import com.redhat.quarkus.utils.JSONUtility;

import org.eclipse.lsp4j.InitializeParams;


/**
 * Represents all extended client capabilities sent from the server
 * 
 * <pre>
 * "extendedClientCapabilities": {
		"commands": {
			"commandsKind": {
				"valueSet": [
					"quarkus.command.configuration.update"
				]
			}
		}
	}
 * </pre>
 */
public class InitializationOptionsExtendedClientCapabilities {

	private ExtendedClientCapabilities extendedClientCapabilities;

	public ExtendedClientCapabilities getExtendedClientCapabilities() {
		return extendedClientCapabilities;
	}

	public void setExtendedClientCapabilities(ExtendedClientCapabilities extendedClientCapabilities) {
		this.extendedClientCapabilities = extendedClientCapabilities;
	}

	/**
	 * Returns the "settings" section of
	 * {@link InitializeParams#getInitializationOptions()}.
	 * 
	 * Here a sample of initializationOptions
	 * 
	 * <pre>
	 * "extendedClientCapabilities": {
			"commands": {
				"commandsKind": {
					"valueSet": [
						"quarkus.command.configuration.update"
					]
				}
			}
		}
	 * </pre>
	 * 
	 * @param initializeParams
	 * @return the "extendedClientCapabilities" section of
	 *         {@link InitializeParams#getInitializationOptions()}.
	 */
	public static ExtendedClientCapabilities getExtendedClientCapabilities(InitializeParams initializeParams) {
		InitializationOptionsExtendedClientCapabilities root = JSONUtility.toModel(
				initializeParams.getInitializationOptions(), InitializationOptionsExtendedClientCapabilities.class);
		return root != null ? root.getExtendedClientCapabilities() : null;
	}
}
