/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.core;

import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.CONFIG_GROUP_ANNOTATION;
import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.CONFIG_ITEM_ANNOTATION;
import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.CONFIG_PROPERTIES_ANNOTATION;
import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.CONFIG_PROPERTY_ANNOTATION;
import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.CONFIG_ROOT_ANNOTATION;
import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.QUARKUS_JAVADOC_PROPERTIES;
import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.QUARKUS_PREFIX;
import static io.quarkus.runtime.util.StringUtil.camelHumpsIterator;
import static io.quarkus.runtime.util.StringUtil.hyphenate;
import static io.quarkus.runtime.util.StringUtil.join;
import static io.quarkus.runtime.util.StringUtil.lowerCase;
import static io.quarkus.runtime.util.StringUtil.lowerCaseFirst;
import static io.quarkus.runtime.util.StringUtil.withoutSuffix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import com.redhat.quarkus.commons.ClasspathKind;
import com.redhat.quarkus.commons.EnumItem;
import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.commons.QuarkusPropertiesScope;
import com.redhat.quarkus.jdt.internal.core.QuarkusDeploymentJavaProject;
import com.redhat.quarkus.jdt.internal.core.utils.JDTQuarkusSearchUtils;
import com.redhat.quarkus.jdt.internal.core.utils.JDTQuarkusUtils;

import io.quarkus.arc.config.ConfigProperties;
import io.quarkus.deployment.bean.JavaBeanUtil;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;

/**
 * JDT Quarkus manager.
 * 
 * @author Angelo ZERR
 * @see https://quarkus.io/guides/extension-authors-guide#configuration
 * @see https://quarkus.io/guides/application-configuration-guide
 */
public class JDTQuarkusManager {

	private static final JDTQuarkusManager INSTANCE = new JDTQuarkusManager();

	private static final Logger LOGGER = Logger.getLogger(JDTQuarkusManager.class.getName());

	private static final List<String> NUMBER_TYPES = Arrays.asList("short", "int", "long", "double", "float");

	public static JDTQuarkusManager getInstance() {
		return INSTANCE;
	}

	private JDTQuarkusManager() {

	}

	/**
	 * Returns the Java field from the given property source
	 * 
	 * @param file           the application.properties file
	 * @param propertySource the property source to find
	 * @param progress       the progress monitor.
	 * @return the Java field from the given property source
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	public IMember findDeclaredQuarkusProperty(IFile file, String propertySource, IProgressMonitor progress)
			throws JavaModelException, CoreException {
		String projectName = file.getProject().getName();
		IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
		return findDeclaredQuarkusProperty(javaProject, propertySource, progress);
	}

	/**
	 * Returns the Java field from the given property source
	 * 
	 * @param javaProject    the Java project
	 * @param propertySource the property source to find
	 * @param progress       the progress monitor.
	 * @return the Java field from the given property sources
	 * @throws JavaModelException
	 */
	public IMember findDeclaredQuarkusProperty(IJavaProject javaProject, String propertySource,
			IProgressMonitor progress) throws JavaModelException {
		int index = propertySource.indexOf('#');
		if (index == -1) {
			return null;
		}
		String className = propertySource.substring(0, index);
		// Try to find type with standard classpath
		IType type = javaProject.findType(className, progress);
		if (type == null) {
			// Not found, type could be included in deployment JAR which is not in classpath
			// Try to find type from deployment JAR
			type = new QuarkusDeploymentJavaProject(javaProject, QuarkusDeploymentJavaProject.DEFAULT_ARTIFACT_RESOLVER,
					false).findType(className, progress);
		}
		String fieldOrMethodName = propertySource.substring(index + 1, propertySource.length());
		int startBracketIndex = fieldOrMethodName.indexOf('(');
		if (startBracketIndex == -1) {
			// it's a field
			return type.getField(fieldOrMethodName);
		}
		String methodName = fieldOrMethodName.substring(0, startBracketIndex);
		// Method signature has been generated with JDT API, so we are sure that we have
		// a ')' character.
		int endBracketIndex = fieldOrMethodName.indexOf(')');
		String methodSignature = fieldOrMethodName.substring(startBracketIndex + 1, endBracketIndex);
		String[] paramTypes = methodSignature.isEmpty() ? CharOperation.NO_STRINGS
				: Signature.getParameterTypes(methodSignature);
		return JavaModelUtil.findMethod(methodName, paramTypes, false, type);
	}

