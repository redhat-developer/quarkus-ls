/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.faulttolerance.properties;

import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.findType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getDefaultValue;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getEnclosedType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getPropertyType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getResolvedResultTypeName;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceMethod;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceType;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.ASYNCHRONOUS_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.BULKHEAD_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.CIRCUITBREAKER_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.FALLBACK_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.MP_FAULT_TOLERANCE_NONFALLBACK_ENABLED_DESCRIPTION;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.MP_FAULT_TOLERANCE_NON_FALLBACK_ENABLED;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.RETRY_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.TIMEOUT_ANNOTATION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.jdt.core.AbstractAnnotationTypeReferencePropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.IPropertiesCollector;
import org.eclipse.lsp4mp.jdt.core.SearchContext;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;

/**
 * Properties provider to collect MicroProfile properties from the MicroProfile
 * Fault Tolerance annotations.
 * 
 * @author Angelo ZERR
 * 
 * @see https://github.com/eclipse/microprofile-fault-tolerance/blob/master/spec/src/main/asciidoc/configuration.asciidoc
 *
 */
public class MicroProfileFaultToleranceProvider extends AbstractAnnotationTypeReferencePropertiesProvider {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileFaultToleranceProvider.class.getName());

	private static final String MICROPROFILE_FAULT_TOLERANCE_CONTEXT_KEY = MicroProfileFaultToleranceProvider.class
			.getName() + "#MicroProfileFaultToleranceContext";

	private static final String[] ANNOTATION_NAMES = { ASYNCHRONOUS_ANNOTATION, BULKHEAD_ANNOTATION,
			CIRCUITBREAKER_ANNOTATION, FALLBACK_ANNOTATION, RETRY_ANNOTATION, TIMEOUT_ANNOTATION };

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	static class AnnotationInfo {

		private final String name;

		private final String simpleName;

		private final List<AnnotationParameter> parameters;

		public AnnotationInfo(IType annotation, IJDTUtils utils, DocumentFormat documentFormat)
				throws JavaModelException {
			this.name = annotation.getFullyQualifiedName();
			this.simpleName = annotation.getElementName();
			this.parameters = new ArrayList<>();
			IMethod[] methods = annotation.getMethods();
			if (methods != null) {
				for (IMethod method : methods) {

					// name
					String name = method.getElementName();

					// type
					String methodResultTypeName = getResolvedResultTypeName(method);
					IType returnType = findType(method.getJavaProject(), methodResultTypeName);
					String type = getPropertyType(returnType, methodResultTypeName);

					// description
					String description = utils.getJavadoc(method, documentFormat);

					// Method source
					String sourceType = getSourceType(method);
					String sourceMethod = getSourceMethod(method);

					String defaultValue = getDefaultValue(method);
					// Enumerations
					IType enclosedType = getEnclosedType(returnType, type, method.getJavaProject());

					AnnotationParameter parameter = new AnnotationParameter(name, type, enclosedType, description,
							sourceType, sourceMethod, defaultValue);
					parameters.add(parameter);
				}
			}
			AnnotationParameter parameter = new AnnotationParameter("enabled", "boolean", null, "Enabling the policy",
					name, null, "true");
			parameters.add(parameter);
		}

		public String getName() {
			return name;
		}

		public String getSimpleName() {
			return simpleName;
		}

		public List<AnnotationParameter> getParameters() {
			return parameters;
		}

	}

	static class AnnotationParameter {

		private final String name;
		private final String type;
		private final IType jdtType;
		private final String description;
		private final String sourceType;
		private final String sourceMethod;
		private final String defaultValue;

		public AnnotationParameter(String name, String type, IType jdtType, String description, String sourceType,
				String sourceMethod, String defaultValue) {
			this.name = name;
			this.type = type;
			this.jdtType = jdtType;
			this.description = description;
			this.sourceType = sourceType;
			this.sourceMethod = sourceMethod;
			this.defaultValue = defaultValue;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public String getDescription() {
			return description;
		}

		public String getSourceType() {
			return sourceType;
		}

		public String getSourceMethod() {
			return sourceMethod;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public IType getJDTType() {
			return jdtType;
		}
	}

	static class MicroProfileFaultToleranceContext {

		private final IJavaProject javaProject;

		private final IJDTUtils utils;

		private final DocumentFormat documentFormat;

		private final Map<String, AnnotationInfo> cache;

		private final Set<String> processedAnnotations;

		public MicroProfileFaultToleranceContext(IJavaProject javaProject, IJDTUtils utils,
				DocumentFormat documentFormat) {
			this.cache = new HashMap<>();
			this.processedAnnotations = new HashSet<>();
			this.javaProject = javaProject;
			this.utils = utils;
			this.documentFormat = documentFormat;
		}

		public AnnotationInfo getAnnotationInfo(String annotation, IProgressMonitor monitor) throws JavaModelException {
			AnnotationInfo info = cache.get(annotation);
			if (info != null) {
				return info;
			}
			return registerAnnotation(annotation, monitor);
		}

		private AnnotationInfo registerAnnotation(String annotationName, IProgressMonitor monitor)
				throws JavaModelException {
			IType annotation = javaProject.findType(annotationName);
			if (annotation == null) {
				return null;
			}
			// Download sources of MicroProfile Fault Tolerance to retrieve the proper
			// Javadoc
			IClassFile classFile = annotation.getClassFile();
			if (classFile != null) {
				try {
					utils.discoverSource(classFile, monitor);
				} catch (CoreException e) {
					LOGGER.log(Level.WARNING,
							"Error while downloading sources for MicroProfile Fault Tolerance dependency", e);
				}
			}
			AnnotationInfo info = new AnnotationInfo(annotation, utils, documentFormat);
			cache.put(info.getName(), info);
			return info;
		}

		/**
		 * Returns true if generation of properties for the given annotation name has
		 * been processed and false otherwise.
		 * 
		 * @param annotationName the MicroProfile Fault Tolerance annotation.
		 * @return true if generation of properties for the given annotation name has
		 *         been processed and false otherwise.
		 */
		public boolean isProcessed(String annotationName) {
			return isProcessed(null, annotationName);
		}

		/**
		 * Return true if generation of properties for the given class name and
		 * annotation name has been processed and false otherwise.
		 * 
		 * @param className      the class name
		 * @param annotationName the MicroProfile Fault Tolerance annotation.
		 * @return true if generation of properties for the given class name and
		 *         annotation name has been processed and false otherwise.
		 */
		public boolean isProcessed(String className, String annotationName) {
			return processedAnnotations.contains(createPrefix(className, null, annotationName));
		}

		public boolean setProcessed(String annotationName) {
			return setProcessed(null, annotationName);
		}

		public boolean setProcessed(String className, String annotationName) {
			return processedAnnotations.add(createPrefix(className, null, annotationName));
		}

		public void addFaultToleranceProperties(IPropertiesCollector collector) {
			// According MP FT sepcification, there are 2 properties:
			// - MP_Fault_Tolerance_NonFallback_Enabled. This property needs to be hard
			// coded
			// - MP_Fault_Tolerance_Metrics_Enabled. This property comes from
			// https://github.com/smallrye/smallrye-fault-tolerance/blob/09901426a7b2228103a706cc58288ebb59934150/implementation/fault-tolerance/src/main/java/io/smallrye/faulttolerance/metrics/MetricsCollectorFactory.java#L30

			if (processedAnnotations.contains(MP_FAULT_TOLERANCE_NON_FALLBACK_ENABLED)) {
				return;
			}
			collector.addItemMetadata(MP_FAULT_TOLERANCE_NON_FALLBACK_ENABLED, "boolean",
					MP_FAULT_TOLERANCE_NONFALLBACK_ENABLED_DESCRIPTION, null, null, null, "false", null, false, 0);
			processedAnnotations.add(MP_FAULT_TOLERANCE_NON_FALLBACK_ENABLED);
		}
	}

	private void collectProperties(IPropertiesCollector collector, AnnotationInfo info, IMember annotatedClassOrMethod,
			IAnnotation mpftAnnotation, MicroProfileFaultToleranceContext mpftContext) throws JavaModelException {
		String annotationName = info.getSimpleName();
		String className = null;
		String methodName = null;
		boolean binary = false;
		String sourceType = null;
		String sourceMethod = null;
		if (annotatedClassOrMethod != null) {
			binary = annotatedClassOrMethod.isBinary();
			switch (annotatedClassOrMethod.getElementType()) {
			case IJavaElement.TYPE:
				IType annotatedClass = (IType) annotatedClassOrMethod;
				className = annotatedClass.getFullyQualifiedName();
				// Check if properties has been generated for the <classname><annotation>:
				if (isProcessed(className, annotationName, mpftContext)) {
					return;
				}
				sourceType = getPropertyType(annotatedClass, className);
				break;
			case IJavaElement.METHOD:
				IMethod annotatedMethod = (IMethod) annotatedClassOrMethod;
				className = annotatedMethod.getDeclaringType().getFullyQualifiedName();
				methodName = annotatedMethod.getElementName();
				sourceType = getSourceType(annotatedMethod);
				sourceMethod = getSourceMethod(annotatedMethod);
				break;
			}
		} else {
			// Check if properties has been generated for the <annotation>:
			if (isProcessed(null, annotationName, mpftContext)) {
				return;
			}
		}

		String prefix = createPrefix(className, methodName, annotationName);

		// parameter
		List<AnnotationParameter> parameters = info.getParameters();
		for (AnnotationParameter parameter : parameters) {
			String propertyName = new StringBuilder(prefix).append('/').append(parameter.getName()).toString();
			String parameterType = parameter.getType();
			String description = parameter.getDescription();
			String defaultValue = getParameterDefaultValue(parameter, mpftAnnotation);
			String extensionName = null;
			if (annotatedClassOrMethod == null) {
				sourceType = parameter.getSourceType();
				sourceMethod = parameter.getSourceMethod();
			}
			// Enumerations
			IType jdtType = parameter.getJDTType();
			super.updateHint(collector, jdtType);

			super.addItemMetadata(collector, propertyName, parameterType, description, sourceType, null, sourceMethod,
					defaultValue, extensionName, binary);
		}
	}

	private static boolean isProcessed(String className, String annotationName,
			MicroProfileFaultToleranceContext mpftContext) {
		if (mpftContext.isProcessed(className, annotationName)) {
			return true;
		}
		mpftContext.setProcessed(className, annotationName);
		return false;
	}

	private static String getParameterDefaultValue(AnnotationParameter parameter, IAnnotation mpftAnnotation)
			throws JavaModelException {
		String defaultValue = mpftAnnotation != null ? getAnnotationMemberValue(mpftAnnotation, parameter.getName())
				: null;
		return defaultValue != null ? defaultValue : parameter.getDefaultValue();
	}

	private static String createPrefix(String className, String methodName, String annotationName) {
		if (className == null && methodName == null) {
			return annotationName;
		}
		StringBuilder prefix = new StringBuilder();
		// classname
		if (className != null) {
			prefix.append(className).append('/');
		}
		// methodname
		if (methodName != null) {
			prefix.append(methodName).append('/');
		}
		// annotation
		prefix.append(annotationName);
		return prefix.toString();
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation mpftAnnotation, String annotationName,
			SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		if (!(javaElement instanceof IMember)) {
			return;
		}
		// The java element is method or a class
		MicroProfileFaultToleranceContext mpftContext = getMicroProfileFaultToleranceContext(context);
		AnnotationInfo info = mpftContext.getAnnotationInfo(annotationName, monitor);
		if (info != null) {
			// 1. Collect properties for <annotation>/<list of parameters>
			collectProperties(context.getCollector(), info, null, null, mpftContext);
			mpftContext.addFaultToleranceProperties(context.getCollector());
			// 2. Collect properties for <classname>/<annotation>/<list of parameters>
			if (javaElement.getElementType() == IJavaElement.METHOD) {
				IMethod annotatedMethod = (IMethod) javaElement;
				IType classType = annotatedMethod.getDeclaringType();
				IAnnotation mpftAnnotationForClass = getAnnotation(classType, annotationName);
				collectProperties(context.getCollector(), info, classType, mpftAnnotationForClass, mpftContext);
			}
			// 3. Collect properties for <classname>/<annotation>/<list of parameters> or
			// <classname>/<methodname>/<annotation>/<list of parameters>
			collectProperties(context.getCollector(), info, (IMember) javaElement, mpftAnnotation, mpftContext);
		}
	}

	private static MicroProfileFaultToleranceContext getMicroProfileFaultToleranceContext(SearchContext context) {
		MicroProfileFaultToleranceContext mpftContext = (MicroProfileFaultToleranceContext) context
				.get(MICROPROFILE_FAULT_TOLERANCE_CONTEXT_KEY);
		if (mpftContext == null) {
			mpftContext = new MicroProfileFaultToleranceContext(context.getJavaProject(), context.getUtils(),
					context.getDocumentFormat());
			context.put(MICROPROFILE_FAULT_TOLERANCE_CONTEXT_KEY, mpftContext);
		}
		return mpftContext;
	}
}
