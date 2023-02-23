/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * IO utilities class
 * 
 * @author Angelo ZERR
 *
 */
public class IOUtils {

	public static String getContent(Path path) throws IOException {
		try (InputStream input = new FileInputStream(path.toFile())) {
			return IOUtils.convertStreamToString(input);
		}
	}

	/**
	 * Convert the given {@link InputStream} into a String. The source InputStream
	 * will then be closed.
	 * 
	 * @param is the input stream
	 * @return the given input stream in a String.
	 */
	public static String convertStreamToString(InputStream is) {
		try (Scanner s = new java.util.Scanner(is)) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}
}