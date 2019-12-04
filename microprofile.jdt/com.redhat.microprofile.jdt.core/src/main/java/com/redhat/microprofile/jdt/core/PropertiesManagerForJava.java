/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import static com.redhat.microprofile.jdt.core.utils.AnnotationUtils.getAnnotation;
import static com.redhat.microprofile.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.util.Ranges;

import com.redhat.microprofile.commons.MicroProfileJavaCodeLensParams;
import com.redhat.microprofile.commons.MicroProfileJavaHoverInfo;
import com.redhat.microprofile.commons.MicroProfileJavaHoverParams;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;

/**
 * JDT quarkus manager for Java files.
 * 
 * @author Angelo ZERR
 *
 */
public class PropertiesManagerForJava {

	private static final Logger LOGGER = Logger.getLogger(PropertiesManagerForJava.class.getName());

	private static final String LOCALHOST = "localhost";

	private static final int PING_TIMEOUT = 2000;

	private static final String JAVAX_WS_RS_PATH_ANNOTATION = "javax.ws.rs.Path";

	private static final String JAVAX_WS_RS_GET_ANNOTATION = "javax.ws.rs.GET";

	private static final String PATH_VALUE = "value";

	private static final PropertiesManagerForJava INSTANCE = new PropertiesManagerForJava();

	private static class JDTQuarkusProjectInfo {

		private static final String APPLICATION_PROPERTIES_FILE = "application.properties";

		private static final int DEFAULT_PORT = 8080;

		private Integer serverPort;

		private long lastModified;

		private File applicationPropertiesFile;

		private Properties properties;

		private final IJavaProject javaProject;

		public JDTQuarkusProjectInfo(IJavaProject javaProject) {
			this.javaProject = javaProject;
		}

		/**
		 * Returns the target/classes/application.properties and null otherwise.
		 * 
		 * <p>
		 * Using this file instead of using src/main/resources/application.properties
		 * gives the capability to get the filtered value.
		 * </p>
		 * 
		 * @return the target/classes/application.properties and null otherwise.
		 */
		private File getApplicationPropertiesFile() {
			if (applicationPropertiesFile != null && applicationPropertiesFile.exists()) {
				return applicationPropertiesFile;
			}
			try {
				List<IPath> outputs = Stream.of(((JavaProject) javaProject).getResolvedClasspath(true)) //
						.filter(entry -> !entry.isTest()) //
						.filter(entry -> entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) //
						.map(entry -> entry.getOutputLocation()) //
						.filter(output -> output != null) //
						.distinct() //
						.collect(Collectors.toList());
				for (IPath output : outputs) {
					File file = javaProject.getProject().getLocation().append(output.removeFirstSegments(1))
							.append(APPLICATION_PROPERTIES_FILE).toFile();
					if (file.exists()) {
						applicationPropertiesFile = file;

						return applicationPropertiesFile;
					}
				}
				return null;
			} catch (JavaModelException e) {
				return null;
			}
		}

		/**
		 * Returns the loaded application.properties and null otherwise.
		 * 
		 * @return the loaded application.properties and null otherwise
		 * @throws JavaModelException
		 */
		public Properties getApplicationProperties() throws JavaModelException {
			File applicationPropertiesFile = getApplicationPropertiesFile();
			if (applicationPropertiesFile == null) {
				reset();
				return null;
			}
			if (applicationPropertiesFile.lastModified() != lastModified) {
				reset();
				try (InputStream input = new FileInputStream(applicationPropertiesFile)) {
					properties = new Properties();
					// load a properties file
					properties.load(input);
					lastModified = applicationPropertiesFile.lastModified();
				} catch (IOException e) {
					reset();
					LOGGER.log(Level.SEVERE,
							"Error while loading properties from '" + applicationPropertiesFile.getPath() + "'.", e);
				}
			}
			return properties;
		}

		private void reset() {
			properties = null;
			serverPort = null;
		}

		/**
		 * Returns the server port.
		 * 
		 * @return the server port.
		 */
		public int getServerPort() {
			try {
				try {
					// Get application properties and reset the cache if needs
					Properties properties = getApplicationProperties();
					if (serverPort == null) {
						if (properties != null) {
							String port = properties.getProperty("quarkus.http.port", "");
							if (!port.trim().isEmpty()) {
								serverPort = Integer.parseInt(port.trim());
							}
						}
					}
				} catch (JavaModelException e) {
					LOGGER.log(Level.SEVERE, "Error while getting 'quarkus.http.port", e);
				}
			} finally {
				if (serverPort == null) {
					serverPort = DEFAULT_PORT;
				}
			}
			return serverPort;
		}
	}

	public static PropertiesManagerForJava getInstance() {
		return INSTANCE;
	}

