package com.redhat.microprofile.jdt.quarkus.route;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertCodeLens;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.cl;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.r;
import static org.junit.Assert.assertNotNull;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeLensParams;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;

import com.redhat.microprofile.jdt.quarkus.QuarkusMavenProjectName;

/**
 * Tests for the CodeLens features introduced by
 * {@link com.redhat.microprofile.psi.internal.quarkus.route.java.ReactiveRouteJaxRsInfoProvider}.
 */
public class ReactiveRouteTest extends BasePropertiesManagerTest {

	@Test
	public void testMultipleRoutesCodeLens() throws Exception {
		IJavaProject javaProject = loadMavenProject(QuarkusMavenProjectName.quarkus_route);
		assertNotNull(javaProject);

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/reactive/routes/MultipleRoutes.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		assertCodeLens(params, JDT_UTILS, //
				cl("http://localhost:8080/first", "", r(9, 28, 28)), //
				cl("http://localhost:8080/second", "", r(9, 28, 28)));
	}

	@Test
	public void testMyDeclarativeRoutesCodeLens() throws Exception {
		IJavaProject javaProject = loadMavenProject(QuarkusMavenProjectName.quarkus_route);
		assertNotNull(javaProject);

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/reactive/routes/MyDeclarativeRoutes.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		assertCodeLens(params, JDT_UTILS, //
				cl("http://localhost:8080/hello", "", r(15, 42, 42)), //
				cl("http://localhost:8080/world", "", r(20, 27, 27)), //
				cl("http://localhost:8080/greetings", "", r(25, 63, 63)), //
				cl("http://localhost:8080/greetings/:name", "", r(30, 69, 69)));
	}

	@Test
	public void testSimpleRoutesCodeLens() throws Exception {
		IJavaProject javaProject = loadMavenProject(QuarkusMavenProjectName.quarkus_route);
		assertNotNull(javaProject);

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/reactive/routes/SimpleRoutes.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		assertCodeLens(params, JDT_UTILS, //
				cl("http://localhost:8080/simple/ping", "", r(10, 25, 25)));
	}
}