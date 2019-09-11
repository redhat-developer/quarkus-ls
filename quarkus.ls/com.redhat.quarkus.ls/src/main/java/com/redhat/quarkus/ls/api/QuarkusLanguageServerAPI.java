/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.ls.api;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.services.LanguageServer;

import com.redhat.quarkus.commons.QuarkusPropertiesChangeEvent;

/**
 * Quarkus language server API.
 * 
 * @author Angelo ZERR
 *
 */
public interface QuarkusLanguageServerAPI extends LanguageServer {

	/**
	 * Notification for quarkus properties changed which occurs when:
	 * 
	 * <ul>
	 * <li>classpath changed</li>
	 * <li>java sources changed</li>
	 * </ul>
	 * 
	 * @param event the quarkus properties change event which gives the information
	 *              if changed comes from classpath or java sources.
	 */
	@JsonNotification("quarkus/quarkusPropertiesChanged")
	void quarkusPropertiesChanged(QuarkusPropertiesChangeEvent event);
}