	private final Map<IJavaProject, JDTQuarkusProjectInfo> infos;

	private PropertiesManagerForJava() {
		this.infos = new HashMap<>();
	}

	/**
	 * Returns the codelens list according the given codelens parameters.
	 * 
	 * @param params  the codelens parameters
	 * @param utils   the utilities class
	 * @param monitor the monitor
	 * @return the codelens list according the given codelens parameters.
	 * @throws JavaModelException
	 */
	public List<? extends CodeLens> codeLens(MicroProfileJavaCodeLensParams params, IJDTUtils utils,
			IProgressMonitor monitor) throws JavaModelException {
		String uri = params.getUri();
		ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);
		if (typeRoot == null) {
			return Collections.emptyList();
		}
		IJavaElement[] elements = typeRoot.getChildren();
		Collection<CodeLens> lenses = new LinkedHashSet<>(elements.length);
		if (params.isUrlCodeLensEnabled()) {
			int serverPort = getJDTQuarkusProjectInfo(typeRoot.getJavaProject()).getServerPort();
			params.setLocalServerPort(serverPort);
			collectURLCodeLenses(typeRoot, elements, null, lenses, params, utils, monitor);
		}
		if (monitor.isCanceled()) {
			lenses.clear();
		}
		return new ArrayList<>(lenses);
	}

	/**
	 * Given the uri returns a {@link ITypeRoot}. May return null if it can not
	 * associate the uri with a Java file ot class file.
	 *
	 * @param uri
	 * @param utils   JDT LS utilities
	 * @param monitor the progress monitor
	 * @return compilation unit
	 */
	private static ITypeRoot resolveTypeRoot(String uri, IJDTUtils utils, IProgressMonitor monitor) {
		utils.waitForLifecycleJobs(monitor);
		final ICompilationUnit unit = utils.resolveCompilationUnit(uri);
		IClassFile classFile = null;
		if (unit == null) {
			classFile = utils.resolveClassFile(uri);
			if (classFile == null) {
				return null;
			}
		} else {
			if (!unit.getResource().exists() || monitor.isCanceled()) {
				return null;
			}
		}
		return unit != null ? unit : classFile;
	}

	private JDTQuarkusProjectInfo getJDTQuarkusProjectInfo(IJavaProject project) throws JavaModelException {
		JDTQuarkusProjectInfo info = infos.get(project);
		if (info == null) {
			info = new JDTQuarkusProjectInfo(project);
			infos.put(project, info);
		}
		return info;
	}

	private void collectURLCodeLenses(ITypeRoot typeRoot, IJavaElement[] elements, String rootPath,
			Collection<CodeLens> lenses, MicroProfileJavaCodeLensParams params, IJDTUtils utils,
			IProgressMonitor monitor) throws JavaModelException {
		for (IJavaElement element : elements) {
			if (monitor.isCanceled()) {
				return;
			}
			if (element.getElementType() == IJavaElement.TYPE) {
				IType type = (IType) element;
				// Get value of JAX-RS @Path annotation from the class
				String pathValue = getJaxRsPathValue(type);
				if (pathValue != null) {
					// Class is annotated with @Path
					// Display code lens only if local server is available.
					if (!params.isCheckServerAvailable()
							|| isServerAvailable(LOCALHOST, params.getLocalServerPort(), PING_TIMEOUT)) {
						// Loop for each method annotated with @Path to generate URL code lens per
						// method.
						collectURLCodeLenses(typeRoot, type.getChildren(), pathValue, lenses, params, utils, monitor);
					}
				}
				continue;
			} else if (element.getElementType() == IJavaElement.METHOD) {
				if (utils.isHiddenGeneratedElement(element)) {
					continue;
				}
				// ignore element if method range overlaps the type range, happens for generated
				// bytcode, i.e. with lombok
				IJavaElement parentType = element.getAncestor(IJavaElement.TYPE);
				if (parentType != null && overlaps(((ISourceReference) parentType).getNameRange(),
						((ISourceReference) element).getNameRange())) {
					continue;
				}
			} else {// neither a type nor a method, we bail
				continue;
			}

			// Here java element is a method
			if (rootPath != null) {
				IMethod method = (IMethod) element;
				// A JAX-RS method is a public method annotated with @GET @POST, @DELETE, @PUT
				// JAX-RS
				// annotation
				if (isJaxRsRequestMethod(method) && Flags.isPublic(method.getFlags())) {
					CodeLens lens = createCodeLens(element, typeRoot, utils);
					if (lens != null) {
						String baseURL = params.getLocalBaseURL();
						String pathValue = getJaxRsPathValue(method);
						String url = buildURL(baseURL, rootPath, pathValue);
						String openURICommandId = params.getOpenURICommand();
						lens.setCommand(new Command(url, openURICommandId != null ? openURICommandId : "",
								Collections.singletonList(url)));
						lenses.add(lens);
					}
				}
			}
		}
	}

	private static String buildURL(String... paths) {
		StringBuilder url = new StringBuilder();
		for (String path : paths) {
			if (path != null && !path.isEmpty()) {
				if (!url.toString().isEmpty() && path.charAt(0) != '/' && url.charAt(url.length() - 1) != '/') {
					url.append('/');
				}
				url.append(path);
			}
		}
		return url.toString();
	}

	private static String getJaxRsPathValue(IAnnotatable annotatable) throws JavaModelException {
		IAnnotation annotationPath = getAnnotation(annotatable, JAVAX_WS_RS_PATH_ANNOTATION);
		return annotationPath != null ? getAnnotationMemberValue(annotationPath, PATH_VALUE) : null;
	}

	private static boolean isJaxRsRequestMethod(IAnnotatable annotatable) throws JavaModelException {
		return getAnnotation(annotatable, JAVAX_WS_RS_GET_ANNOTATION) != null;
	}

	private boolean overlaps(ISourceRange typeRange, ISourceRange methodRange) {
		if (typeRange == null || methodRange == null) {
			return false;
		}
		// method range is overlapping if it appears before or actually overlaps the
		// type's range
		return methodRange.getOffset() < typeRange.getOffset() || methodRange.getOffset() >= typeRange.getOffset()
				&& methodRange.getOffset() <= (typeRange.getOffset() + typeRange.getLength());
	}

	private static CodeLens createCodeLens(IJavaElement element, ITypeRoot typeRoot, IJDTUtils utils)
			throws JavaModelException {
		ISourceRange r = ((ISourceReference) element).getNameRange();
		if (r == null) {
			return null;
		}
		CodeLens lens = new CodeLens();
		final Range range = utils.toRange(typeRoot, r.getOffset(), r.getLength());
		lens.setRange(range);
		String uri = utils.toClientUri(utils.toUri(typeRoot));
		lens.setData(Arrays.asList(uri, range.getStart()));
		return lens;
	}

	private static boolean isServerAvailable(String host, int port, int timeout) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), timeout);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Returns the hover information according to the given <code>params</code>
	 * 
	 * @param params  the hover parameters
	 * @param utils   the utilities class
	 * @param monitor the monitor
	 * @return the hover information according to the given <code>params</code>
	 * @throws JavaModelException
	 */
	public MicroProfileJavaHoverInfo hover(MicroProfileJavaHoverParams params, IJDTUtils utils,
			IProgressMonitor monitor) throws JavaModelException {
		String uri = params.getUri();
		ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);

		Position hoverPosition = params.getPosition();
		int hoveredOffset = utils.toOffset(typeRoot.getBuffer(), hoverPosition.getLine(), hoverPosition.getCharacter());
		IJavaElement hoverElement = typeRoot.getElementAt(hoveredOffset);
		if (hoverElement == null) {
			return null;
		}
		if (hoverElement.getElementType() == IJavaElement.FIELD) {

			IField hoverField = (IField) hoverElement;

			IAnnotation annotation = getAnnotation(hoverField, MicroProfileConstants.CONFIG_PROPERTY_ANNOTATION);

			if (annotation == null) {
				return null;
			}

			String annotationSource = ((ISourceReference) annotation).getSource();
			String propertyKey = getAnnotationMemberValue(annotation,
					MicroProfileConstants.CONFIG_PROPERTY_ANNOTATION_NAME);

			if (propertyKey == null) {
				return null;
			}

			ISourceRange r = ((ISourceReference) annotation).getSourceRange();
			int offset = annotationSource.indexOf(propertyKey);
			final Range propertyKeyRange = utils.toRange(typeRoot, r.getOffset() + offset, propertyKey.length());

			if (hoverPosition.equals(propertyKeyRange.getEnd())
					|| !Ranges.containsPosition(propertyKeyRange, hoverPosition)) {
				return null;
			}

			IJavaProject javaProject = typeRoot.getJavaProject();

			if (javaProject == null) {
				return null;
			}

			Properties properties = getJDTQuarkusProjectInfo(javaProject).getApplicationProperties();

			if (properties == null) {
				return null;
			}

			String propertyValue = properties.getProperty(propertyKey);
			if (propertyValue == null) {
				propertyValue = getAnnotationMemberValue(annotation,
						MicroProfileConstants.CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE);
				if (propertyValue != null && propertyValue.length() == 0) {
					propertyValue = null;
				}
			}

			return new MicroProfileJavaHoverInfo(propertyKey, propertyValue, propertyKeyRange);
		}

		return null;
	}

}