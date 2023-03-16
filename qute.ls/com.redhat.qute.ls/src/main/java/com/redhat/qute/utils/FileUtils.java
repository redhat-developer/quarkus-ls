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

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

/**
 * File utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class FileUtils {

	private static final String FILE_SCHEME = "file";

	/**
	 * Returns true if the given uri is a file uri and false otherwise.
	 * 
	 * @param uri the uri
	 * 
	 * @return true if the given uri is a file uri and false otherwise.
	 */
	public static boolean isFileURI(URI uri) {
		return StringUtils.isEmpty(uri.getScheme()) || FILE_SCHEME.equals(uri.getScheme());
	}

	/**
	 * Returns the path for the given file uri and null otherwise.
	 *
	 * @param uriString the Uri as string.
	 *
	 * @return the path for the given file uri and null otherwise.
	 */
	public static Path createPath(String uriString) {
		if (StringUtils.isEmpty(uriString)) {
			return null;
		}
		try {
			URI uri = URI.create(uriString);
			if (!isFileURI(uri)) {
				// The uri is not a file URI, ignore it.
				return null;
			}
		} catch (Exception e) {
			return null;
		}
		String fileUri = uriString;
		if (fileUri.startsWith("file:/")) {
			String convertedUri = fileUri.replace("file:///", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
			convertedUri = convertedUri.replace("file://", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
			return new File(URI.create(convertedUri)).toPath();
		}
		return new File(fileUri).toPath();
	}
	
	public static String toUri(Path path) {
		return path.toUri().toASCIIString();
	}
}
