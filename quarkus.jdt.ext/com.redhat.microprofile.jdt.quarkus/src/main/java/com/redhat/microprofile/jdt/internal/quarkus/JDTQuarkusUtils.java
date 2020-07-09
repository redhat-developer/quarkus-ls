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

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.microprofile.commons.metadata.ConverterKind;
import com.redhat.microprofile.commons.metadata.ItemMetadata;

/**
 * JDT Quarkus utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class JDTQuarkusUtils {

	private static final Logger LOGGER = Logger.getLogger(JDTQuarkusUtils.class.getName());

	private static final List<ConverterKind> DEFAULT_QUARKUS_CONVERTERS = Arrays.asList(ConverterKind.KEBAB_CASE,
			ConverterKind.VERBATIM);

	/**
	 * Returns the extension name (ex: quarkus-core) from the given JAR location (ex
	 * :
	 * C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.21.1/quarkus-core-0.21.1.jar).
	 * 
	 * @param location the JAR location
	 * @return the extension name (ex: quarkus-core) from the given JAR location.
	 */
	public static String getExtensionName(String location) {
		if (location == null) {
			return null;
		}
		if (!location.endsWith(".jar")) {
			return null;
		}
		int start = location.lastIndexOf('/');
		if (start == -1) {
			return null;
		}
		start++;
		int end = location.lastIndexOf('-');
		if (end == -1) {
			end = location.lastIndexOf('.');
		}
		if (end < start) {
			return null;
		}
		String extensionName = location.substring(start, end);
		if (extensionName.endsWith("-deployment")) {
			extensionName = extensionName.substring(0, extensionName.length() - "-deployment".length());
		}
		return extensionName;
	}

	/**
	 * Returns true if <code>javaProject</code> is a Quarkus project. Returns false
	 * otherwise.
	 * 
	 * @param javaProject the Java project to check
	 * @return true only if <code>javaProject</code> is a Quarkus project.
	 */
	public static boolean isQuarkusProject(IJavaProject javaProject) {
		try {
			return javaProject.findType(QuarkusConstants.QUARKUS_RUNTIME_CLASS_NAME) != null;
		} catch (JavaModelException e) {
			LOGGER.log(Level.INFO, "Current Java project is not a Quarkus project", e);
			return false;
		}
	}

	public static void updateConverterKinds(ItemMetadata metadata, IMember member, IType enclosedType)
			throws JavaModelException {
		if (enclosedType == null || !enclosedType.isEnum()) {
			return;
		}
		// By default Quarkus set the enum values as kebab and verbatim
		metadata.setConverterKinds(DEFAULT_QUARKUS_CONVERTERS);
	}

	public static boolean isSupportNamingStrategy(IJavaProject javaProject) {
		try {
			return javaProject.findType(QuarkusConstants.CONFIG_PROPERTIES_NAMING_STRATEGY_ENUM) != null;
		} catch (JavaModelException e) {
			return false;
		}
	}
}
