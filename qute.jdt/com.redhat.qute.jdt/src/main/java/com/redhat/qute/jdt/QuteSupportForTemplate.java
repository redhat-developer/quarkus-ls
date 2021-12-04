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
package com.redhat.qute.jdt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.jdt.internal.resolver.ClassFileTypeResolver;
import com.redhat.qute.jdt.internal.resolver.CompilationUnitTypeResolver;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;
import com.redhat.qute.jdt.internal.template.JavaTypesSearch;
import com.redhat.qute.jdt.internal.template.QuarkusIntegrationForQute;
import com.redhat.qute.jdt.utils.IJDTUtils;
import com.redhat.qute.jdt.utils.JDTQuteProjectUtils;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * Qute support for Template file.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteSupportForTemplate {

	private static final Logger LOGGER = Logger.getLogger(QuteSupportForTemplate.class.getName());

	private static final String JAVA_LANG_ITERABLE = "java.lang.Iterable";

	private static final List<String> COMMONS_ITERABLE_TYPES = Arrays.asList("Iterable", JAVA_LANG_ITERABLE,
			"java.util.List", "java.util.Set");

	private static final QuteSupportForTemplate INSTANCE = new QuteSupportForTemplate();

	public static QuteSupportForTemplate getInstance() {
		return INSTANCE;
	}

	/**
	 * Returns the project information for the given project Uri.
	 * 
	 * @param params  the project information parameters.
	 * @param utils   the JDT LS utility.
	 * @param monitor the progress monitor.
	 * 
	 * @return the project information for the given project Uri and null otherwise.
	 */
	public ProjectInfo getProjectInfo(QuteProjectParams params, IJDTUtils utils, IProgressMonitor monitor) {
		IJavaProject javaProject = getJavaProjectFromTemplateFile(params.getTemplateFileUri(), utils);
		if (javaProject == null) {
			return null;
		}
		return JDTQuteProjectUtils.getProjectInfo(javaProject);
	}

	/**
	 * Collect data model templates from the given project Uri. A data model
	 * template can be declared with:
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
	 * @param params  the project uri.
	 * @param utils   JDT LS utilities
	 * @param monitor the progress monitor
	 * 
	 * @return data model templates from the given project Uri.
	 * 
	 * @throws CoreException
	 */
	public DataModelProject<DataModelTemplate<DataModelParameter>> getDataModelProject(
			QuteDataModelProjectParams params, IJDTUtils utils, IProgressMonitor monitor) throws CoreException {
		String projectUri = params.getProjectUri();
		IJavaProject javaProject = getJavaProjectFromProjectUri(projectUri);
		if (javaProject == null) {
			return null;
		}
		return QuarkusIntegrationForQute.getDataModelProject(javaProject, monitor);
	}

	/**
	 * Returns Java types for the given pattern which belong to the given project
	 * Uri.
	 * 
	 * @param params  the java types parameters.
	 * @param utils   the JDT LS utility.
	 * @param monitor the progress monitor.
	 * 
	 * @return list of Java types.
	 * 
	 * @throws CoreException
	 */
	public List<JavaTypeInfo> getJavaTypes(QuteJavaTypesParams params, IJDTUtils utils, IProgressMonitor monitor)
			throws CoreException {
		String projectUri = params.getProjectUri();
		IJavaProject javaProject = getJavaProjectFromProjectUri(projectUri);
		if (javaProject == null) {
			return null;
		}
		return new JavaTypesSearch(params.getPattern(), javaProject).search(monitor);
	}

	public Location getJavaDefinition(QuteJavaDefinitionParams params, IJDTUtils utils, IProgressMonitor monitor)
			throws CoreException {
		String projectUri = params.getProjectUri();
		IJavaProject javaProject = getJavaProjectFromProjectUri(projectUri);
		if (javaProject == null) {
			return null;
		}
		String className = params.getSourceType();
		IType type = findType(className, javaProject, monitor);
		if (type == null) {
			return null;
		}

		String fieldName = params.getSourceField();
		if (fieldName != null) {
			IField field = type.getField(fieldName);
			return field != null && field.exists() ? utils.toLocation(field) : null;
		}

		String sourceMethod = params.getSourceMethod();
		if (sourceMethod != null) {
			IMethod method = findMethod(type, sourceMethod);
			String sourceMethodParameter = params.getSourceMethodParameter();
			if (sourceMethodParameter != null) {
				ILocalVariable[] parameters = method.getParameters();
				for (ILocalVariable parameter : parameters) {
					if (sourceMethodParameter.equals(parameter.getElementName())) {
						return utils.toLocation(parameter);
					}
				}
				return null;
			}
			return method != null && method.exists() ? utils.toLocation(method) : null;
		}

		return utils.toLocation(type);
	}

	private IMethod findMethod(IType type, String sourceMethod) throws JavaModelException {
		// For the moment we search method only by name
		// FIXME:use method signature to retrieve the proper method (see findMethodOLD)
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			if (sourceMethod.equals(method.getElementName())) {
				return method;
			}
		}
		return null;
	}

	private IMethod findMethodOLD(IType type, String sourceMethod) throws JavaModelException {
		int startBracketIndex = sourceMethod.indexOf('(');
		String methodName = sourceMethod.substring(0, startBracketIndex);
		// Method signature has been generated with JDT API, so we are sure that we have
		// a ')' character.
		int endBracketIndex = sourceMethod.indexOf(')');
		String methodSignature = sourceMethod.substring(startBracketIndex, endBracketIndex + 1);
		String[] paramTypes = methodSignature.isEmpty() ? CharOperation.NO_STRINGS
				: Signature.getParameterTypes(methodSignature);

		// try findMethod for non constructor. If result is null, findMethod for
		// constructor
		IMethod method = JavaModelUtil.findMethod(methodName, paramTypes, false, type);
		if (method == null) {
			method = JavaModelUtil.findMethod(methodName, paramTypes, true, type);
		}
		return method;
	}

	public ResolvedJavaTypeInfo getResolvedJavaType(QuteResolvedJavaTypeParams params, IJDTUtils utils,
			IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		String projectUri = params.getProjectUri();
		IJavaProject javaProject = getJavaProjectFromProjectUri(projectUri);
		if (javaProject == null) {
			return null;
		}
		String className = params.getClassName();
		int index = className.indexOf('<');
		if (index != -1) {
			// ex : java.util.List<org.acme.Item>
			String iterableClassName = className.substring(0, index);
			IType iterableType = findType(iterableClassName, javaProject, monitor);
			if (iterableType == null) {
				return null;
			}

			boolean iterable = isIterable(iterableType, monitor);
			if (!iterable) {
				return null;
			}

			String iterableOf = className.substring(index + 1, className.length() - 1);
			return createIterableType(className, iterableClassName, iterableOf);
		} else if (className.endsWith("[]")) {
			// ex : org.acme.Item[]
			String iterableOfClassName = className.substring(0, className.length() - 2);
			IType iterableOf = findType(iterableOfClassName, javaProject, monitor);
			if (iterableOf == null) {
				return null;
			}
			return createIterableType(className, null, iterableOfClassName);
		}

		// ex : org.acme.Item
		IType type = findType(className, javaProject, monitor);
		if (type == null) {
			return null;
		}

		ITypeResolver typeResolver = createTypeResolver(type);

		// Collect fields
		List<JavaFieldInfo> fieldsInfo = new ArrayList<>();
		IField[] fields = type.getFields();
		for (IField field : fields) {
			if (isValidField(field)) {
				// Only public fields are available
				JavaFieldInfo info = new JavaFieldInfo();
				info.setSignature(typeResolver.resolveFieldSignature(field));
				fieldsInfo.add(info);
			}
		}

		// Collect methods
		List<JavaMethodInfo> methodsInfo = new ArrayList<>();
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			if (isValidMethod(method, type.isInterface())) {
				try {
					JavaMethodInfo info = new JavaMethodInfo();
					info.setSignature(typeResolver.resolveMethodSignature(method));
					methodsInfo.add(info);
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE,
							"Error while getting method signature of '" + method.getElementName() + "'.", e);
				}
			}
		}

		// Collect type extensions
		List<String> extendedTypes = null;
		if (type.isInterface()) {
			IType[] interfaces = findImplementedInterfaces(type, monitor);
			if (interfaces != null && interfaces.length > 0) {
				extendedTypes = Stream.of(interfaces) //
						.map(interfaceType -> interfaceType.getFullyQualifiedName()) //
						.collect(Collectors.toList());
			}
		} else {
			ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(monitor);
			IType[] rootClasses = typeHierarchy.getAllClasses();
			extendedTypes = Stream.of(rootClasses) //
					.map(interfaceType -> interfaceType.getFullyQualifiedName()) //
					.collect(Collectors.toList());

		}

		if (extendedTypes != null) {
			extendedTypes.remove(className);
		}

		ResolvedJavaTypeInfo resolvedClass = new ResolvedJavaTypeInfo();
		resolvedClass.setSignature(className);
		resolvedClass.setFields(fieldsInfo);
		resolvedClass.setMethods(methodsInfo);
		resolvedClass.setExtendedTypes(extendedTypes);
		return resolvedClass;
	}

	private IType findType(String className, IJavaProject javaProject, IProgressMonitor monitor)
			throws JavaModelException {
		try {
			IType type = javaProject.findType(className, monitor);
			if (type != null) {
				return type;
			}
			if (className.indexOf('.') == -1) {
				// No package, try with java.lang package
				// ex : if className = String we should find type of java.lang.String
				return javaProject.findType("java.lang." + className, monitor);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while finding type for '" + className + "'.", e);
		}
		return null;
	}

	private ResolvedJavaTypeInfo createIterableType(String className, String iterableClassName, String iterableOf) {
		ResolvedJavaTypeInfo resolvedClass = new ResolvedJavaTypeInfo();
		resolvedClass.setSignature(className);
		resolvedClass.setIterableType(iterableClassName);
		resolvedClass.setIterableOf(iterableOf);
		return resolvedClass;
	}

	private static boolean isIterable(IType iterableType, IProgressMonitor monitor) throws CoreException {
		String iterableClassName = iterableType.getFullyQualifiedName();
		// Fast test
		if (COMMONS_ITERABLE_TYPES.contains(iterableClassName)) {
			return true;
		}
		// Check if type implements "java.lang.Iterable"
		IType[] interfaces = findImplementedInterfaces(iterableType, monitor);
		boolean iterable = interfaces == null ? false
				: Stream.of(interfaces)
						.anyMatch(interfaceType -> JAVA_LANG_ITERABLE.equals(interfaceType.getFullyQualifiedName()));
		return iterable;
	}

	private static boolean isValidField(IField field) throws JavaModelException {
		return Flags.isPublic(field.getFlags());
	}

	private static boolean isValidMethod(IMethod method, boolean isInterface) {
		try {
			if (method.isConstructor() || !method.exists()) {
				return false;
			}
			if (!isInterface && !Flags.isPublic(method.getFlags())) {
				return false;
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while checking if '" + method.getElementName() + "' is valid.", e);
			return false;
		}
		return true;
	}

	private static IJavaProject getJavaProjectFromProjectUri(String projectName) {
		if (projectName == null) {
			return null;
		}
		IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
		return javaProject.exists() ? javaProject : null;
	}

	private static IJavaProject getJavaProjectFromTemplateFile(String templateFileUri, IJDTUtils utils) {
		templateFileUri = templateFileUri.replace("vscode-notebook-cell", "file");
		IFile file = utils.findFile(templateFileUri);
		if (file == null || file.getProject() == null) {
			// The uri doesn't belong to an Eclipse project
			return null;
		}
		// The uri belong to an Eclipse project
		if (!(JavaProject.hasJavaNature(file.getProject()))) {
			// The uri doesn't belong to a Java project
			return null;
		}

		String projectName = file.getProject().getName();
		return JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
	}

	private static IType[] findImplementedInterfaces(IType type, IProgressMonitor progressMonitor)
			throws CoreException {
		ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(progressMonitor);
		return typeHierarchy.getRootInterfaces();
	}

	public static ITypeResolver createTypeResolver(IMember member) {
		ITypeResolver typeResolver = !member.isBinary()
				? new CompilationUnitTypeResolver((ICompilationUnit) member.getAncestor(IJavaElement.COMPILATION_UNIT))
				: new ClassFileTypeResolver((IClassFile) member.getAncestor(IJavaElement.CLASS_FILE));
		return typeResolver;
	}

}
