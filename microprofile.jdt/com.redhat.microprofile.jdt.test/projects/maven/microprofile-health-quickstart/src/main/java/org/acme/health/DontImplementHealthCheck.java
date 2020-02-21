package org.acme.health;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class DontImplementHealthCheck  /*implements HealthCheck*/ {

	public HealthCheckResponse call() {
		return null;
	}

}
