/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package com.redhat.quarkus.utils;

import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.ls.commons.TextDocument;

public class ApplicationPropertiesDocumentUtils {

	/**
	 * Returns the number of total lines in <code>document</code>
	 * @param document the document of interest
	 * @return the number of total lines in <code>document</code>
	 */
	public static int getTotalLines(TextDocument document) throws BadLocationException{
		int length = document.getText().length();
		int lastLineIndex = document.positionAt(length).getLine();
		return lastLineIndex + 1;
	}
}