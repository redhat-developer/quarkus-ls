package com.redhat.microprofile.jdt.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.microprofile.commons.metadata.ConfigurationMetadata;

/**
 * Abstract class for static properties provider.
 * 
 * Static properties are properties that should be available when a certain
 * class is present in the classpath. As a result, no search patterns are
 * required.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractStaticPropertiesProvider extends AbstractPropertiesProvider {

	private static final Logger LOGGER = Logger.getLogger(AbstractStaticPropertiesProvider.class.getName());

	private static final String PLATFORM_PLUGIN = "platform:/plugin/"; //$NON-NLS-1$

	private final String pluginId;

	private final String path;

	private ConfigurationMetadata metadata;

	public AbstractStaticPropertiesProvider(String pluginId, String path) {
		this.pluginId = pluginId;
		this.path = path;
	}

	@Override
	public final void beginSearch(SearchContext context, IProgressMonitor monitor) {
		if (isAdaptedFor(context, monitor)) {
			collectStaticProperties(context, monitor);
		}
	}

	/**
	 * Returns true if static properties must be collected for the given context and
	 * false otherwise.
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
	protected void collectStaticProperties(SearchContext context, IProgressMonitor monitor) {
		if (metadata == null) {
			try {
				metadata = getMetadata();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "In AbstractStaticPropertiesProvider#collectStaticProperties, IOException", e);
			}
		}
		if (metadata != null) {
			context.getCollector().merge(metadata);
		}
	}

	/**
	 * Returns a <code>ConfigurationMetadata</code> instance from
	 * the data stored from the json file located at <code>this.path</code>
	 * 
	 * @return <code>ConfigurationMetadata</code> instance from
	 * the data stored from the json file located at <code>this.path</code>
	 * @throws IOException
	 */
	protected ConfigurationMetadata getMetadata() throws IOException {
		InputStream in = getInputStream();
		Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8.name());
		return createGson().fromJson(reader, ConfigurationMetadata.class);
	}

	/**
	 * Returns a <code>InputStream</code> instance that reads from the
	 * file located at <code>this.path</code>
	 * 
	 * @return a <code>InputStream</code> instance that reads from the
	 * file located at <code>this.path</code>
	 * @throws IOException
	 */
	protected InputStream getInputStream() throws IOException {
		if (path == null || path.length() < 0) {
			return null;
		}
		if (pluginId != null) {
			URL url = new URL(new StringBuilder(PLATFORM_PLUGIN).append(pluginId).append("/").append(path).toString());
			return url.openStream();
		}
		return new FileInputStream(new File(path));
	}

	private static Gson createGson() {
		return new GsonBuilder().registerTypeAdapterFactory(new EnumTypeAdapter.Factory()).create();
	}

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