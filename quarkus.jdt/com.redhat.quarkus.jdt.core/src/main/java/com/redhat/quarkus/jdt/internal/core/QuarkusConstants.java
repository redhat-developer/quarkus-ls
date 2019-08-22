/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.internal.core;

/**
 * Quarkus constants.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusConstants {

	/**
	 * Quarkus prefix used in the Quarkus property.
	 */
	public static final String QUARKUS_PREFIX = "quarkus.";

	/**
	 * The Quarkus Config* annotations
	 */
	public static final String CONFIG_ROOT_ANNOTATION = "io.quarkus.runtime.annotations.ConfigRoot";
	public static final String CONFIG_GROUP_ANNOTATION = "io.quarkus.runtime.annotations.ConfigGroup";
	public static final String CONFIG_ITEM_ANNOTATION = "io.quarkus.runtime.annotations.ConfigItem";
	public static final String CONFIG_PROPERTY_ANNOTATION = "org.eclipse.microprofile.config.inject.ConfigProperty";

	/**
	 * Quarkus properties file embedded in the Quarkus JAR.
	 */
	public static final String QUARKUS_JAVADOC_PROPERTIES = "quarkus-javadoc.properties";

	/**
	 * Quarkus client commands
	 */
	public static final String QUARKUS_CLASSPATH_CHANGED_COMMAND = "quarkusTools.classpathChanged";

	private QuarkusConstants() {
	}
}