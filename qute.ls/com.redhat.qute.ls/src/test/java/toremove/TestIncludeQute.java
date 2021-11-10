package toremove;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import io.quarkus.qute.Engine;
import io.quarkus.qute.ReflectionValueResolver;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateLocator.TemplateLocation;
import io.quarkus.qute.Variant;

public class TestIncludeQute {

	public static void main(String[] args) {
		Map data = new HashMap<>();

		Engine engine = Engine.builder().addDefaults().addValueResolver(new ReflectionValueResolver())
				.addLocator(TestIncludeQute::locate).build();

		Template template = engine
				.parse(convertStreamToString(TestIncludeQute.class.getResourceAsStream("include/include.qute.html")));
		String s = template.data(data).render();
		System.err.println(s);
	}

	private static String convertStreamToString(InputStream is) {
		try (Scanner s = new java.util.Scanner(is)) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}

	private static String[] suffixes = { "html", "qute.html" };

	private static String basePath = "toremove/";

	/**
	 * @param path
	 * @return the optional reader
	 */
	private static Optional<TemplateLocation> locate(String path) {
		URL resource = null;
		String templatePath = basePath + path;
		// LOGGER.debugf("Locate template for %s", templatePath);
		resource = locatePath(templatePath);
		if (resource == null) {
			// Try path with suffixes
			for (String suffix : suffixes) {
				templatePath = basePath + path + "." + suffix;
				resource = locatePath(templatePath);
				if (resource != null) {
					break;
				}
			}
		}
		if (resource != null) {
			return Optional.of(new ResourceTemplateLocation(resource, guessVariant(templatePath)));
		}
		return Optional.empty();
	}

	private static URL locatePath(String path) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null) {
			cl = TestIncludeQute.class.getClassLoader();
		}
		return cl.getResource(path);
	}

	static Variant guessVariant(String path) {
		// TODO detect locale and encoding
		return Variant.forContentType(null);
	}

	static class ResourceTemplateLocation implements TemplateLocation {

		private final URL resource;
		private final Optional<Variant> variant;

		public ResourceTemplateLocation(URL resource, Variant variant) {
			this.resource = resource;
			this.variant = Optional.ofNullable(variant);
		}

		@Override
		public Reader read() {
			try {
				return new InputStreamReader(resource.openStream(), Charset.forName("utf-8"));
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		public Optional<Variant> getVariant() {
			return variant;
		}

	}
}
