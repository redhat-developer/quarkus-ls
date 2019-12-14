/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.providers;

import static com.redhat.microprofile.jdt.core.utils.AnnotationUtils.getAnnotation;
import static com.redhat.microprofile.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.findType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getPropertyType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getResolvedResultTypeName;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getResolvedTypeName;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getSourceField;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getSourceMethod;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getSourceType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.isSimpleFieldType;
import static io.quarkus.runtime.util.StringUtil.camelHumpsIterator;
import static io.quarkus.runtime.util.StringUtil.join;
import static io.quarkus.runtime.util.StringUtil.lowerCase;
import static io.quarkus.runtime.util.StringUtil.withoutSuffix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.microprofile.jdt.core.AbstractAnnotationTypeReferencePropertiesProvider;
import com.redhat.microprofile.jdt.core.IPropertiesCollector;
import com.redhat.microprofile.jdt.core.MicroProfileConstants;
import com.redhat.microprofile.jdt.core.SearchContext;
import com.redhat.microprofile.jdt.internal.quarkus.JDTQuarkusUtils;
import com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants;

import io.quarkus.arc.config.ConfigProperties;
import io.quarkus.deployment.bean.JavaBeanUtil;

/**
 * Properties provider to collect Quarkus properties from the Java classes or
 * interfaces annotated with "io.quarkus.arc.config.ConfigProperties"
 * annotation.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusConfigPropertiesProvider extends AbstractAnnotationTypeReferencePropertiesProvider {

	private static final Logger LOGGER = Logger.getLogger(QuarkusConfigPropertiesProvider.class.getName());

	private static final String[] ANNOTATION_NAMES = { QuarkusConstants.CONFIG_PROPERTIES_ANNOTATION };

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation annotation, String annotationName,
			SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		processConfigProperties(javaElement, annotation, context.getCollector(), monitor);
	}

	// ------------- Process Quarkus ConfigProperties -------------

	private void processConfigProperties(IJavaElement javaElement, IAnnotation configPropertiesAnnotation,
			IPropertiesCollector collector, IProgressMonitor monitor) throws JavaModelException {
		if (javaElement.getElementType() != IJavaElement.TYPE) {
			return;
		}
		IType configPropertiesType = (IType) javaElement;
		// Location (JAR, src)
		IPackageFragmentRoot packageRoot = (IPackageFragmentRoot) javaElement
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		String location = packageRoot.getPath().toString();
		// Quarkus Extension name
		String extensionName = JDTQuarkusUtils.getExtensionName(location);

		String prefix = determinePrefix(configPropertiesType, configPropertiesAnnotation);
		if (configPropertiesType.isInterface()) {
			// See
			// https://github.com/quarkusio/quarkus/blob/0796d712d9a3cf8251d9d8808b705f1a04032ee2/extensions/arc/deployment/src/main/java/io/quarkus/arc/deployment/configproperties/InterfaceConfigPropertiesUtil.java#L89
			List<IType> allInterfaces = new ArrayList<>(Arrays.asList(findInterfaces(configPropertiesType, monitor)));
			allInterfaces.add(0, configPropertiesType);

			for (IType configPropertiesInterface : allInterfaces) {
				// Loop for each methods.
				IJavaElement[] elements = configPropertiesInterface.getChildren();
				// Loop for each fields.
				for (IJavaElement child : elements) {
					if (child.getElementType() == IJavaElement.METHOD) {
						IMethod method = (IMethod) child;
						if (Flags.isDefaultMethod(method.getFlags())) { // don't do anything with default methods
							continue;
						}
						if (method.getNumberOfParameters() > 0) {
							LOGGER.log(Level.INFO,
									"Method " + method.getElementName() + " of interface "
											+ method.getDeclaringType().getFullyQualifiedName()
											+ " is not a getter method since it defined parameters");
							continue;
						}
						if ("Void()".equals(method.getReturnType())) {
							LOGGER.log(Level.INFO,
									"Method " + method.getElementName() + " of interface "
											+ method.getDeclaringType().getFullyQualifiedName()
											+ " is not a getter method since it returns void");
							continue;
						}
						String name = null;
						String defaultValue = null;
						IAnnotation configPropertyAnnotation = getAnnotation(method,
								MicroProfileConstants.CONFIG_PROPERTY_ANNOTATION);
						if (configPropertyAnnotation != null) {
							name = getAnnotationMemberValue(configPropertyAnnotation,
									MicroProfileConstants.CONFIG_PROPERTY_ANNOTATION_NAME);
							defaultValue = getAnnotationMemberValue(configPropertyAnnotation,
									MicroProfileConstants.CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE);
						}
						if (name == null) {
							name = getPropertyNameFromMethodName(method);
						}
						if (name == null) {
							continue;
						}

						String propertyName = prefix + "." + name;
						String methodResultTypeName = getResolvedResultTypeName(method);
						IType returnType = findType(method.getJavaProject(), methodResultTypeName);

						// Method result type
						String type = getPropertyType(returnType, methodResultTypeName);

						// TODO: extract Javadoc from Java sources
						String description = null;

						// Method source
						String sourceType = getSourceType(method);
						String sourceMethod = getSourceMethod(method);

						// Enumerations
						super.updateHint(collector, returnType);

						if (isSimpleFieldType(returnType, methodResultTypeName)) {
							super.addItemMetadata(collector, propertyName, type, description, sourceType, null,
									sourceMethod, defaultValue, extensionName, method.isBinary());
						} else {
							populateConfigObject(returnType, propertyName, extensionName, new HashSet<>(), collector,
									monitor);
						}

					}
				}
			}
		} else {
			// See
			// https://github.com/quarkusio/quarkus/blob/e8606513e1bd14f0b1aaab7f9969899bd27c55a3/extensions/arc/deployment/src/main/java/io/quarkus/arc/deployment/configproperties/ClassConfigPropertiesUtil.java#L117
			// TODO : validation
			populateConfigObject(configPropertiesType, prefix, extensionName, new HashSet<>(), collector, monitor);
		}
	}

	private static IType[] findInterfaces(IType type, IProgressMonitor progressMonitor) throws JavaModelException {
		ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(progressMonitor);
		return typeHierarchy.getAllSuperInterfaces(type);
	}

	private void populateConfigObject(IType configPropertiesType, String prefixStr, String extensionName,
			Set<IType> typesAlreadyProcessed, IPropertiesCollector collector, IProgressMonitor monitor)
			throws JavaModelException {
		if (typesAlreadyProcessed.contains(configPropertiesType)) {
			return;
		}
		typesAlreadyProcessed.add(configPropertiesType);
		IJavaElement[] elements = configPropertiesType.getChildren();
		// Loop for each fields.
		for (IJavaElement child : elements) {
			if (child.getElementType() == IJavaElement.FIELD) {
				// The following code is an adaptation for JDT of
				// Quarkus arc code:
				// https://github.com/quarkusio/quarkus/blob/e8606513e1bd14f0b1aaab7f9969899bd27c55a3/extensions/arc/deployment/src/main/java/io/quarkus/arc/deployment/configproperties/ClassConfigPropertiesUtil.java#L211
				IField field = (IField) child;
				boolean useFieldAccess = false;
				String setterName = JavaBeanUtil.getSetterName(field.getElementName());
				String configClassInfo = configPropertiesType.getFullyQualifiedName();
				IMethod setter = findMethod(configPropertiesType, setterName, field.getTypeSignature());
				if (setter == null) {
					if (!Flags.isPublic(field.getFlags()) || Flags.isFinal(field.getFlags())) {
						LOGGER.log(Level.INFO,
								"Configuration properties class " + configClassInfo
										+ " does not have a setter for field " + field
										+ " nor is the field a public non-final field");
						continue;
					}
					useFieldAccess = true;
				}
				if (!useFieldAccess && !Flags.isPublic(setter.getFlags())) {
					LOGGER.log(Level.INFO, "Setter " + setterName + " of class " + configClassInfo + " must be public");
					continue;
				}

				String name = field.getElementName();
				// The default value is managed with assign like : 'public String suffix = "!"';
				// Getting "!" value is possible but it requires to re-parse the Java file to
				// build a DOM CompilationUnit to extract assigned value.
				final String defaultValue = null;
				String propertyName = prefixStr + "." + name;

				String fieldTypeName = getResolvedTypeName(field);
				IType fieldClass = findType(field.getJavaProject(), fieldTypeName);
				if (isSimpleFieldType(fieldClass, fieldTypeName)) {

					// Class type
					String type = getPropertyType(fieldClass, fieldTypeName);

					// Javadoc
					String description = null;

					// field and class source
					String sourceType = getSourceType(field);
					String sourceField = getSourceField(field);

					// Enumerations
					super.updateHint(collector, fieldClass);

					super.addItemMetadata(collector, propertyName, type, description, sourceType, sourceField, null,
							defaultValue, extensionName, field.isBinary());
				} else {
					populateConfigObject(fieldClass, propertyName, extensionName, typesAlreadyProcessed, collector,
							monitor);
				}
			}
		}
	}

	private static String getPropertyNameFromMethodName(IMethod method) {
		try {
			return JavaBeanUtil.getPropertyNameFromGetter(method.getElementName());
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.INFO, "Method " + method.getElementName() + " of interface "
					+ method.getDeclaringType().getElementName()
					+ " is not a getter method. Either rename the method to follow getter name conventions or annotate the method with @ConfigProperty");
			return null;
		}
	}

	private static IMethod findMethod(IType configPropertiesType, String setterName, String fieldTypeSignature) {
		IMethod method = configPropertiesType.getMethod(setterName, new String[] { fieldTypeSignature });
		return method.exists() ? method : null;
	}

	private static String determinePrefix(IType configPropertiesType, IAnnotation configPropertiesAnnotation)
			throws JavaModelException {
		String fromAnnotation = getPrefixFromAnnotation(configPropertiesAnnotation);
		if (fromAnnotation != null) {
			return fromAnnotation;
		}
		return getPrefixFromClassName(configPropertiesType);
	}

	private static String getPrefixFromAnnotation(IAnnotation configPropertiesAnnotation) throws JavaModelException {
		String value = getAnnotationMemberValue(configPropertiesAnnotation, "prefix");
		if (value == null) {
			return null;
		}
		if (ConfigProperties.UNSET_PREFIX.equals(value) || value.isEmpty()) {
			return null;
		}
		return value;
	}

	private static String getPrefixFromClassName(IType className) {
		String simpleName = className.getElementName(); // className.isInner() ? className.local() :
														// className.withoutPackagePrefix();
		return join("-", withoutSuffix(lowerCase(camelHumpsIterator(simpleName)), "config", "configuration",
				"properties", "props"));
	}

}
