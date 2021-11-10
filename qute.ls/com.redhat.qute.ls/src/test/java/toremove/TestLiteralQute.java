package toremove;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import io.quarkus.qute.Engine;
import io.quarkus.qute.ReflectionValueResolver;
import io.quarkus.qute.Template;

public class TestLiteralQute {

	public static void main(String[] args) {

		List<Item> items=new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			Item item = new Item(i);
			items.add(item);
			for (int j = 0; j <7; j++) {
				item.addReview(new Review(j));
			}
		}
		
		
		Map<String, Object> data = new HashMap<>();
		data.put("items", items);
		
		Engine engine = Engine.builder().addDefaults().addValueResolver(new ReflectionValueResolver()).build();
		
		Template template = engine.parse(convertStreamToString(TestLiteralQute.class.getResourceAsStream("literal.qute")));
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
