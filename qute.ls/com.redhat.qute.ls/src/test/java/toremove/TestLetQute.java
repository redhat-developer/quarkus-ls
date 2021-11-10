package toremove;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import io.quarkus.qute.Engine;
import io.quarkus.qute.ReflectionValueResolver;
import io.quarkus.qute.Template;

public class TestLetQute {

	public static void main(String[] args) {

		Map<String, Object> data = new HashMap<>();
		data.put("item", new Item(0));

		Engine engine = Engine.builder().addDefaults().addValueResolver(new ReflectionValueResolver()).build();

		Template template = engine.parse(convertStreamToString(TestLetQute.class.getResourceAsStream("let.qute")));
		String s = template.data(data).render();

		System.err.println(s);

	}

	private static String convertStreamToString(InputStream is) {
		try (Scanner s = new java.util.Scanner(is)) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}
}
