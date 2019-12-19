/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus;

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
	public static final String CONFIG_PROPERTIES_ANNOTATION = "io.quarkus.arc.config.ConfigProperties";
	
	public static final String CONFIG_ANNOTATION_NAME = "name";
	public static final String CONFIG_ROOT_ANNOTATION_PHASE = "phase";
	public static final String CONFIG_ITEM_ANNOTATION_DEFAULT_VALUE = "defaultValue";

	/**
	 * Quarkus properties file embedded in the Quarkus JAR.
	 */
	public static final String QUARKUS_JAVADOC_PROPERTIES = "quarkus-javadoc.properties";
	public static final String QUARKUS_EXTENSION_PROPERTIES = "quarkus-extension.properties";

	private QuarkusConstants() {
	}
}