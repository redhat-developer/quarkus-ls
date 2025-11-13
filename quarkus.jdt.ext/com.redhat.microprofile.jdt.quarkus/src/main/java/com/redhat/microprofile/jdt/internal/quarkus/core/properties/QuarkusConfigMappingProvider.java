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
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.WITH_DEFAULT_ANNOTATION;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.WITH_DEFAULT_ANNOTATION_VALUE;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.WITH_NAME_ANNOTATION;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.WITH_NAME_ANNOTATION_VALUE;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.WITH_PARENT_NAME_ANNOTATION;
import static io.quarkus.runtime.util.StringUtil.camelHumpsIterator;
import static io.quarkus.runtime.util.StringUtil.hyphenate;
import static io.quarkus.runtime.util.StringUtil.join;
import static io.quarkus.runtime.util.StringUtil.lowerCase;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getFirstAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.hasAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.findType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getEnclosedType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getPropertyType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getResolvedResultTypeName;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceMethod;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.isOptional;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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
		Set<IType> allInterfaces = findInterfaces(configMappingType, monitor);
		for (IType configMappingInterface : allInterfaces) {
			populateConfigObject(configMappingInterface, prefix, extensionName, new HashSet<>(),
					configMappingAnnotation, collector, monitor);
		}
	}

	private static Set<IType> findInterfaces(IType type, IProgressMonitor progressMonitor) throws JavaModelException {
		// No reason to use a JDK interface to generate a config class? Primarily to fix
		// the java.nio.file.Path case.
		// see
		// https://github.com/smallrye/smallrye-config/blob/22635f24dc7634706867cc52e28d5bd82d15f54e/implementation/src/main/java/io/smallrye/config/ConfigMappingInterface.java#L782C9-L783C58
		if (type.getFullyQualifiedName() == null || type.getFullyQualifiedName().startsWith("java")) {
			return Collections.emptySet();
		}
		Set<IType> result = new HashSet<>();
		result.add(type);

		ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(progressMonitor);
		IType[] allSuperInterfaces = typeHierarchy.getAllSuperInterfaces(type);
		result.addAll(Arrays.asList(allSuperInterfaces));
		return result;
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
						|| "void".equals(method.getReturnType()) || "<clinit>".equals(method.getElementName())) {
					continue;
				}

				String returnTypeSignature = method.getReturnType();
				String resolvedTypeSignature = getResolvedResultTypeName(method);
				if (isOptional(resolvedTypeSignature)) {
					// it's an optional type
					// Optional<List<String>> databases();
					// extract the type List<String>
					String[] typeArguments = org.eclipse.jdt.core.Signature.getTypeArguments(returnTypeSignature);
					if (typeArguments.length < 1) {
						continue;
					}
					returnTypeSignature = typeArguments[0];
					resolvedTypeSignature = JavaModelUtil.getResolvedTypeName(returnTypeSignature, configMappingType);
				}

				IType returnType = findType(method.getJavaProject(), resolvedTypeSignature);
				boolean leafType = isLeafType(returnType);

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

				if (!leafType) {
					if (isMap(returnType, resolvedTypeSignature)) {
						// Map<String, String>
						// Map<String, SomeConfig>
						propertyName += ".{*}";
						String[] typeArguments = org.eclipse.jdt.core.Signature.getTypeArguments(returnTypeSignature);
						if (typeArguments.length > 1) {
							String genericTypeName = typeArguments[1];
							resolvedTypeSignature = JavaModelUtil.getResolvedTypeName(genericTypeName,
									configMappingType);
							returnType = findType(method.getJavaProject(), resolvedTypeSignature);
							leafType = isLeafType(returnType);
						} else {
							leafType = false;
						}

					} else if (isCollection(returnType, resolvedTypeSignature)) {
						// List<String>, List<App>
						propertyName += "[*]"; // Generate indexed property.
						String[] typeArguments = org.eclipse.jdt.core.Signature.getTypeArguments(returnTypeSignature);
						if (typeArguments.length > 0) {
							String genericTypeName = typeArguments[0];
							resolvedTypeSignature = JavaModelUtil.getResolvedTypeName(genericTypeName,
									configMappingType);
							returnType = findType(method.getJavaProject(), resolvedTypeSignature);
							leafType = isLeafType(returnType);
						} else {
							leafType = false;
						}
					}
				}

				if (leafType) {
					// String, int, Optional, or Class (not interface)
					ItemMetadata metadata = super.addItemMetadata(collector, propertyName, type, description,
							sourceType, null, sourceMethod, defaultValue, extensionName, method.isBinary());
					JDTQuarkusUtils.updateConverterKinds(metadata, method, enclosedType);
				} else {
					// Other type (App interface, etc)
					Set<IType> allInterfaces = findInterfaces(returnType, monitor);
					for (IType configMappingInterface : allInterfaces) {
						populateConfigObject(configMappingInterface, propertyName, extensionName, typesAlreadyProcessed,
								configMappingAnnotation, collector, monitor);
					}
				}
			}
		}
	}

	/**
	 * Returns true if the given return type should be treated as a leaf in the
	 * configuration tree, i.e. it is null or not an interface, and therefore not
	 * recursively visited.
	 * 
	 * @throws JavaModelException
	 */
	private static boolean isLeafType(IType returnType) throws JavaModelException {
		return returnType == null || !returnType.isInterface();
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
		IAnnotation withNameAnnotation = getFirstAnnotation((IAnnotatable) member, WITH_NAME_ANNOTATION);
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
		NamingStrategy namingStrategy = getNamingStrategy(configMappingAnnotation);
		if (namingStrategy != null) {
			switch (namingStrategy) {
			case VERBATIM:
				// The method name is used as is to map the configuration property.
				return name;
			case SNAKE_CASE:
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
	 * Returns the Quarkus @ConfigMapping(namingStrategy=...) value.
	 *
	 * @param configMappingAnnotation
	 * @return the Quarkus @ConfigMapping(namingStrategy=...) value.
	 * @throws JavaModelException
	 */
	private static NamingStrategy getNamingStrategy(IAnnotation configMappingAnnotation) throws JavaModelException {
		// 2) Check if ConfigMapping.NamingStrategy is used
		// @ConfigMapping(prefix = "server", namingStrategy =
		// ConfigMapping.NamingStrategy.VERBATIM)
		// public interface ServerVerbatimNamingStrategy
		// --> See https://quarkus.io/guides/config-mappings#namingstrategy
		String namingStrategy = getAnnotationMemberValue(configMappingAnnotation,
				CONFIG_MAPPING_ANNOTATION_NAMING_STRATEGY);
		if (namingStrategy != null) {
			try {
				return NamingStrategy.valueOf(namingStrategy.toUpperCase());
			} catch (Exception e) {

			}
		}
		return null;
	}

	/**
	 * Returns the value of @WithDefault("a value") and null otherwise.
	 * 
	 * @param member the filed, method which is annotated with @WithDefault. s
	 * @return the value of @WithDefault("a value") and null otherwise.
	 */
	private static String getWithDefault(IMember member) {
		try {
			IAnnotation withDefaultAnnotation = getFirstAnnotation((IAnnotatable) member, WITH_DEFAULT_ANNOTATION);
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
