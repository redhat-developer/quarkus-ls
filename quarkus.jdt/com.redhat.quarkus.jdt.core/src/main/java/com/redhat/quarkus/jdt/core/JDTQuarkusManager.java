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

import static io.quarkus.runtime.util.StringUtil.camelHumpsIterator;
import static io.quarkus.runtime.util.StringUtil.hyphenate;
import static io.quarkus.runtime.util.StringUtil.join;
import static io.quarkus.runtime.util.StringUtil.lowerCase;
import static io.quarkus.runtime.util.StringUtil.lowerCaseFirst;
import static io.quarkus.runtime.util.StringUtil.withoutSuffix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;

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

	/**
	 * Quarkus prefix used in the Quarkus property.
	 */
	private static final String QUARKUS_PREFIX = "quarkus.";

	/**
	 * The Quarkus Config* annotations
	 */
	private static final String CONFIG_ROOT_ANNOTATION = "io.quarkus.runtime.annotations.ConfigRoot";
	private static final String CONFIG_GROUP_ANNOTATION = "io.quarkus.runtime.annotations.ConfigGroup";
	private static final String CONFIG_ITEM_ANNOTATION = "io.quarkus.runtime.annotations.ConfigItem";
	private static final String CONFIG_PROPERTY_ANNOTATION = "org.eclipse.microprofile.config.inject.ConfigProperty";

	private static final JDTQuarkusManager INSTANCE = new JDTQuarkusManager();

	public static JDTQuarkusManager getInstance() {
		return INSTANCE;
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
	 * @param projectName the Eclipse project name
	 * @param converter   the documentation converter to convert JavaDoc in a format
	 *                    like Markdown
	 * @param monitor     the progress monitor
	 * @return the Quarkus project information for the given Eclipse
	 *         <code>projectName</code>.
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	public QuarkusProjectInfo getQuarkusProjectInfo(String projectName, DocumentationConverter converter,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);

		QuarkusProjectInfo info = new QuarkusProjectInfo();
		info.setProjectName(javaProject.getProject().getName());

		List<ExtendedConfigDescriptionBuildItem> quarkusProperties = new ArrayList<>();
		Map<IPackageFragmentRoot, Properties> javadocCache = new HashMap();

		// Scan Quarkus annotations Config* by using Java eclipse search engine.
		SearchPattern pattern = createQuarkusConfigSearchPattern();
		SearchEngine engine = new SearchEngine();
		engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
				createSearchScope(javaProject), new SearchRequestor() {

					@Override
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						Object element = match.getElement();
						if (element instanceof IAnnotatable && element instanceof IJavaElement) {
							IJavaElement javaElement = (IJavaElement) element;
							processQuarkusAnnotation(javaElement, converter, javadocCache, quarkusProperties, monitor);
						}
					}
				}, monitor);

		// FIXME: manage isQuarkusProject
		info.setQuarkusProject(quarkusProperties.size() > 0);
		info.setProperties(quarkusProperties);
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
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties, IProgressMonitor monitor)
			throws JavaModelException {
		IAnnotation[] annotations = ((IAnnotatable) javaElement).getAnnotations();
		for (IAnnotation annotation : annotations) {
			if (CONFIG_PROPERTY_ANNOTATION.contentEquals(annotation.getElementName())) {
				processConfigProperty(javaElement, annotation, converter, javadocCache, quarkusProperties, monitor);
			} else if (CONFIG_ROOT_ANNOTATION.contentEquals(annotation.getElementName())) {
				processConfigRoot(javaElement, annotation, converter, javadocCache, quarkusProperties, monitor);
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
	 */
	private static void processConfigProperty(IJavaElement javaElement, IAnnotation configPropertyAnnotation,
			DocumentationConverter converter, Map<IPackageFragmentRoot, Properties> javadocCache,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties, IProgressMonitor monitor) {
		// TODO: implement collect of @ConfigProperty

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
		String baseKey = QUARKUS_PREFIX + extension;
		processConfigGroup(javaElement, baseKey, configPhase, converter, javadocCache, quarkusProperties, monitor);
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
	private static void processConfigGroup(IJavaElement javaElement, String baseKey, ConfigPhase configPhase,
			DocumentationConverter converter, Map<IPackageFragmentRoot, Properties> javadocCache,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties, IProgressMonitor monitor)
			throws JavaModelException {
		if (javaElement.getElementType() == IJavaElement.TYPE) {
			IJavaElement parent = javaElement.getParent();
			if (parent.getElementType() == IJavaElement.CLASS_FILE) {
				IJavaElement[] elements = ((IParent) javaElement).getChildren();
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
							processConfigGroup(fieldClass, subKey, configPhase, converter, javadocCache,
									quarkusProperties, monitor);
						} else {
							String propertyName = subKey;

							// Class type
							String type = fieldClass != null ? fieldClass.getFullyQualifiedName() : fieldTypeName;

							// Javadoc
							String docs = getJavadoc(field, javadocCache, monitor);
							docs = converter.convert(docs);

							// Location (JAR, src)
							IPackageFragmentRoot packageRoot = (IPackageFragmentRoot) field
									.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
							String location = packageRoot.getPath().toString();

							// field and class source
							String source = field.getDeclaringType().getFullyQualifiedName() + "#"
									+ field.getElementName();

							// Enumerations
							List<String> enumerations = getEnumerations(fieldClass);

							// Default value for primitive type
							if ("boolean".equals(fieldTypeName)) {
								addField(propertyName, type,
										ConfigItem.NO_DEFAULT.equals(defaultValue) ? "false" : defaultValue, docs,
										location, source, enumerations, configPhase, quarkusProperties);
							} else if ("int".equals(fieldTypeName)) {
								addField(propertyName, type,
										ConfigItem.NO_DEFAULT.equals(defaultValue) ? "0" : defaultValue, docs, location,
										source, enumerations, configPhase, quarkusProperties);
							} else if ("long".equals(fieldTypeName)) {
								addField(propertyName, type,
										ConfigItem.NO_DEFAULT.equals(defaultValue) ? "0" : defaultValue, docs, location,
										source, enumerations, configPhase, quarkusProperties);
							} else if ("double".equals(fieldTypeName)) {
								addField(propertyName, type,
										ConfigItem.NO_DEFAULT.equals(defaultValue) ? "0" : defaultValue, docs, location,
										source, enumerations, configPhase, quarkusProperties);
							} else if ("float".equals(fieldTypeName)) {
								addField(propertyName, type,
										ConfigItem.NO_DEFAULT.equals(defaultValue) ? "0" : defaultValue, docs, location,
										source, enumerations, configPhase, quarkusProperties);
							} else if (isMap(fieldTypeName)) {
								// FIXME: find better mean to check field is a Map
								// this code works only if user uses Map as declaration and not if it declare
								// HashMap for instance
								String[] rawTypeParameters = getRawTypeParameters(fieldTypeName);
								if ((rawTypeParameters[0].trim().equals("java.lang.String"))) {
									// The key Map must be a String
									processMap(field, subKey, rawTypeParameters[1], docs, location, source, configPhase,
											quarkusProperties);
								}
							} else if (isList(fieldTypeName)) {
								addField(propertyName, type, defaultValue, docs, location, source, enumerations,
										configPhase, quarkusProperties);
							} else if (isOptionnal(fieldTypeName)) {
								ExtendedConfigDescriptionBuildItem item = addField(propertyName, type, defaultValue,
										docs, location, source, enumerations, configPhase, quarkusProperties);
								item.setRequired(false);
							} else {
								addField(propertyName, type, defaultValue, docs, location, source, enumerations,
										configPhase, quarkusProperties);
							}
						}
					}
				}
			}
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
		// Try to get javadoc from sources
		String javadoc = findJavadocFromSource(field);
		if (javadoc != null) {
			return javadoc;
		}
		// Try to get attached javadoc
		javadoc = field.getAttachedJavadoc(monitor);
		if (javadoc != null) {
			return javadoc;
		}
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

	private static List<String> getEnumerations(IType fieldClass) throws JavaModelException {
		List<String> enumerations = null;
		if (fieldClass != null && fieldClass.isEnum()) {
			enumerations = new ArrayList<>();
			IJavaElement[] children = fieldClass.getChildren();
			for (IJavaElement c : children) {
				if (c.getElementType() == IJavaElement.FIELD && ((IField) c).isEnumConstant()) {
					String enumName = ((IField) c).getElementName();
					enumerations.add(enumName);
				}
			}
		}
		return enumerations;
	}

	private static boolean isOptionnal(String fieldTypeName) {
		return fieldTypeName.startsWith("java.util.Optionnal<");
	}

	private static String[] getRawTypeParameters(String fieldTypeName) {
		int start = fieldTypeName.indexOf("<") + 1;
		int end = fieldTypeName.indexOf(">");
		String[] rawTypeParameters = fieldTypeName.substring(start, end).split(",");
		return rawTypeParameters;
	}

	private static void processMap(IField field, String baseKey, String mapValueClass, String docs, String location, String source,
			ConfigPhase configPhase, List<ExtendedConfigDescriptionBuildItem> quarkusProperties) {
		final String subKey = baseKey + ".{*}";
		if ("java.util.Map".equals(mapValueClass)) {
			// ignore, Map must be parameterized
		} else if (isMap(mapValueClass)) {
			String[] rawTypeParameters = getRawTypeParameters(mapValueClass);
			processMap(field, subKey, rawTypeParameters[1], docs, location, source, configPhase, quarkusProperties);
		} else if (isOptionnal(mapValueClass)) {
			// Optionals are not allowed as a map value type
		} else {
			String propertyName = subKey;
			// TODO: manage Map....
			addField(propertyName, "java.util.Map<java.lang.String," + mapValueClass + ">", null, docs, location,
					source, null, configPhase, quarkusProperties);
		}
	}

	private static boolean isMap(String mapValueClass) {
		return mapValueClass.startsWith("java.util.Map<");
	}

	private static boolean isList(String valueClass) {
		return valueClass.startsWith("java.util.List<");
	}

	private static ExtendedConfigDescriptionBuildItem addField(String propertyName, String type, String defaultValue,
			String docs, String location, String source, List<String> enums, ConfigPhase configPhase,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties) {
		ExtendedConfigDescriptionBuildItem property = new ExtendedConfigDescriptionBuildItem();
		property.setPropertyName(propertyName);
		property.setType(type);
		property.setDefaultValue(defaultValue);
		property.setDocs(docs);

		// Extra properties
		property.setLocation(location);
		property.setSource(source);
		property.setPhase(getPhase(configPhase));
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
			if (annotationName.equals(annotation.getElementName())) {
				return annotation;
			}
		}
		return null;
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

	private static IJarEntryResource findJavadocFromQuakusJavadocProperties(IPackageFragmentRoot packageRoot)
			throws JavaModelException {
		Object[] resources = packageRoot.getNonJavaResources();
		if (resources != null) {
			for (Object object : resources) {
				if (object instanceof IJarEntryResource) {
					IJarEntryResource res = (IJarEntryResource) object;
					if ("META-INF".equals(res.getName())) {
						IJarEntryResource[] children = res.getChildren();
						if (children != null) {
							for (IJarEntryResource r : children) {
								if ("quarkus-javadoc.properties".equals(r.getName())) {
									return r;

								}
							}
						}
						return null;
					}
				}
			}
		}
		return null;
	}

	private static IJavaSearchScope createSearchScope(IJavaProject project) throws JavaModelException {
		return SearchEngine.createJavaSearchScope(new IJavaElement[] { project },
				/* IJavaSearchScope.SOURCES | */ IJavaSearchScope.APPLICATION_LIBRARIES);
	}

	private static SearchPattern createQuarkusConfigSearchPattern() {
		// Pattern to search @ConfigRoot annotation
		SearchPattern configRootPattern = SearchPattern.createPattern(CONFIG_ROOT_ANNOTATION,
				IJavaSearchConstants.ANNOTATION_TYPE, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_EXACT_MATCH);
		// Pattern to search @ConfigProperty annotation
		SearchPattern configPropertyPattern = SearchPattern.createPattern(CONFIG_PROPERTY_ANNOTATION,
				IJavaSearchConstants.ANNOTATION_TYPE, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_EXACT_MATCH);
		return SearchPattern.createOrPattern(configRootPattern, configPropertyPattern);
	}
}
