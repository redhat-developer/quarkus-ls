/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.ls.commons.client;

/**
 * Commonly used client commands
 * 
 * @author Angelo ZERR
 *
 */
public class CommandKind {

	private CommandKind() {
	}

	/**
	 * Client command to open URI
	 */
	public static final String COMMAND_OPEN_URI = "qute.command.open.uri";

	public static final String COMMAND_JAVA_DEFINITION = "qute.command.java.definition";

}