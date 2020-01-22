package com.redhat.microprofile.jdt.core;

import static com.redhat.microprofile.commons.metadata.ItemMetadata.CONFIG_PHASE_BUILD_TIME;
import static com.redhat.microprofile.commons.metadata.ItemMetadata.CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED;
import static com.redhat.microprofile.commons.metadata.ItemMetadata.CONFIG_PHASE_RUN_TIME;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;

import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;


/**
 * Tests if <code>boolean</code> and <code>int</code> @ConfigItem properties without the 
 * <code>defaultValue</code> annotation, has a default value of <code>false</code> 
 * and <code>0</code> respectively 
 */
public class ConfigItemIntBoolDefaultValueTest extends BasePropertiesManagerTest {
	
	
	@Test
	public void configItemIntBoolDefaultValueTest() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.config_quickstart, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);
		
		String booleanDefault = "false";
		String intDefault = "0";

		assertProperties(infoFromClasspath, 185 /* properties from JAR */ + //
				3 /* properties from Java sources with ConfigProperty */ + //
				2 /* properties from Java sources with ConfigRoot */,
				
				// @ConfigItem(name = ConfigItem.PARENT)
				// boolean enable;
				p("quarkus-core", "quarkus.log.console.async", "boolean",
						"Indicates whether to log asynchronously", true,
						"io.quarkus.runtime.logging.AsyncConfig", "enable", null, CONFIG_PHASE_RUN_TIME, booleanDefault),


				// @ConfigItem
				// boolean enable;
				p("quarkus-core", "quarkus.log.file.enable", "boolean",
						"If file logging should be enabled", true,
						"io.quarkus.runtime.logging.FileConfig", "enable", null, CONFIG_PHASE_RUN_TIME, booleanDefault),

				// @ConfigItem(name = ConfigItem.PARENT)
				// boolean enable;
				p("quarkus-core", "quarkus.log.file.async", "boolean",
						"Indicates whether to log asynchronously", true,
						"io.quarkus.runtime.logging.AsyncConfig", "enable", null, CONFIG_PHASE_RUN_TIME, booleanDefault),
				
				// @ConfigItem
				// boolean enable;
				p("quarkus-core", "quarkus.log.syslog.enable", "boolean",
						"If syslog logging should be enabled", true,
						"io.quarkus.runtime.logging.SyslogConfig", "enable", null, CONFIG_PHASE_RUN_TIME, booleanDefault),
				
				
				// @ConfigItem
				// public boolean useCountingFraming;
				p("quarkus-core", "quarkus.log.syslog.use-counting-framing", "boolean",
						"Set to {@code true} if the message being sent should be prefixed with the size of the message", true,
						"io.quarkus.runtime.logging.SyslogConfig", "useCountingFraming", null, CONFIG_PHASE_RUN_TIME, booleanDefault),
				
				// @ConfigItem
				// boolean blockOnReconnect;
				p("quarkus-core", "quarkus.log.syslog.block-on-reconnect", "boolean",
						"Enables or disables blocking when attempting to reconnect a\n" + 
						"{@link org.jboss.logmanager.handlers.SyslogHandler.Protocol#TCP\n" +
						"TCP} or {@link org.jboss.logmanager.handlers.SyslogHandler.Protocol#SSL_TCP SSL TCP} protocol", true,
						"io.quarkus.runtime.logging.SyslogConfig", "blockOnReconnect", null, CONFIG_PHASE_RUN_TIME, booleanDefault),
				
				
				// @ConfigItem(name = ConfigItem.PARENT)
				// boolean enable;
				p("quarkus-core", "quarkus.log.syslog.async", "boolean",
						"Indicates whether to log asynchronously", true,
						"io.quarkus.runtime.logging.AsyncConfig", "enable", null, CONFIG_PHASE_RUN_TIME, booleanDefault),
				
				// @ConfigItem
				// public boolean enabled;
				p("quarkus-resteasy-common", "quarkus.resteasy.gzip.enabled", "boolean",
						"If gzip is enabled", true,
						"io.quarkus.resteasy.common.deployment.ResteasyCommonProcessor.ResteasyCommonConfigGzip", "enabled", null, CONFIG_PHASE_BUILD_TIME, booleanDefault),
				
				// @ConfigItem
				// public boolean basic;
				p("quarkus-vertx-http", "quarkus.http.auth.basic", "boolean",
						"If basic auth should be enabled. If both basic and form auth is enabled then basic auth will be enabled in silent mode.\n" + 
						"\n" + 
						"If no authentication mechanisms are configured basic auth is the default, unless an\n" +
						"{@link io.quarkus.security.identity.IdentityProvider}\n" +
						"is present that supports {@link io.quarkus.security.identity.request.TokenAuthenticationRequest} in which case\n" +
						"form auth will be the default.", true,
						"io.quarkus.vertx.http.runtime.AuthConfig", "basic", null, CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED, booleanDefault),
				
				// @ConfigItem
				// public boolean enabled;
				p("quarkus-vertx-http", "quarkus.http.auth.form.enabled", "boolean",
						"If form authentication is enabled", true,
						"io.quarkus.vertx.http.runtime.FormAuthConfig", "enabled", null, CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED, booleanDefault),
				
				// @ConfigItem
				// public boolean useAsyncDNS;
				p("quarkus-vertx-core", "quarkus.vertx.use-async-dns", "boolean",
						"Enables the async DNS resolver.", true,
						"io.quarkus.vertx.core.runtime.config.VertxConfiguration", "useAsyncDNS", null, CONFIG_PHASE_RUN_TIME, booleanDefault),
				
				// @ConfigItem
				// public int reconnectAttempts;
				p("quarkus-vertx-core", "quarkus.vertx.eventbus.reconnect-attempts", "int",
						"The number of reconnection attempts.", true,
						"io.quarkus.vertx.core.runtime.config.EventBusConfiguration", "reconnectAttempts", null, CONFIG_PHASE_RUN_TIME, intDefault),
				
				// @ConfigItem
				// public boolean reusePort;
				p("quarkus-vertx-core", "quarkus.vertx.eventbus.reuse-port", "boolean",
						"Whether or not to reuse the port.", true,
						"io.quarkus.vertx.core.runtime.config.EventBusConfiguration", "reusePort", null, CONFIG_PHASE_RUN_TIME, booleanDefault),
				
				// @ConfigItem
				// public boolean ssl;
				p("quarkus-vertx-core", "quarkus.vertx.eventbus.ssl", "boolean",
						"Enables or Disabled SSL.", true,
						"io.quarkus.vertx.core.runtime.config.EventBusConfiguration", "ssl", null, CONFIG_PHASE_RUN_TIME, booleanDefault),
				
				// @ConfigItem
				// public boolean tcpKeepAlive;
				p("quarkus-vertx-core", "quarkus.vertx.eventbus.tcp-keep-alive", "boolean",
						"Whether or not to keep the TCP connection opened (keep-alive).", true,
						"io.quarkus.vertx.core.runtime.config.EventBusConfiguration", "tcpKeepAlive", null, CONFIG_PHASE_RUN_TIME, booleanDefault),
				
				// @ConfigItem
				// public boolean trustAll;
				p("quarkus-vertx-core", "quarkus.vertx.eventbus.trust-all", "boolean",
						"Enables or disables the trust all parameter.", true,
						"io.quarkus.vertx.core.runtime.config.EventBusConfiguration", "trustAll", null, CONFIG_PHASE_RUN_TIME, booleanDefault),

				// @ConfigItem
				// public boolean clustered;
				p("quarkus-vertx-core", "quarkus.vertx.cluster.clustered", "boolean",
						"Enables or disables the clustering.", true,
						"io.quarkus.vertx.core.runtime.config.ClusterConfiguration", "clustered", null, CONFIG_PHASE_RUN_TIME, booleanDefault));

		assertPropertiesDuplicate(infoFromClasspath);
	}
}
