package org.acme.qute;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@Path("/hello")
public class HelloResource {

    @Inject
    Template hello;

    @Inject
    Template goodbye;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("name") String name) {
    	hello.data(new Item(null, name)); // this parameter
    	hello.data("age", 12);
    	hello.data("height", 1.50, "weight", 50L);
        return hello.data("name", name);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get2(@QueryParam("name") String name) {
    	goodbye.data("age2", 12);
        return goodbye.data("name2", name);
    }
}
