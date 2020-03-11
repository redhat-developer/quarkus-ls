package com.redhat.microprofile.jdt.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

/**
 * Abstract class for static properties provider.
 * 
 * Static properties are properties that should be available when
 * a certain class is present in the classpath. As a result,
 * no search patterns are required.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractStaticPropertiesProvider extends AbstractPropertiesProvider {

	@Override
	public final void beginSearch(SearchContext context, IProgressMonitor monitor) {
		if (isAdaptedFor(context, monitor)) {
			collectStaticProperties(context, monitor);
		}
	}

	/**
	 * Returns true if static properties must be collected for the given context and false
	 * otherwise.
	 * 
	 * @param context the building scope context
	 * @param monitor the progress monitor
	 * @return
	 */
	protected abstract boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor);

	/**
	 * Collect static properties from the given context
	 * 
	 * @param context the building scope context
	 * @param monitor the progress monitor
	 */
	protected abstract void collectStaticProperties(SearchContext context, IProgressMonitor monitor);

	@Override
	public void collectProperties(SearchMatch match, SearchContext context, IProgressMonitor monitor) {
		// Do nothing
	}

	@Override
	protected String[] getPatterns() {
		return null;
	}

	@Override
	protected SearchPattern createSearchPattern(String pattern) {
		return null;
	}

}