	public QuarkusProjectInfo getQuarkusProjectInfo(IFile file, QuarkusPropertiesScope propertiesScope,
			DocumentationConverter converter, IProgressMonitor progress) throws JavaModelException, CoreException {
		String projectName = file.getProject().getName();
		IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
		ClasspathKind classpathKind = JDTQuarkusUtils.getClasspathKind(file, javaProject);
		return getQuarkusProjectInfo(javaProject, propertiesScope, converter, classpathKind, progress);
	}

	/**
	 * Returns the Quarkus project information for the given Eclipse
	 * <code>projectName</code>. This method
	 * 
	 * <ul>
	 * <li>scan all classes from the project annotated with @ConfigRoot and compute
	 * Quarkus properties.</li>
	 * </ul>
	 * 
	 * <p>
	 * The compute of Quarkus property name follow the same logic than
	 * https://github.com/quarkusio/quarkus/blob/master/core/deployment/src/main/java/io/quarkus/deployment/configuration/ConfigDefinition.java
	 * </p>
	 * 
	 * @param projectName     the Eclipse project name
	 * @param propertiesScope the search scope
	 * @param converter       the documentation converter to convert JavaDoc in a
	 *                        format like Markdown
	 * @param monitor         the progress monitor
	 * @return the Quarkus project information for the given Eclipse
	 *         <code>projectName</code>.
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	public QuarkusProjectInfo getQuarkusProjectInfo(IJavaProject javaProject, QuarkusPropertiesScope propertiesScope,
			DocumentationConverter converter, ClasspathKind classpathKind, IProgressMonitor monitor)
			throws JavaModelException, CoreException {
		QuarkusProjectInfo info = createInfo(javaProject, classpathKind);
		if (classpathKind == ClasspathKind.NONE) {
			info.setProperties(Collections.emptyList());
			return info;
		}
		long startTime = System.currentTimeMillis();
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Start computing Quarkus properties for '" + info.getProjectURI() + "' project.");
		}
		try {
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties = new ArrayList<>();
			Map<IPackageFragmentRoot, Properties> javadocCache = new HashMap<>();

			// Scan Quarkus annotations Config* by using Java eclipse search engine.
			SearchPattern pattern = JDTQuarkusSearchUtils.createQuarkusConfigSearchPattern();
			SearchEngine engine = new SearchEngine();
			IJavaSearchScope scope = JDTQuarkusSearchUtils.createQuarkusSearchScope(javaProject, propertiesScope,
					classpathKind == ClasspathKind.SRC);
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
					new SearchRequestor() {

						@Override
						public void acceptSearchMatch(SearchMatch match) throws CoreException {
							Object element = match.getElement();
							if (element instanceof IAnnotatable && element instanceof IJavaElement) {
								IJavaElement javaElement = (IJavaElement) element;
								processQuarkusAnnotation(javaElement, converter, javadocCache, quarkusProperties,
										monitor);
							}
						}
					}, monitor);
			info.setProperties(quarkusProperties);
		} finally {
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("End computing Quarkus properties for '" + info.getProjectURI() + "' project in "
						+ (System.currentTimeMillis() - startTime) + "ms.");
			}
		}
		return info;
	}

	private static QuarkusProjectInfo createInfo(IJavaProject javaProject, ClasspathKind classpathKind) {
		QuarkusProjectInfo info = new QuarkusProjectInfo();
		info.setProjectURI(JDTQuarkusUtils.getProjectURI(javaProject));
		info.setClasspathKind(classpathKind);
		return info;
	}

	/**
	 * Process Quarkus Config* annotation from the given <code>javaElement</code>.
	 *
	 * @param javaElement       the class, field element which have a Quarkus
	 *                          Config* annotations.
	 * @param converter         the documentation converter to use.
	 * @param javadocCache      javadoc cache
	 * @param quarkusProperties the properties to fill.
	 * @param monitor           the progress monitor.
	 * @throws JavaModelException
	 */
	private void processQuarkusAnnotation(IJavaElement javaElement, DocumentationConverter converter,
			Map<IPackageFragmentRoot, Properties> javadocCache,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties, IProgressMonitor monitor) {
		try {
			IAnnotation[] annotations = ((IAnnotatable) javaElement).getAnnotations();
			for (IAnnotation annotation : annotations) {
				if (isMatchAnnotation(annotation, CONFIG_PROPERTY_ANNOTATION)) {
					processConfigProperty(javaElement, annotation, converter, javadocCache, quarkusProperties, monitor);
				} else if (isMatchAnnotation(annotation, CONFIG_ROOT_ANNOTATION)) {
					processConfigRoot(javaElement, annotation, converter, javadocCache, quarkusProperties, monitor);
				} else if (isMatchAnnotation(annotation, CONFIG_PROPERTIES_ANNOTATION)) {
					processConfigProperties(javaElement, annotation, converter, javadocCache, quarkusProperties,
							monitor);
				}
			}
		} catch (Exception e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, "Cannot compute Quarkus properties for the Java element '"
						+ javaElement.getElementName() + "'.", e);
			}
		}
	}

	// ------------- Process Quarkus ConfigProperty -------------

	/**
	 * Process Quarkus ConfigProperty annotation from the given
	 * <code>javaElement</code>.
	 * 
	 * @param javaElement              the class, field element which have a Quarkus
	 *                                 ConfigProperty annotations.
	 * @param configPropertyAnnotation the Quarkus ConfigProperty annotation.
	 * @param converter                the documentation converter to use.
	 * @param javadocCache
	 * @param quarkusProperties        the properties to fill.
	 * @param monitor                  the progress monitor.
	 * @throws JavaModelException
	 */
	private static void processConfigProperty(IJavaElement javaElement, IAnnotation configPropertyAnnotation,
			DocumentationConverter converter, Map<IPackageFragmentRoot, Properties> javadocCache,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties, IProgressMonitor monitor)
			throws JavaModelException {
		if (javaElement.getElementType() == IJavaElement.FIELD) {
			String propertyName = getAnnotationMemberValue(configPropertyAnnotation, "name");
			if (propertyName != null && !propertyName.isEmpty()) {
				IField field = (IField) javaElement;
				String fieldTypeName = getResolvedTypeName(field);
				IType fieldClass = findType(field.getJavaProject(), fieldTypeName);
				String defaultValue = getAnnotationMemberValue(configPropertyAnnotation, "defaultValue");
				// Location (JAR, src)
				IPackageFragmentRoot packageRoot = (IPackageFragmentRoot) javaElement
						.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
				String location = packageRoot.getPath().toString();
				// Quarkus Extension name
				String extensionName = JDTQuarkusUtils.getExtensionName(location);

				addField(location, extensionName, field, fieldTypeName, fieldClass, propertyName, defaultValue,
						converter, javadocCache, null, quarkusProperties, monitor);
			}
		}
	}

	// ------------- Process Quarkus ConfigRoot -------------

	/**
	 * Process Quarkus ConfigRoot annotation from the given
	 * <code>javaElement</code>.
	 * 
	 * @param javaElement          the class, field element which have a Quarkus
	 *                             ConfigRoot annotations.
	 * @param configRootAnnotation the Quarkus ConfigRoot annotation.
	 * @param converter            the documentation converter to use.
	 * @param javadocCache
	 * @param quarkusProperties    the properties to fill.
	 * @param monitor              the progress monitor.
	 */
	private static void processConfigRoot(IJavaElement javaElement, IAnnotation configRootAnnotation,
			DocumentationConverter converter, Map<IPackageFragmentRoot, Properties> javadocCache,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties, IProgressMonitor monitor)
			throws JavaModelException {
		ConfigPhase configPhase = getConfigPhase(configRootAnnotation);
		String configRootAnnotationName = getConfigRootName(configRootAnnotation);
		String extension = getExtensionName(getSimpleName(javaElement), configRootAnnotationName, configPhase);
		if (extension == null) {
			return;
		}
		// Location (JAR, src)
		IPackageFragmentRoot packageRoot = (IPackageFragmentRoot) javaElement
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		String location = packageRoot.getPath().toString();
		// Quarkus Extension name
		String extensionName = JDTQuarkusUtils.getExtensionName(location);

		String baseKey = QUARKUS_PREFIX + extension;
		processConfigGroup(location, extensionName, javaElement, baseKey, configPhase, converter, javadocCache,
				quarkusProperties, monitor);
	}

	/**
	 * Returns the Quarkus @ConfigRoot(phase=...) value.
	 * 
	 * @param configRootAnnotation
	 * @return the Quarkus @ConfigRoot(phase=...) value.
	 * @throws JavaModelException
	 */
	private static ConfigPhase getConfigPhase(IAnnotation configRootAnnotation) throws JavaModelException {
		String value = getAnnotationMemberValue(configRootAnnotation, "phase");
		if (value != null) {
			if (value.endsWith(ConfigPhase.RUN_TIME.name())) {
				return ConfigPhase.RUN_TIME;
			}
			if (value.endsWith(ConfigPhase.BUILD_AND_RUN_TIME_FIXED.name())) {
				return ConfigPhase.BUILD_AND_RUN_TIME_FIXED;
			}
		}
		return ConfigPhase.BUILD_TIME;
	}

	/**
	 * Returns the Quarkus @ConfigRoot(name=...) value and
	 * {@link ConfigItem#HYPHENATED_ELEMENT_NAME} otherwise.
	 * 
	 * @param configRootAnnotation @ConfigRoot annotation
	 * @return the Quarkus @ConfigRoot(name=...) value and
	 *         {@link ConfigItem#HYPHENATED_ELEMENT_NAME} otherwise.
	 * @throws JavaModelException
	 */
	private static String getConfigRootName(IAnnotation configRootAnnotation) throws JavaModelException {
		String value = getAnnotationMemberValue(configRootAnnotation, "name");
		if (value != null) {
			return value;
		}
		return ConfigItem.HYPHENATED_ELEMENT_NAME;
	}

	/**
	 * Returns the simple name of the given <code>javaElement</code>
	 * 
	 * @param javaElement the Java class element
	 * @return the simple name of the given <code>javaElement</code>
	 */
	private static String getSimpleName(IJavaElement javaElement) {
		String elementName = javaElement.getElementName();
		int index = elementName.lastIndexOf('.');
		return index != -1 ? elementName.substring(index + 1, elementName.length()) : elementName;
	}

	/**
	 * Returns the Quarkus extension name according the
	 * <code>configRootClassSimpleName</code>, <code>configRootAnnotationName</code>
	 * and <code>configPhase</code>.
	 * 
	 * @param configRootClassSimpleName the simple class name where ConfigRoot is
	 *                                  declared.
	 * @param configRootAnnotationName  the name declared in the ConfigRoot
	 *                                  annotation.
	 * @param configPhase               the config phase.
	 * @see https://github.com/quarkusio/quarkus/blob/master/core/deployment/src/main/java/io/quarkus/deployment/configuration/ConfigDefinition.java#L173
	 *      (registerConfigRoot)
	 * @return the Quarkus extension name according the
	 *         <code>configRootClassSimpleName</code>,
	 *         <code>configRootAnnotationName</code> and <code>configPhase</code>.
	 */
	private static String getExtensionName(String configRootClassSimpleName, String configRootAnnotationName,
			ConfigPhase configPhase) {
		// See
		// https://github.com/quarkusio/quarkus/blob/master/core/deployment/src/main/java/io/quarkus/deployment/configuration/ConfigDefinition.java#L173
		// registerConfigRoot
		final String containingName;
		if (configPhase == ConfigPhase.RUN_TIME) {
			containingName = join(withoutSuffix(lowerCaseFirst(camelHumpsIterator(configRootClassSimpleName)), "Config",
					"Configuration", "RunTimeConfig", "RunTimeConfiguration"));
		} else {
			containingName = join(withoutSuffix(lowerCaseFirst(camelHumpsIterator(configRootClassSimpleName)), "Config",
					"Configuration", "BuildTimeConfig", "BuildTimeConfiguration"));
		}
		final String name = configRootAnnotationName;
		final String rootName;
		if (name.equals(ConfigItem.PARENT)) {
			// throw reportError(configRoot, "Root cannot inherit parent name because it has
			// no parent");
			return null;
		} else if (name.equals(ConfigItem.ELEMENT_NAME)) {
			rootName = containingName;
		} else if (name.equals(ConfigItem.HYPHENATED_ELEMENT_NAME)) {
			rootName = join("-",
					withoutSuffix(lowerCase(camelHumpsIterator(configRootClassSimpleName)), "config", "configuration"));
		} else {
			rootName = name;
		}
		return rootName;
	}

	// ------------- Process Quarkus ConfigGroup -------------

	/**
	 * Process Quarkus ConfigGroup annotation from the given
	 * <code>javaElement</code>.
	 * 
	 * @param extensionName     the Quarkus extension name
	 * @param location          the JAR/src location
	 * 
	 * @param javaElement       the class, field element which have a Quarkus
	 *                          ConfigGroup annotations.
	 * @param baseKey           the base key
	 * @param configPhase       the phase
	 * @param converter         the documentation converter to use.
	 * @param javadocCache      the Javadoc cache
	 * @param quarkusProperties the properties to fill.
	 * @param monitor           the progress monitor.
	 * @throws JavaModelException
	 */
	private static void processConfigGroup(final String location, String extensionName, IJavaElement javaElement,
			String baseKey, ConfigPhase configPhase, DocumentationConverter converter,
			Map<IPackageFragmentRoot, Properties> javadocCache,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties, IProgressMonitor monitor)
			throws JavaModelException {
		if (javaElement.getElementType() == IJavaElement.TYPE) {
			IJavaElement[] elements = ((IType) javaElement).getChildren();
			for (IJavaElement child : elements) {
				if (child.getElementType() == IJavaElement.FIELD) {
					IField field = (IField) child;
					final IAnnotation configItemAnnotation = getAnnotation((IAnnotatable) field,
							CONFIG_ITEM_ANNOTATION);
					String name = configItemAnnotation == null ? hyphenate(field.getElementName())
							: getAnnotationMemberValue(configItemAnnotation, "name");
					if (name == null) {
						name = ConfigItem.HYPHENATED_ELEMENT_NAME;
					}
					String subKey;
					boolean consume;
					if (name.equals(ConfigItem.PARENT)) {
						subKey = baseKey;
						consume = false;
					} else if (name.equals(ConfigItem.ELEMENT_NAME)) {
						subKey = baseKey + "." + field.getElementName();
						consume = true;
					} else if (name.equals(ConfigItem.HYPHENATED_ELEMENT_NAME)) {
						subKey = baseKey + "." + hyphenate(field.getElementName());
						consume = true;
					} else {
						subKey = baseKey + "." + name;
						consume = true;
					}
					final String defaultValue = configItemAnnotation == null ? ConfigItem.NO_DEFAULT
							: getAnnotationMemberValue(configItemAnnotation, "defaultValue");

					String fieldTypeName = getResolvedTypeName(field);
					IType fieldClass = findType(field.getJavaProject(), fieldTypeName);
					final IAnnotation configGroupAnnotation = getAnnotation((IAnnotatable) fieldClass,
							CONFIG_GROUP_ANNOTATION);
					if (configGroupAnnotation != null) {
						processConfigGroup(location, extensionName, fieldClass, subKey, configPhase, converter,
								javadocCache, quarkusProperties, monitor);
					} else {
						addField(location, extensionName, field, fieldTypeName, fieldClass, subKey, defaultValue,
								converter, javadocCache, configPhase, quarkusProperties, monitor);
					}
				}
			}
		}
	}

	// ------------- Process Quarkus ConfigProperties -------------

	private static void processConfigProperties(IJavaElement javaElement, IAnnotation configPropertiesAnnotation,
			DocumentationConverter converter, Map<IPackageFragmentRoot, Properties> javadocCache,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties, IProgressMonitor monitor)
			throws JavaModelException {
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
						IAnnotation configPropertyAnnotation = getAnnotation(method, CONFIG_PROPERTY_ANNOTATION);
						if (configPropertyAnnotation != null) {
							name = getAnnotationMemberValue(configPropertyAnnotation, "name");
							defaultValue = getAnnotationMemberValue(configPropertyAnnotation, "defaultValue");
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
						String type = returnType != null ? returnType.getFullyQualifiedName() : methodResultTypeName;

						// TODO: extract Javadoc from Java sources
						String docs = null;

						// Method source
						String source = method.getDeclaringType().getFullyQualifiedName() + "#"
								+ method.getElementName() + method.getSignature();

						// Enumerations
						List<EnumItem> enumerations = getEnumerations(returnType);

						if (isSimpleFieldType(returnType, methodResultTypeName)) {
							addField(propertyName, type, defaultValue, docs, location, extensionName, source,
									enumerations, null, quarkusProperties);
						} else {
							populateConfigObject(returnType, propertyName, location, extensionName, new HashSet<>(),
									quarkusProperties, monitor);
						}

					}
				}
			}
		} else {
			// See
			// https://github.com/quarkusio/quarkus/blob/e8606513e1bd14f0b1aaab7f9969899bd27c55a3/extensions/arc/deployment/src/main/java/io/quarkus/arc/deployment/configproperties/ClassConfigPropertiesUtil.java#L117
			// TODO : validation
			populateConfigObject(configPropertiesType, prefix, location, extensionName, new HashSet<>(),
					quarkusProperties, monitor);
		}
	}

	private static boolean isSimpleFieldType(IType type, String typeName) {
		return type == null || isPrimitiveType(typeName) || isList(typeName) || isMap(typeName) || isOptional(typeName);
	}

	private static IType[] findInterfaces(IType type, IProgressMonitor progressMonitor) throws JavaModelException {
		ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(progressMonitor);
		return typeHierarchy.getAllSuperInterfaces(type);
	}

	private static void populateConfigObject(IType configPropertiesType, String prefixStr, final String location,
			String extensionName, Set<IType> typesAlreadyProcessed,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties, IProgressMonitor monitor)
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
				// Getting "!" value is possible but it requires to reparse the Java file to
				// build a DOM CompilationUnit to extract assigned value.
				final String defaultValue = null;
				String propertyName = prefixStr + "." + name;

				String fieldTypeName = getResolvedTypeName(field);
				IType fieldClass = findType(field.getJavaProject(), fieldTypeName);
				if (isSimpleFieldType(fieldClass, fieldTypeName)) {

					// Class type
					String type = fieldClass != null ? fieldClass.getFullyQualifiedName() : fieldTypeName;

					// Javadoc
					String docs = null;

					// field and class source
					String source = field.getDeclaringType().getFullyQualifiedName() + "#" + field.getElementName();

					// Enumerations
					List<EnumItem> enumerations = getEnumerations(fieldClass);

					addField(propertyName, type, defaultValue, docs, location, extensionName, source, enumerations,
							null, quarkusProperties);
				} else {
					populateConfigObject(fieldClass, propertyName, location, extensionName, typesAlreadyProcessed,
							quarkusProperties, monitor);
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

	private static void addField(String location, String extensionName, IField field, String fieldTypeName,
			IType fieldClass, String propertyName, String defaultValue, DocumentationConverter converter,
			Map<IPackageFragmentRoot, Properties> javadocCache, ConfigPhase configPhase,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties, IProgressMonitor monitor)
			throws JavaModelException {

		// Class type
		String type = fieldClass != null ? fieldClass.getFullyQualifiedName() : fieldTypeName;

		// Javadoc
		String docs = getJavadoc(field, javadocCache, monitor);
		docs = converter.convert(docs);

		// field and class source
		String source = field.getDeclaringType().getFullyQualifiedName() + "#" + field.getElementName();

		// Enumerations
		List<EnumItem> enumerations = getEnumerations(fieldClass);

		// Default value for primitive type
		if ("boolean".equals(fieldTypeName)) {
			addField(propertyName, type, ConfigItem.NO_DEFAULT.equals(defaultValue) ? "false" : defaultValue, docs,
					location, extensionName, source, enumerations, configPhase, quarkusProperties);
		} else if (isNumber(fieldTypeName)) {
			addField(propertyName, type, ConfigItem.NO_DEFAULT.equals(defaultValue) ? "0" : defaultValue, docs,
					location, extensionName, source, enumerations, configPhase, quarkusProperties);
		} else if (isMap(fieldTypeName)) {
			// FIXME: find better mean to check field is a Map
			// this code works only if user uses Map as declaration and not if they declare
			// HashMap for instance
			String[] rawTypeParameters = getRawTypeParameters(fieldTypeName);
			if ((rawTypeParameters[0].trim().equals("java.lang.String"))) {
				// The key Map must be a String
				processMap(field, propertyName, rawTypeParameters[1], docs, location, extensionName, source,
						configPhase, converter, javadocCache, quarkusProperties, monitor);
			}
		} else if (isList(fieldTypeName)) {
			addField(propertyName, type, defaultValue, docs, location, extensionName, source, enumerations, configPhase,
					quarkusProperties);
		} else if (isOptional(fieldTypeName)) {
			ExtendedConfigDescriptionBuildItem item = addField(propertyName, type, defaultValue, docs, location,
					extensionName, source, enumerations, configPhase, quarkusProperties);
			item.setRequired(false);
		} else {
			addField(propertyName, type, defaultValue, docs, location, extensionName, source, enumerations, configPhase,
					quarkusProperties);
		}
	}

	/**
	 * Returns the Javadoc from the given field. There are 3 strategies to extract
	 * the Javadoc:
	 * 
	 * <ul>
	 * <li>try to extract Javadoc from the source (from '.java' source file or from
	 * JAR which is linked to source).</li>
	 * <li>try to get Javadoc from the attached Javadoc.</li>
	 * <li>get Javadoc from the Quarkus properties file stored in JAR META-INF/
	 * </ul>
	 * 
	 * @param field
	 * @param javadocCache
	 * @param monitor
	 * @return
	 * @throws JavaModelException
	 */
	private static String getJavadoc(IField field, Map<IPackageFragmentRoot, Properties> javadocCache,
			IProgressMonitor monitor) throws JavaModelException {
		// TODO: get Javadoc from source anad attached doc by processing Javadoc tag as
		// markdown
		// Try to get javadoc from sources
		/*
		 * String javadoc = findJavadocFromSource(field); if (javadoc != null) { return
		 * javadoc; } // Try to get attached javadoc javadoc =
		 * field.getAttachedJavadoc(monitor); if (javadoc != null) { return javadoc; }
		 */
		// Try to get the javadoc inside the META-INF/quarkus-javadoc.properties of the
		// JAR
		IPackageFragmentRoot packageRoot = (IPackageFragmentRoot) field.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		Properties properties = javadocCache.get(packageRoot);
		if (properties == null) {
			properties = new Properties();
			javadocCache.put(packageRoot, properties);
			IJarEntryResource quarkusJavadocResource = findJavadocFromQuakusJavadocProperties(packageRoot);
			if (quarkusJavadocResource != null) {
				try {
					properties.load(quarkusJavadocResource.getContents());
				} catch (Exception e) {
					// TODO : log it
					e.printStackTrace();
				}
			}
		}
		if (properties.isEmpty()) {
			return null;
		}
		// The META-INF/quarkus-javadoc.properties stores Javadoc without $ . Ex:
		// io.quarkus.deployment.SslProcessor.SslConfig.native_=Enable native SSL
		// support.

		String fieldKey = field.getDeclaringType().getFullyQualifiedName() + "." + field.getElementName();

		// Here field key contains '$'
		// Ex : io.quarkus.deployment.SslProcessor$SslConfig.native_
		// replace '$' with '.'
		fieldKey = fieldKey.replace('$', '.');
		return properties.getProperty(fieldKey);
	}

	private static String findJavadocFromSource(IField field) throws JavaModelException {
		ISourceRange range = field.getJavadocRange();
		if (range != null) {
			int start = range.getOffset() - field.getSourceRange().getOffset();
			return field.getSource().substring(start, range.getLength());
		}
		return null;
	}

	private static List<EnumItem> getEnumerations(IType fieldClass) throws JavaModelException {
		List<EnumItem> enumerations = null;
		if (fieldClass != null && fieldClass.isEnum()) {
			enumerations = new ArrayList<>();
			IJavaElement[] children = fieldClass.getChildren();
			for (IJavaElement c : children) {
				if (c.getElementType() == IJavaElement.FIELD && ((IField) c).isEnumConstant()) {
					String enumName = ((IField) c).getElementName();
					// TODO: extract Javadoc
					String enumDocs = null;
					enumerations.add(new EnumItem(enumName, enumDocs));
				}
			}
		}
		return enumerations;
	}

	private static boolean isOptional(String fieldTypeName) {
		return fieldTypeName.startsWith("java.util.Optional");
	}

	private static String[] getRawTypeParameters(String fieldTypeName) {
		int start = fieldTypeName.indexOf("<") + 1;
		int end = fieldTypeName.lastIndexOf(">");
		String keyValue = fieldTypeName.substring(start, end);
		int index = keyValue.indexOf(',');
		return new String[] { keyValue.substring(0, index), keyValue.substring(index + 1, keyValue.length()) };
	}

	private static void processMap(IField field, String baseKey, String mapValueClass, String docs, String location,
			String extensionName, String source, ConfigPhase configPhase, DocumentationConverter converter,
			Map<IPackageFragmentRoot, Properties> javadocCache,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties, IProgressMonitor monitor)
			throws JavaModelException {
		final String subKey = baseKey + ".{*}";
		if ("java.util.Map".equals(mapValueClass)) {
			// ignore, Map must be parameterized
		} else if (isMap(mapValueClass)) {
			String[] rawTypeParameters = getRawTypeParameters(mapValueClass);
			processMap(field, subKey, rawTypeParameters[1], docs, location, extensionName, source, configPhase,
					converter, javadocCache, quarkusProperties, monitor);
		} else if (isOptional(mapValueClass)) {
			// Optionals are not allowed as a map value type
		} else {
			IType type = findType(field.getJavaProject(), mapValueClass);
			if (type == null || isPrimitiveType(mapValueClass)) {
				// This case comes from when mapValueClass is:
				// - Simple type, like java.lang.String
				// - Type which cannot be found (bad classpath?)
				addField(location, extensionName, field, mapValueClass, null, subKey, null, converter, javadocCache,
						configPhase, quarkusProperties, monitor);
			} else {
				processConfigGroup(location, extensionName, type, subKey, configPhase, converter, javadocCache,
						quarkusProperties, monitor);
			}
		}
	}

	private static boolean isPrimitiveType(String valueClass) {
		return valueClass.equals("java.lang.String") || valueClass.equals("java.lang.Boolean")
				|| valueClass.equals("java.lang.Integer") || valueClass.equals("java.lang.Long")
				|| valueClass.equals("java.lang.Double") || valueClass.equals("java.lang.Float");
	}

	private static boolean isMap(String mapValueClass) {
		return mapValueClass.startsWith("java.util.Map");
	}

	private static boolean isList(String valueClass) {
		return valueClass.startsWith("java.util.List");
	}

	private static boolean isNumber(String valueClass) {
		return NUMBER_TYPES.contains(valueClass);
	}

	private static ExtendedConfigDescriptionBuildItem addField(String propertyName, String type, String defaultValue,
			String docs, String location, String extensionName, String source, List<EnumItem> enums,
			ConfigPhase configPhase, List<ExtendedConfigDescriptionBuildItem> quarkusProperties) {
		if (ConfigItem.NO_DEFAULT.equals(defaultValue)) {
			defaultValue = null;
		}
		ExtendedConfigDescriptionBuildItem property = new ExtendedConfigDescriptionBuildItem();
		property.setPropertyName(propertyName);
		property.setType(type);
		property.setDefaultValue(defaultValue);
		property.setDocs(docs);

		// Extra properties

		property.setExtensionName(extensionName);
		property.setLocation(location);
		property.setSource(source);
		if (configPhase != null) {
			property.setPhase(getPhase(configPhase));
		}
		property.setRequired(defaultValue == null);
		property.setEnums(enums);

		quarkusProperties.add(property);
		return property;
	}

	private static int getPhase(ConfigPhase configPhase) {
		switch (configPhase) {
		case BUILD_TIME:
			return ExtendedConfigDescriptionBuildItem.CONFIG_PHASE_BUILD_TIME;
		case BUILD_AND_RUN_TIME_FIXED:
			return ExtendedConfigDescriptionBuildItem.CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED;
		case RUN_TIME:
			return ExtendedConfigDescriptionBuildItem.CONFIG_PHASE_RUN_TIME;
		default:
			return ExtendedConfigDescriptionBuildItem.CONFIG_PHASE_BUILD_TIME;
		}
	}

	// ------------- JDT Utilities -------------

	/**
	 * Returns the annotation from the given <code>annotatable</code> element with
	 * the given name <code>annotationName</code> and null otherwise.
	 * 
	 * @param annotatable    the class, field which can be annotated.
	 * @param annotationName the annotation name
	 * @return the annotation from the given <code>annotatable</code> element with
	 *         the given name <code>annotationName</code> and null otherwise.
	 * @throws JavaModelException
	 */
	private static IAnnotation getAnnotation(IAnnotatable annotatable, String annotationName)
			throws JavaModelException {
		if (annotatable == null) {
			return null;
		}
		IAnnotation[] annotations = annotatable.getAnnotations();
		for (IAnnotation annotation : annotations) {
			if (isMatchAnnotation(annotation, annotationName)) {
				return annotation;
			}
		}
		return null;
	}

	private static boolean isMatchAnnotation(IAnnotation annotation, String annotationName) {
		return annotationName.endsWith(annotation.getElementName());
	}

	private static String getAnnotationMemberValue(IAnnotation annotation, String memberName)
			throws JavaModelException {
		for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
			if (memberName.equals(pair.getMemberName())) {
				return pair.getValue() != null ? pair.getValue().toString() : null;
			}
		}
		return null;
	}

	private static IType findType(IJavaProject project, String name) {
		try {
			return project.findType(name);
		} catch (JavaModelException e) {
			return null;
		}
	}

	private static String getResolvedTypeName(IField field) {
		try {
			String signature = field.getTypeSignature();
			IType primaryType = field.getTypeRoot().findPrimaryType();
			return JavaModelUtil.getResolvedTypeName(signature, primaryType);
		} catch (JavaModelException e) {
			return null;
		}
	}

	private static String getResolvedResultTypeName(IMethod method) {
		try {
			String signature = method.getReturnType();
			IType primaryType = method.getTypeRoot().findPrimaryType();
			return JavaModelUtil.getResolvedTypeName(signature, primaryType);
		} catch (JavaModelException e) {
			return null;
		}
	}

	private static IJarEntryResource findJavadocFromQuakusJavadocProperties(IPackageFragmentRoot packageRoot)
			throws JavaModelException {
		return JDTQuarkusSearchUtils.findPropertiesResource(packageRoot, QUARKUS_JAVADOC_PROPERTIES);
	}

}
