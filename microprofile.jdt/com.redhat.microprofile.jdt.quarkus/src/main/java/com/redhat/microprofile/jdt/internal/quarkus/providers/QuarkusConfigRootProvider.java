package com.redhat.microprofile.jdt.internal.quarkus.providers;

import static com.redhat.microprofile.jdt.core.utils.AnnotationUtils.getAnnotation;
import static com.redhat.microprofile.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.findType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getPropertyType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getResolvedTypeName;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getSourceField;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getSourceType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.isList;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.isMap;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.isNumber;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.isPrimitiveBoolean;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.isOptional;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.isPrimitiveType;
import static io.quarkus.runtime.util.StringUtil.camelHumpsIterator;
import static io.quarkus.runtime.util.StringUtil.hyphenate;
import static io.quarkus.runtime.util.StringUtil.join;
import static io.quarkus.runtime.util.StringUtil.lowerCase;
import static io.quarkus.runtime.util.StringUtil.lowerCaseFirst;
import static io.quarkus.runtime.util.StringUtil.withoutSuffix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.jdt.core.AbstractPropertiesProvider;
import com.redhat.microprofile.jdt.core.ArtifactResolver;
import com.redhat.microprofile.jdt.core.IPropertiesCollector;
import com.redhat.microprofile.jdt.core.SearchContext;
import com.redhat.microprofile.jdt.core.utils.JDTTypeUtils;
import com.redhat.microprofile.jdt.internal.quarkus.JDTQuarkusUtils;
import com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;

public class QuarkusConfigRootProvider extends AbstractPropertiesProvider {

	private static final String[] ANNOTATION_NAMES = { QuarkusConstants.CONFIG_ROOT_ANNOTATION };

	private static final String JAVADOC_CACHE_KEY = QuarkusConfigRootProvider.class.getName() + "#javadoc";

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	public void begin(SearchContext context) {
		Map<IPackageFragmentRoot, Properties> javadocCache = new HashMap<>();
		context.put(JAVADOC_CACHE_KEY, javadocCache);
	}

