package com.redhat.qute.jdt.internal.template;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.jdt.utils.IJDTUtils;

/**
 * Support with template data method invocation template#data(name, value).
 * 
 * @author Angelo ZERR
 *
 */
public class TemplateDataSupport {

	private static final Logger LOGGER = Logger.getLogger(TemplateDataSupport.class.getName());

	/**
	 * Search all method invocation of template#data(name, value) to collect data
	 * model parameters for the given template.
	 * 
	 * @param fieldOrMethod the template field (ex : Template hello;) or method
	 *                      which returns TemplateInstance.
	 * @param template      the data model template to update with collect of data
	 *                      model parameters.
	 * @param monitor       the progress monitor.
	 */
	public static void collectParametersFromDataMethodInvocation(IMember fieldOrMethod,
			DataModelTemplate<DataModelParameter> template, IProgressMonitor monitor) {
		try {
			search(fieldOrMethod, new TemplateDataCollector(template, monitor), monitor);
		} catch (CoreException e) {
			LOGGER.log(Level.SEVERE,
					"Error while getting collecting template parameters for '" + fieldOrMethod.getElementName() + "'.",
					e);
		}
	}

	private static void search(IMember fieldOrMethod, TemplateDataVisitor visitor, IProgressMonitor monitor)
			throws CoreException {
		boolean searchInJavaProject = isSearchInJavaProject(fieldOrMethod);
		SearchEngine engine = new SearchEngine();
		SearchPattern pattern = SearchPattern.createPattern(fieldOrMethod, IJavaSearchConstants.REFERENCES);
		int searchScope = IJavaSearchScope.SOURCES;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(true,
				new IJavaElement[] {
						searchInJavaProject ? fieldOrMethod.getJavaProject() : fieldOrMethod.getCompilationUnit() },
				searchScope);
		engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
				new SearchRequestor() {

					@Override
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						Object o = match.getElement();
						if (o instanceof IMethod) {
							IMethod method = (IMethod) o;
							visitor.setMethod(method);
							// Get the AST of the method declaration where template field of CheckedTemplate
							// method is referenced.
							CompilationUnit cu = getASTRoot(method.getCompilationUnit());
							ASTNode methodDeclarationAST = new NodeFinder(cu, method.getSourceRange().getOffset(),
									method.getSourceRange().getLength()).getCoveringNode();
							// Visit the body of the method declaration to collect method invocation of
							// temlate.data(param-name, param-type);
							methodDeclarationAST.accept(visitor);
						}
					}
				}, monitor);
	}

	/**
	 * Returns true if the search of method invocation of template#data(name, value)
	 * must be done in Java project or inside the compilation unit of the
	 * field/method.
	 * 
	 * @param fieldOrMethod
	 * @return
	 */
	private static boolean isSearchInJavaProject(IMember fieldOrMethod) {
		if (fieldOrMethod.getElementType() == IJavaElement.FIELD) {
			return false;
		}
		IType type = fieldOrMethod.getDeclaringType();
		boolean innerClass = type.getParent() != null && type.getParent().getElementType() == IJavaElement.TYPE;
		return innerClass;
	}

	public static Location getDataMethodInvocationLocation(IMember fieldOrMethod, String parameterName, IJDTUtils utils,
			IProgressMonitor monitor) {
		try {
			TemplateDataLocation dataLocation = new TemplateDataLocation(parameterName, utils);
			search(fieldOrMethod, dataLocation, monitor);
			return dataLocation.getLocation();
		} catch (CoreException e) {
			LOGGER.log(Level.SEVERE,
					"Error while getting location template.data for '" + fieldOrMethod.getElementName() + "'.", e);
			return null;
		}
	}

	private static CompilationUnit getASTRoot(ITypeRoot typeRoot) {
		return ASTResolving.createQuickFixAST((ICompilationUnit) typeRoot, null);
	}
}
