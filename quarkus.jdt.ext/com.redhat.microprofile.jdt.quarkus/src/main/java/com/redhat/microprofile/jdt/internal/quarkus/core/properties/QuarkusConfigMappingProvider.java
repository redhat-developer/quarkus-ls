/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.core.properties;

import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.CONFIG_MAPPING_ANNOTATION;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.CONFIG_MAPPING_ANNOTATION_NAMING_STRATEGY;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.CONFIG_MAPPING_ANNOTATION_PREFIX;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.CONFIG_MAPPING_NAMING_STRATEGY_SNAKE_CASE;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.CONFIG_MAPPING_NAMING_STRATEGY_VERBATIM;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.WITH_DEFAULT_ANNOTATION;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.WITH_DEFAULT_ANNOTATION_VALUE;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.WITH_NAME_ANNOTATION;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.WITH_NAME_ANNOTATION_VALUE;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.WITH_PARENT_NAME_ANNOTATION;
import static io.quarkus.runtime.util.StringUtil.camelHumpsIterator;
import static io.quarkus.runtime.util.StringUtil.hyphenate;
import static io.quarkus.runtime.util.StringUtil.join;
import static io.quarkus.runtime.util.StringUtil.lowerCase;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.hasAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.findType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getEnclosedType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getPropertyType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getResolvedResultTypeName;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceMethod;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.isOptional;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.isPrimitiveType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.jdt.core.AbstractAnnotationTypeReferencePropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.IPropertiesCollector;
import org.eclipse.lsp4mp.jdt.core.SearchContext;

import com.redhat.microprofile.jdt.quarkus.JDTQuarkusUtils;

/**
 * Properties provider to collect Quarkus properties from the Java classes or
 * interfaces annotated with "io.smallrye.config.ConfigMapping" annotation.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/config-mappings
 */
public class QuarkusConfigMappingProvider extends AbstractAnnotationTypeReferencePropertiesProvider {

	private static final Logger LOGGER = Logger.getLogger(QuarkusConfigMappingProvider.class.getName());

