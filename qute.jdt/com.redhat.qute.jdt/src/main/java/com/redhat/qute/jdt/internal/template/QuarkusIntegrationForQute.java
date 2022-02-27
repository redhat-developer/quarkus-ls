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
package com.redhat.qute.jdt.internal.template;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.QuteProjectScope;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.usertags.UserTagInfo;
import com.redhat.qute.jdt.internal.template.datamodel.DataModelProviderRegistry;

/**
 * Support for Quarkus integration for Qute which collect parameters information
 * (a name and a Java type) for Qute template. This collect uses several
 * strategies :
 * 
 * <ul>
 * <li>@CheckedTemplate support: collect parameters for Qute Template by
 * searching @CheckedTemplate annotation.</li>
 * <li>Template field support: collect parameters for Qute Template by searching
 * Template instance declared as field in Java class.</li>
 * <li>Template extension support: see
 * https://quarkus.io/guides/qute-reference#template_extension_methods</li>
 * </ul>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#quarkus_integration
 * @see https://quarkus.io/guides/qute-reference#typesafe_templates
 * @see https://quarkus.io/guides/qute-reference#template_extension_methods
 */
public class QuarkusIntegrationForQute {

	private static final String TEMPLATES_TAGS_ENTRY = "templates.tags";
	private static final Logger LOGGER = Logger.getLogger(QuarkusIntegrationForQute.class.getName());

	public static DataModelProject<DataModelTemplate<DataModelParameter>> getDataModelProject(IJavaProject javaProject,
			IProgressMonitor monitor) throws CoreException {
		return DataModelProviderRegistry.getInstance().getDataModelProject(javaProject,
				QuteProjectScope.SOURCES_AND_DEPENDENCIES, monitor);
	}

	public static String resolveSignature(ILocalVariable methodParameter, IType type) {
		return resolveSignature(methodParameter.getTypeSignature(), type);
	}

	private static String resolveSignature(String signature, IType type) {
		if (signature.charAt(0) == '[') {
			// It's an array object
			// [QItem; --> org.acme.Item[]
			return resolveSignature2(signature.substring(1), type) + "[]";
		}
		int start = signature.indexOf('<');
		if (start == -1) {
			// No generic
			return resolveSignature2(signature, type);
		}

		String mainSignature = resolveSignature2(signature, type);
		int end = signature.indexOf('>', start);
		String genericSignature = resolveSignature2(signature.substring(start + 1, end), type);
		return mainSignature + "<" + genericSignature + ">";
	}

	private static String resolveSignature2(String signature, IType type) {
		String resolvedSignatureWithoutPackage = Signature.toString(signature);
		if (JavaTypeInfo.PRIMITIVE_TYPES.contains(resolvedSignatureWithoutPackage)) {
			return resolvedSignatureWithoutPackage;
		}

		try {
			String[][] resolvedSignature = type.resolveType(resolvedSignatureWithoutPackage);
			if (resolvedSignature != null) {
				return resolvedSignature[0][0] + "." + resolvedSignature[0][1];
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while resolving signature '" + resolvedSignatureWithoutPackage + "'.", e);
		}
		return resolvedSignatureWithoutPackage;
	}

	/**
	 * Collect user tags from the given Java project.
	 * 
	 * @param javaProject the Java project.
	 * @param monitor     the progress monitor
	 * 
	 * @return user tags from the given Java project.
	 * 
	 * @throws CoreException
	 */
	public static List<UserTagInfo> getUserTags(IJavaProject javaProject, IProgressMonitor monitor)
			throws CoreException {
		List<UserTagInfo> tags = new ArrayList<UserTagInfo>();
		// Loop for each JAR of the classpath and try to collect files from the
		// 'templates.tags' entry
		IClasspathEntry[] resolvedClasspath = ((JavaProject) javaProject).getResolvedClasspath();
		for (IClasspathEntry entry : resolvedClasspath) {
			if (entry.isTest()) {
				continue;
			}
			switch (entry.getEntryKind()) {

			case IClasspathEntry.CPE_LIBRARY:
				String jarPath = entry.getPath().toOSString();
				IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(jarPath);
				if (root != null) {
					collectUserTags(root, tags);
				}
			}
		}
		return tags;
	}

	/**
	 * Collect user tags for the given package fragment root.
	 * 
	 * @param root the package fragment root.
	 * @param tags the user tags list to fill.
	 * 
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	private static void collectUserTags(IPackageFragmentRoot root, List<UserTagInfo> tags)
			throws JavaModelException, CoreException {
		IJavaElement[] children = root.getChildren();
		for (IJavaElement child : children) {
			if (child instanceof IPackageFragment) {
				IPackageFragment packageRoot = (IPackageFragment) child;
				if (TEMPLATES_TAGS_ENTRY.equals(packageRoot.getElementName())) {
					// 'templates.tags' entry exists, loop for all resources to build user tags
					Object[] resources = packageRoot.getNonJavaResources();
					if (resources != null) {
						for (Object object : resources) {
							if (object instanceof IJarEntryResource) {
								// It's a HTML, etc file, build the user tag
								IJarEntryResource templateFile = (IJarEntryResource) object;
								String fileName = templateFile.getName();
								String uri = toUri(templateFile);
								String content = convertStreamToString(templateFile.getContents());
								UserTagInfo tagInfo = new UserTagInfo();
								tagInfo.setFileName(fileName);
								tagInfo.setUri(uri);
								tagInfo.setContent(content);
								tags.add(tagInfo);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Convert the given {@link InputStream} into a String. The source InputStream
	 * will then be closed.
	 * 
	 * @param is the input stream
	 * @return the given input stream in a String.
	 */
	private static String convertStreamToString(InputStream is) {
		try (Scanner s = new java.util.Scanner(is)) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}

	private static final String JDT_SCHEME = "jdt";
	private static final String CONTENTS_AUTHORITY = "jarentry";

	// see
	// https://github.com/microsoft/vscode-java-dependency/blob/27c306b770c23b1eba1f9a7c3e70d2793baced68/jdtls.ext/com.microsoft.jdtls.ext.core/src/com/microsoft/jdtls/ext/core/ExtUtils.java#L39

	private static String toUri(IJarEntryResource jarEntryFile) {
		IPackageFragmentRoot fragmentRoot = jarEntryFile.getPackageFragmentRoot();
		try {
			return new URI(JDT_SCHEME, CONTENTS_AUTHORITY, jarEntryFile.getFullPath().toPortableString(),
					fragmentRoot.getHandleIdentifier(), null).toASCIIString();
		} catch (URISyntaxException e) {
			JavaLanguageServerPlugin.logException("Error generating URI for jarentryfile ", e);
			return null;
		}
	}
}