	@Override
	public void contributeToClasspath(IJavaProject project, boolean excludeTestCode, ArtifactResolver artifactResolver,
			List<IClasspathEntry> deploymentJarEntries) throws JavaModelException {
		IClasspathEntry[] entries = project.getResolvedClasspath(true);
		List<String> existingJars = Stream.of(entries)
				// filter entry to collect only JAR
				.filter(entry -> entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
				// filter Quarkus deployment JAR marked as test scope. Ex:
				// 'quarkus-core-deployment' can be marked as test scope, we must exclude them
				// to avoid to ignore it in the next step.
				.filter(entry -> !excludeTestCode || (excludeTestCode && !entry.isTest())) //
				.map(entry -> entry.getPath().lastSegment()).collect(Collectors.toList());
		for (IClasspathEntry entry : entries) {
			if (excludeTestCode && entry.isTest()) {
				continue;
			}
			switch (entry.getEntryKind()) {

			case IClasspathEntry.CPE_LIBRARY:

				try {
					String jarPath = entry.getPath().toOSString();
					IPackageFragmentRoot root = project.getPackageFragmentRoot(jarPath);
					if (root != null) {
						IJarEntryResource resource = JDTTypeUtils.findPropertiesResource(root,
								QuarkusConstants.QUARKUS_EXTENSION_PROPERTIES);
						if (resource != null) {
							Properties properties = new Properties();
							properties.load(resource.getContents());
							// deployment-artifact=io.quarkus\:quarkus-undertow-deployment\:0.21.1
							String deploymentArtifact = properties.getProperty("deployment-artifact");
							String[] result = deploymentArtifact.split(":");
							String groupId = result[0];
							String artifactId = result[1];
							String version = result[2];
							// Get or download deployment JAR
							String deploymentJarFile = artifactResolver.getArtifact(groupId, artifactId, version);
							if (deploymentJarFile != null) {
								IPath deploymentJarFilePath = new Path(deploymentJarFile);
								String deploymentJarName = deploymentJarFilePath.lastSegment();
								if (!existingJars.contains(deploymentJarName)) {
									// The *-deployment JAR is not included in the classpath project, add it.
									existingJars.add(deploymentJarName);
									IPath sourceAttachmentPath = null;
									// Get or download deployment sources JAR
									String sourceJarFile = artifactResolver.getSources(groupId, artifactId, version);
									if (sourceJarFile != null) {
										sourceAttachmentPath = new Path(sourceJarFile);
									}
									deploymentJarEntries.add(JavaCore.newLibraryEntry(deploymentJarFilePath,
											sourceAttachmentPath, null));
								}
							}
						}
					}
				} catch (Exception e) {
					// do nothing
				}

				break;
			}
		}
		// Add the Quarkus project in classpath to resolve dependencies of deployment
		// Quarkus JARs.
		deploymentJarEntries.add(JavaCore.newProjectEntry(project.getProject().getLocation()));
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation annotation, String annotationName,
			SearchContext context, IPropertiesCollector collector, IProgressMonitor monitor) throws JavaModelException {
		Map<IPackageFragmentRoot, Properties> javadocCache = (Map<IPackageFragmentRoot, Properties>) context
				.get(JAVADOC_CACHE_KEY);
		processConfigRoot(javaElement, annotation, javadocCache, collector, monitor);
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
	 * @param collector            the properties to fill.
	 * @param monitor              the progress monitor.
	 */
	private void processConfigRoot(IJavaElement javaElement, IAnnotation configRootAnnotation,
			Map<IPackageFragmentRoot, Properties> javadocCache, IPropertiesCollector collector,
			IProgressMonitor monitor) throws JavaModelException {
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

		String baseKey = QuarkusConstants.QUARKUS_PREFIX + extension;
		processConfigGroup(extensionName, javaElement, baseKey, configPhase, javadocCache, collector, monitor);
	}

	/**
	 * Returns the Quarkus @ConfigRoot(phase=...) value.
	 * 
	 * @param configRootAnnotation
	 * @return the Quarkus @ConfigRoot(phase=...) value.
	 * @throws JavaModelException
	 */
	private static ConfigPhase getConfigPhase(IAnnotation configRootAnnotation) throws JavaModelException {
		String value = getAnnotationMemberValue(configRootAnnotation, QuarkusConstants.CONFIG_ROOT_ANNOTATION_PHASE);
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
		String value = getAnnotationMemberValue(configRootAnnotation, QuarkusConstants.CONFIG_ANNOTATION_NAME);
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
	 * @param extensionName the Quarkus extension name
	 * 
	 * @param javaElement   the class, field element which have a Quarkus
	 *                      ConfigGroup annotations.
	 * @param baseKey       the base key
	 * @param configPhase   the phase
	 * @param converter     the documentation converter to use.
	 * @param javadocCache  the Javadoc cache
	 * @param collector     the properties to fill.
	 * @param monitor       the progress monitor.
	 * @throws JavaModelException
	 */
	private void processConfigGroup(String extensionName, IJavaElement javaElement, String baseKey,
			ConfigPhase configPhase, Map<IPackageFragmentRoot, Properties> javadocCache, IPropertiesCollector collector,
			IProgressMonitor monitor) throws JavaModelException {
		if (javaElement.getElementType() == IJavaElement.TYPE) {
			IJavaElement[] elements = ((IType) javaElement).getChildren();
			for (IJavaElement child : elements) {
				if (child.getElementType() == IJavaElement.FIELD) {
					IField field = (IField) child;
					final IAnnotation configItemAnnotation = getAnnotation((IAnnotatable) field,
							QuarkusConstants.CONFIG_ITEM_ANNOTATION);
					String name = configItemAnnotation == null ? hyphenate(field.getElementName())
							: getAnnotationMemberValue(configItemAnnotation, QuarkusConstants.CONFIG_ANNOTATION_NAME);
					if (name == null) {
						name = ConfigItem.HYPHENATED_ELEMENT_NAME;
					}
					String subKey;
					if (name.equals(ConfigItem.PARENT)) {
						subKey = baseKey;
					} else if (name.equals(ConfigItem.ELEMENT_NAME)) {
						subKey = baseKey + "." + field.getElementName();
					} else if (name.equals(ConfigItem.HYPHENATED_ELEMENT_NAME)) {
						subKey = baseKey + "." + hyphenate(field.getElementName());
					} else {
						subKey = baseKey + "." + name;
					}
					final String defaultValue = configItemAnnotation == null ? ConfigItem.NO_DEFAULT
							: getAnnotationMemberValue(configItemAnnotation, QuarkusConstants.CONFIG_ITEM_ANNOTATION_DEFAULT_VALUE);

					String fieldTypeName = getResolvedTypeName(field);
					IType fieldClass = findType(field.getJavaProject(), fieldTypeName);
					final IAnnotation configGroupAnnotation = getAnnotation((IAnnotatable) fieldClass,
							QuarkusConstants.CONFIG_GROUP_ANNOTATION);
					if (configGroupAnnotation != null) {
						processConfigGroup(extensionName, fieldClass, subKey, configPhase, javadocCache, collector,
								monitor);
					} else {
						addItemMetadata(extensionName, field, fieldTypeName, fieldClass, subKey, defaultValue,
								javadocCache, configPhase, collector, monitor);
					}
				}
			}
		}
	}

	private void addItemMetadata(String extensionName, IField field, String fieldTypeName, IType fieldClass,
			String name, String defaultValue, Map<IPackageFragmentRoot, Properties> javadocCache,
			ConfigPhase configPhase, IPropertiesCollector collector, IProgressMonitor monitor)
			throws JavaModelException {

		// Class type
		String type = getPropertyType(fieldClass, fieldTypeName);

		// Javadoc
		String description = getJavadoc(field, javadocCache, monitor);

		// field and class source
		String sourceType = getSourceType(field);
		String sourceField = getSourceField(field);

		// Enumerations
		super.updateHint(collector, fieldClass);

		ItemMetadata item = null;
		// Default value for primitive type
		if (isPrimitiveBoolean(fieldTypeName)) {
			item = super.addItemMetadata(collector, name, type, description, sourceType, sourceField, null,
					defaultValue == null || ConfigItem.NO_DEFAULT.equals(defaultValue) ? "false" : defaultValue, extensionName,
					field.isBinary());
		} else if (isNumber(fieldTypeName)) {
			item = super.addItemMetadata(collector, name, type, description, sourceType, sourceField, null,
					defaultValue == null || ConfigItem.NO_DEFAULT.equals(defaultValue) ? "0" : defaultValue, extensionName, field.isBinary());
		} else if (isMap(fieldTypeName)) {
			// FIXME: find better mean to check field is a Map
			// this code works only if user uses Map as declaration and not if they declare
			// HashMap for instance
			String[] rawTypeParameters = getRawTypeParameters(fieldTypeName);
			if ((rawTypeParameters[0].trim().equals("java.lang.String"))) {
				// The key Map must be a String
				processMap(field, name, rawTypeParameters[1], description, extensionName, sourceType, configPhase,
						javadocCache, collector, monitor);
			}
		} else if (isList(fieldTypeName)) {
			item = super.addItemMetadata(collector, name, type, description, sourceType, sourceField, null,
					defaultValue, extensionName, field.isBinary());
		} else if (isOptional(fieldTypeName)) {
			item = super.addItemMetadata(collector, name, type, description, sourceType, sourceField, null,
					defaultValue, extensionName, field.isBinary());
			item.setRequired(false);
		} else {
			if (ConfigItem.NO_DEFAULT.equals(defaultValue)) {
				defaultValue = null;
			}
			item = super.addItemMetadata(collector, name, type, description, sourceType, sourceField, null,
					defaultValue, extensionName, field.isBinary());
		}
		if (item != null) {
			item.setPhase(getPhase(configPhase));
		}
	}

	private static String[] getRawTypeParameters(String fieldTypeName) {
		int start = fieldTypeName.indexOf("<") + 1;
		int end = fieldTypeName.lastIndexOf(">");
		String keyValue = fieldTypeName.substring(start, end);
		int index = keyValue.indexOf(',');
		return new String[] { keyValue.substring(0, index), keyValue.substring(index + 1, keyValue.length()) };
	}

	private void processMap(IField field, String baseKey, String mapValueClass, String docs, String extensionName,
			String source, ConfigPhase configPhase, Map<IPackageFragmentRoot, Properties> javadocCache,
			IPropertiesCollector collector, IProgressMonitor monitor) throws JavaModelException {
		final String subKey = baseKey + ".{*}";
		if ("java.util.Map".equals(mapValueClass)) {
			// ignore, Map must be parameterized
		} else if (isMap(mapValueClass)) {
			String[] rawTypeParameters = getRawTypeParameters(mapValueClass);
			processMap(field, subKey, rawTypeParameters[1], docs, extensionName, source, configPhase, javadocCache,
					collector, monitor);
		} else if (isOptional(mapValueClass)) {
			// Optionals are not allowed as a map value type
		} else {
			IType type = findType(field.getJavaProject(), mapValueClass);
			if (type == null || isPrimitiveType(mapValueClass)) {
				// This case comes from when mapValueClass is:
				// - Simple type, like java.lang.String
				// - Type which cannot be found (bad classpath?)
				addItemMetadata(extensionName, field, mapValueClass, null, subKey, null, javadocCache, configPhase,
						collector, monitor);
			} else {
				processConfigGroup(extensionName, type, subKey, configPhase, javadocCache, collector, monitor);
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

	private static IJarEntryResource findJavadocFromQuakusJavadocProperties(IPackageFragmentRoot packageRoot)
			throws JavaModelException {
		return JDTTypeUtils.findPropertiesResource(packageRoot, QuarkusConstants.QUARKUS_JAVADOC_PROPERTIES);
	}

	private static int getPhase(ConfigPhase configPhase) {
		switch (configPhase) {
		case BUILD_TIME:
			return ItemMetadata.CONFIG_PHASE_BUILD_TIME;
		case BUILD_AND_RUN_TIME_FIXED:
			return ItemMetadata.CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED;
		case RUN_TIME:
			return ItemMetadata.CONFIG_PHASE_RUN_TIME;
		default:
			return ItemMetadata.CONFIG_PHASE_BUILD_TIME;
		}
	}
}
