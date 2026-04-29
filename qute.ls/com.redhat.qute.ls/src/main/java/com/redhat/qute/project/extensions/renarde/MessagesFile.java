/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions.renarde;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import com.redhat.qute.project.extensions.config.PropertiesFile;

/**
 * Represents a messages.properties file with its parsed content.
 * 
 * <p>
 * Manages loading and reloading of Java properties files used for
 * internationalization (i18n) in Renarde applications.
 * </p>
 * 
 * @author Angelo ZERR
 */
public class MessagesFile extends PropertiesFile {

	/**
	 * Creates a new messages file info and loads its content.
	 * 
	 * @param messagesFile     the path to the messages.properties file
	 * @param messagesFileName
	 * @throws FileNotFoundException if the file does not exist
	 * @throws IOException           if an error occurs reading the file
	 */
	public MessagesFile(Path messagesFile, PropertiesFileName messagesFileName) throws IOException {
		super(messagesFile, messagesFileName);
	}

}