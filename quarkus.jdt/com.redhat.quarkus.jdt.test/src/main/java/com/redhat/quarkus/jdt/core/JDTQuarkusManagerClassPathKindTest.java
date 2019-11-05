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

import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.assertProperties;
import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.p;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.quarkus.commons.ClasspathKind;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.commons.QuarkusPropertiesScope;
import com.redhat.quarkus.jdt.internal.core.utils.DependencyUtil;

/**
 * Test collect Quarkus properties from classpath kind
 * 
 * <ul>
 * <li>not in classpath -> 0 quarkus properties</li>
 * <li>in /java/main/src classpath -> N quarkus properties</li>
 * <li>in /java/main/test classpath-> N + M quarkus properties</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class JDTQuarkusManagerClassPathKindTest extends BaseJDTQuarkusManagerTest {

	@Test
	public void applicationConfigurationTest() throws Exception {

		IJavaProject javaProject = loadMavenProject("application-configuration-test");

		// not in classpath -> 0 quarkus properties
		IFile fileFromNone = javaProject.getProject().getFile(new Path("application.properties"));
		QuarkusProjectInfo infoFromNone = JDTQuarkusManager.getInstance().getQuarkusProjectInfo(fileFromNone,
				QuarkusPropertiesScope.classpath, DocumentationConverter.DEFAULT_CONVERTER, new NullProgressMonitor());
		Assert.assertEquals(ClasspathKind.NONE, infoFromNone.getClasspathKind());
		Assert.assertEquals(0, infoFromNone.getProperties().size());

		File resteasyJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-resteasy-common-deployment", "0.21.2",
				null);
		Assert.assertNotNull("quarkus-resteasy-common-deployment*.jar is missing", resteasyJARFile);

		// in /java/main/src classpath -> N quarkus properties
		IFile fileFromSrc = javaProject.getProject().getFile(new Path("src/main/resources/application.properties"));
		QuarkusProjectInfo infoFromSrc = JDTQuarkusManager.getInstance().getQuarkusProjectInfo(fileFromSrc,
				QuarkusPropertiesScope.classpath, DocumentationConverter.DEFAULT_CONVERTER, new NullProgressMonitor());
		Assert.assertEquals(ClasspathKind.SRC, infoFromSrc.getClasspathKind());
		assertProperties(infoFromSrc, 55 /* properties from JAR */ + 3 /* properties from Java sources */,

				// quarkus-resteasy JAR
				p("quarkus-resteasy-common", "quarkus.resteasy.gzip.enabled", "boolean", "If gzip is enabled",
						resteasyJARFile.getAbsolutePath(),
						"io.quarkus.resteasy.common.deployment.ResteasyCommonProcessor$ResteasyCommonConfigGzip#enabled",
						1, null),

				// GreetingResource
				// @ConfigProperty(name = "greeting.message")
				// String message;
				p(null, "greeting.message", "java.lang.String", null, "/application-configuration-test/src/main/java",
						"org.acme.config.GreetingResource#message", 0, null),

				// @ConfigProperty(name = "greeting.suffix" , defaultValue="!")
				// String suffix;
				p(null, "greeting.suffix", "java.lang.String", null, "/application-configuration-test/src/main/java",
						"org.acme.config.GreetingResource#suffix", 0, "!"),

				// @ConfigProperty(name = "greeting.name")
				// Optional<String> name;
				p(null, "greeting.name", "java.util.Optional", null, "/application-configuration-test/src/main/java",
						"org.acme.config.GreetingResource#name", 0, null));

		// in /java/main/test classpath-> N + M quarkus properties
		File undertowJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-undertow-deployment", "0.21.2", null);
		Assert.assertNotNull("quarkus-undertow-deployment*.jar is missing", undertowJARFile);

		IFile filefromTest = javaProject.getProject().getFile(new Path("src/test/resources/application.properties"));
		QuarkusProjectInfo infoFromTest = JDTQuarkusManager.getInstance().getQuarkusProjectInfo(filefromTest,
				QuarkusPropertiesScope.classpath, DocumentationConverter.DEFAULT_CONVERTER, new NullProgressMonitor());
		Assert.assertEquals(ClasspathKind.TEST, infoFromTest.getClasspathKind());
		assertProperties(infoFromTest, 63 /* properties from JAR */ + 22 /* properties from JAR (test) */ + 3 /*
																												 * properties
																												 * from
																												 * (src)
																												 * Java
																												 * sources
																												 */
				+ 3 /* properties from (test) Java sources */,

				// quarkus-resteasy JAR
				p("quarkus-resteasy-common", "quarkus.resteasy.gzip.enabled", "boolean", "If gzip is enabled",
						resteasyJARFile.getAbsolutePath(),
						"io.quarkus.resteasy.common.deployment.ResteasyCommonProcessor$ResteasyCommonConfigGzip#enabled",
						1, null),

				// quarkus-undertow has maven test scope, add it
				// <dependency>
				// <groupId>io.quarkus</groupId>
				// <artifactId>quarkus-undertow</artifactId>
				// <scope>test</scope>
				// </dependency>

				p("quarkus-undertow", "quarkus.servlet.context-path", "java.util.Optional<java.lang.String>",
						"The context path to serve all Servlet context from. This will also affect any resources\n that run as a Servlet, e.g. JAX-RS",
						undertowJARFile.getAbsolutePath(), "io.quarkus.undertow.deployment.ServletConfig#contextPath",
						1, null),

				// GreetingResource
				// @ConfigProperty(name = "greeting.message")
				// String message;
				p(null, "greeting.message", "java.lang.String", null, "/application-configuration-test/src/main/java",
						"org.acme.config.GreetingResource#message", 0, null),

				// @ConfigProperty(name = "greeting.suffix" , defaultValue="!")
				// String suffix;
				p(null, "greeting.suffix", "java.lang.String", null, "/application-configuration-test/src/main/java",
						"org.acme.config.GreetingResource#suffix", 0, "!"),

				// @ConfigProperty(name = "greeting.name")
				// Optional<String> name;
				p(null, "greeting.name", "java.util.Optional", null, "/application-configuration-test/src/main/java",
						"org.acme.config.GreetingResource#name", 0, null),

				// TestResource
				// @ConfigProperty(name = "greeting.message.test")
				// String message;
				p(null, "greeting.message.test", "java.lang.String", null,
						"/application-configuration-test/src/test/java", "org.acme.config.TestResource#message", 0,
						null),

				// @ConfigProperty(name = "greeting.suffix.test" , defaultValue="!")
				// String suffix;
				p(null, "greeting.suffix.test", "java.lang.String", null,
						"/application-configuration-test/src/test/java", "org.acme.config.TestResource#suffix", 0, "!"),

				// @ConfigProperty(name = "greeting.name.test")
				// Optional<String> name;
				p(null, "greeting.name.test", "java.util.Optional", null,
						"/application-configuration-test/src/test/java", "org.acme.config.TestResource#name", 0, null)

		);
	}

}
