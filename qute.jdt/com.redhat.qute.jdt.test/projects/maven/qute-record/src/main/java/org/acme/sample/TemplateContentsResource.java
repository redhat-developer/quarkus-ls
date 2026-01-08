package org.acme.sample;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateContents;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

public class TemplateContentsResource {
    
    @TemplateContents(value = "Hello {name}!")
    record Hello(String name) implements TemplateInstance {}

    @TemplateContents("""
            Hello {name}!
            """)
    record Hello2(String name) implements TemplateInstance {}

    @CheckedTemplate
    public static class Templates {
        @TemplateContents("Item is {item}")
        public static native TemplateInstance item(String item);  
    }

    @GET
    public TemplateInstance hello(@QueryParam("name") String name) {
        return new Hello(name); 
    }

    @GET
    public TemplateInstance hello2(@QueryParam("name") String name) {
        return new Hello2(name); 
    }
    
    @GET
    @Path("/item")
    public TemplateInstance item() {
        return Templates.item("foo"); 
    }
}
