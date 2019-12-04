/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.ls.api;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.services.LanguageServer;

import com.redhat.microprofile.commons.MicroProfilePropertiesChangeEvent;

/**
 * MicroProfile language server API.
 * 
 * @author Angelo ZERR
 *
 */
public interface MicroProfileLanguageServerAPI extends LanguageServer {

	/**
	 * Notification for MicroProfile properties changed which occurs when:
	 * 
	 * <ul>
	 * <li>classpath (java sources and dependencies) changed</li>
	 * <li>only java sources changed</li>
	 * </ul>
	 * 
	 * @param event the MicroProfile properties change event which gives the
	 *              information if changed comes from classpath or java sources.
	 */
	@JsonNotification("microprofile/propertiesChanged")
	void propertiesChanged(MicroProfilePropertiesChangeEvent event);

}
