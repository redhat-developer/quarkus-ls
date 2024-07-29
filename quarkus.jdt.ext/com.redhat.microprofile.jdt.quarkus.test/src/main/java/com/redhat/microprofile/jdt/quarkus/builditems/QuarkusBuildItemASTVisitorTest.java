package com.redhat.microprofile.jdt.quarkus.builditems;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.d;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;

import com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants;
import com.redhat.microprofile.jdt.internal.quarkus.builditems.QuarkusBuildItemErrorCode;
import com.redhat.microprofile.jdt.quarkus.QuarkusMavenProjectName;

public class QuarkusBuildItemASTVisitorTest extends BasePropertiesManagerTest {

	@Test
	public void buildItemClassifier() throws Exception {
		IJavaProject javaProject = loadMavenProject(QuarkusMavenProjectName.quarkus_builditems);

		{ // Bad BuildItem shows error
			MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
			IFile javaFile = javaProject.getProject()
					.getFile(new Path("src/main/java/org/acme/builditems/BadBuildItem.java"));
			String uri = javaFile.getLocation().toFile().toURI().toASCIIString();
			diagnosticsParams.setUris(Arrays.asList(uri));
			diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

			Diagnostic d = d(4, 13, 25,
					"BuildItem class `org.acme.builditems.BadBuildItem` must either be declared final or abstract",
					DiagnosticSeverity.Error, QuarkusConstants.QUARKUS_DIAGNOSTIC_SOURCE,
					QuarkusBuildItemErrorCode.InvalidModifierBuildItem);
			assertJavaDiagnostics(diagnosticsParams, JDT_UTILS, d);
		}

		{ // Good BuildItem shows no error
			MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
			IFile javaFile = javaProject.getProject()
					.getFile(new Path("src/main/java/org/acme/builditems/GoodBuildItem.java"));
			String uri = javaFile.getLocation().toFile().toURI().toASCIIString();
			diagnosticsParams.setUris(Arrays.asList(uri));
			diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);
			assertJavaDiagnostics(diagnosticsParams, JDT_UTILS);
		}

	}
}