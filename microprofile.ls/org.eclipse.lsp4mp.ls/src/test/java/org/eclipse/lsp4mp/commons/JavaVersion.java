/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.commons;

/**
 * Java version utility class
 * 
 * @author Fred Bricon
 *
 */
public class JavaVersion {
	/**
	 * Current Java specification version running in this JVM
	 */
	public static final int CURRENT;

	private JavaVersion() {
		//no public instantiation
	}
	
	static {
		// Java > 1.8 have better API than this. See java.lang.Runtime.version()
		String version = System.getProperty("java.specification.version");
		if (version.startsWith("1.")) {
			version = version.substring(2);
		}
		CURRENT = Integer.parseInt(version);
	}
	
}