	private static final String[] ANNOTATION_NAMES = { CONFIG_MAPPING_ANNOTATION };

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation annotation, String annotationName,
			SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		processConfigMapping(javaElement, annotation, context.getCollector(), monitor);
	}

	// ------------- Process Quarkus ConfigMapping -------------

	private void processConfigMapping(IJavaElement javaElement, IAnnotation configMappingAnnotation,
			IPropertiesCollector collector, IProgressMonitor monitor) throws JavaModelException {
		if (javaElement.getElementType() != IJavaElement.TYPE) {
			return;
		}
		IType configMappingType = (IType) javaElement;
		if (!configMappingType.isInterface()) {
			// @ConfigMapping can be used only with interfaces.
			return;
		}
		// Location (JAR, src)
		IPackageFragmentRoot packageRoot = (IPackageFragmentRoot) javaElement
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		String location = packageRoot.getPath().toString();
		// Quarkus Extension name
		String extensionName = JDTQuarkusUtils.getExtensionName(location);

		String prefix = getPrefixFromAnnotation(configMappingAnnotation);
		if (prefix == null || prefix.trim().isEmpty()) {
			// @ConfigMapping has no prefix
			return;
		}
		// @ConfigMapping(prefix="server") case
		List<IType> allInterfaces = new ArrayList<>(Arrays.asList(findInterfaces(configMappingType, monitor)));
		allInterfaces.add(0, configMappingType);
		for (IType configMappingInterface : allInterfaces) {
			populateConfigObject(configMappingInterface, prefix, extensionName, new HashSet<>(),
					configMappingAnnotation, collector, monitor);
		}
	}

	private static IType[] findInterfaces(IType type, IProgressMonitor progressMonitor) throws JavaModelException {
		ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(progressMonitor);
		return typeHierarchy.getAllSuperInterfaces(type);
	}

	private void populateConfigObject(IType configMappingType, String prefixStr, String extensionName,
			Set<IType> typesAlreadyProcessed, IAnnotation configMappingAnnotation, IPropertiesCollector collector,
			IProgressMonitor monitor) throws JavaModelException {
		if (typesAlreadyProcessed.contains(configMappingType)) {
			return;
		}
		typesAlreadyProcessed.add(configMappingType);
		IJavaElement[] elements = configMappingType.getChildren();
		// Loop for each methods
		for (IJavaElement child : elements) {
			if (child.getElementType() == IJavaElement.METHOD) {
				IMethod method = (IMethod) child;
				if (Flags.isDefaultMethod(method.getFlags()) || method.getNumberOfParameters() > 0
						|| "void".equals(method.getReturnType())) {
					continue;
				}

				String returnTypeSignature = method.getReturnType();
				String resolvedTypeSignature = getResolvedResultTypeName(method);
				if (isOptional(resolvedTypeSignature)) {
					// it's an optional type
					// Optional<List<String>> databases();
					// extract the type List<String>
					returnTypeSignature = org.eclipse.jdt.core.Signature.getTypeArguments(returnTypeSignature)[0];
					resolvedTypeSignature = JavaModelUtil.getResolvedTypeName(returnTypeSignature, configMappingType);
				}

				IType returnType = findType(method.getJavaProject(), resolvedTypeSignature);
				boolean simpleType = isSimpleType(resolvedTypeSignature, returnType);
				if (!simpleType) {
					if (returnType != null && !returnType.isInterface()) {
						// When type is not an interface, it requires Converters
						// ex :
						// interface Server {Log log; class Log {}}
						// throw the error;
						// java.lang.IllegalArgumentException: SRCFG00013: No Converter registered for
						// class org.acme.Server2$Log
						// at
						// io.smallrye.config.SmallRyeConfig.requireConverter(SmallRyeConfig.java:466)
						// at
						// io.smallrye.config.ConfigMappingContext.getConverter(ConfigMappingContext.java:113)
						continue;
					}
				}

				String defaultValue = getWithDefault(method);
				String propertyName = getPropertyName(method, prefixStr, configMappingAnnotation);

				// Method result type
				String type = getPropertyType(returnType, resolvedTypeSignature);

				// TODO: extract Javadoc from Java sources
				String description = null;

				// Method source
				String sourceType = getSourceType(method);
				String sourceMethod = getSourceMethod(method);

				// Enumerations
				IType enclosedType = getEnclosedType(returnType, resolvedTypeSignature, method.getJavaProject());
				super.updateHint(collector, enclosedType);

				if (!simpleType) {
					if (isMap(returnType, resolvedTypeSignature)) {
						// Map<String, String>
						propertyName += ".{*}";
						simpleType = true;
					} else if (isCollection(returnType, resolvedTypeSignature)) {
						// List<String>, List<App>
						propertyName += "[*]"; // Generate indexed property.
						String genericTypeName = org.eclipse.jdt.core.Signature
								.getTypeArguments(returnTypeSignature)[0];
						resolvedTypeSignature = JavaModelUtil.getResolvedTypeName(genericTypeName, configMappingType);
						returnType = findType(method.getJavaProject(), resolvedTypeSignature);
						simpleType = isSimpleType(resolvedTypeSignature, returnType);
					}
				}

				if (simpleType) {
					// String, int, etc
					ItemMetadata metadata = super.addItemMetadata(collector, propertyName, type, description,
							sourceType, null, sourceMethod, defaultValue, extensionName, method.isBinary());
					JDTQuarkusUtils.updateConverterKinds(metadata, method, enclosedType);
				} else {
					// Other type (App, etc)
					populateConfigObject(returnType, propertyName, extensionName, typesAlreadyProcessed,
							configMappingAnnotation, collector, monitor);
				}
			}
		}
	}

	private boolean isSimpleType(String resolvedTypeSignature, IType returnType) {
		return returnType == null || isPrimitiveType(resolvedTypeSignature);
	}

	private static boolean isMap(IType type, String typeName) {
		// Fast check
		if (typeName.startsWith("java.util.Map") || typeName.startsWith("java.util.SortedMap")) {
			return true;
		}
		// TODO : check if type extends Map
		return false;
	}

	private static boolean isCollection(IType type, String typeName) {
		// Fast check
		if (typeName.startsWith("java.util.Collection") || typeName.startsWith("java.util.Set")
				|| typeName.startsWith("java.util.SortedSet") || typeName.startsWith("java.util.List")) {
			return true;
		}
		// TODO : check if type extends Collection
		return false;
	}

	private String getPropertyName(IMember member, String prefix, IAnnotation configMappingAnnotation)
			throws JavaModelException {
		if (hasAnnotation((IAnnotatable) member, WITH_PARENT_NAME_ANNOTATION)) {
			return prefix;
		}
		return prefix + "." + convertName(member, configMappingAnnotation);
	}

	private static String convertName(IMember member, IAnnotation configMappingAnnotation) throws JavaModelException {
		// 1) Check if @WithName is used
		// @WithName("name")
		// String host();
		// --> See https://quarkus.io/guides/config-mappings#withname
		IAnnotation withNameAnnotation = getAnnotation((IAnnotatable) member, WITH_NAME_ANNOTATION);
		if (withNameAnnotation != null) {
			String name = getAnnotationMemberValue(withNameAnnotation, WITH_NAME_ANNOTATION_VALUE);
			if (StringUtils.isNotEmpty(name)) {
				return name;
			}
		}

		String name = member.getElementName();

		// 2) Check if ConfigMapping.NamingStrategy is used
		// @ConfigMapping(prefix = "server", namingStrategy =
		// ConfigMapping.NamingStrategy.VERBATIM)
		// public interface ServerVerbatimNamingStrategy
		// --> See https://quarkus.io/guides/config-mappings#namingstrategy
		String namingStrategy = getAnnotationMemberValue(configMappingAnnotation,
				CONFIG_MAPPING_ANNOTATION_NAMING_STRATEGY);
		if (namingStrategy != null) {
			int index = namingStrategy.lastIndexOf('.');
			if (index != -1) {
				namingStrategy = namingStrategy.substring(index + 1, namingStrategy.length());
			}
			switch (namingStrategy) {
			case CONFIG_MAPPING_NAMING_STRATEGY_VERBATIM:
				// The method name is used as is to map the configuration property.
				return name;
			case CONFIG_MAPPING_NAMING_STRATEGY_SNAKE_CASE:
				// The method name is derived by replacing case changes with an underscore to
				// map the configuration property.
				return snake(name);
			default:
				// KEBAB_CASE
				// The method name is derived by replacing case changes with a dash to map the
				// configuration property.
				return hyphenate(name);
			}
		}

		// None namingStrategy, use KEBAB_CASE as default
		return hyphenate(name);
	}

	/**
	 * Returns the value of @WithDefault("a value") and null otherwise.
	 * 
	 * @param member the filed, method which is annotated with @WithDefault. s
	 * @return the value of @WithDefault("a value") and null otherwise.
	 */
	private static String getWithDefault(IMember member) {
		try {
			IAnnotation withDefaultAnnotation = getAnnotation((IAnnotatable) member, WITH_DEFAULT_ANNOTATION);
			if (withDefaultAnnotation != null) {
				String defaultValue = getAnnotationMemberValue(withDefaultAnnotation, WITH_DEFAULT_ANNOTATION_VALUE);
				if (StringUtils.isNotEmpty(defaultValue)) {
					return defaultValue;
				}
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting @WithDefault value", e);
		}
		return null;
	}

	private static String getPrefixFromAnnotation(IAnnotation configMappingAnnotation) throws JavaModelException {
		String value = getAnnotationMemberValue(configMappingAnnotation, CONFIG_MAPPING_ANNOTATION_PREFIX);
		if (value == null || value.isEmpty()) {
			return null;
		}
		return value;
	}

	private static String snake(String orig) {
		return join("_", lowerCase(camelHumpsIterator(orig)));
	}
}
