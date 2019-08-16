/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverCapabilities;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.Assert;

import com.google.gson.Gson;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.settings.QuarkusCompletionSettings;
import com.redhat.quarkus.settings.QuarkusHoverSettings;

/**
 * Quarkus assert
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusAssert {

	private static QuarkusProjectInfo DEFAULT_PROJECT;

	public static QuarkusProjectInfo getDefaultQuarkusProjectInfo() {
		if (DEFAULT_PROJECT == null) {
			DEFAULT_PROJECT = new Gson().fromJson(
					"{\"quarkusProject\":true,\"projectName\":\"hibernate-orm-resteasy\",\"properties\":[{\"propertyName\":\"quarkus.thread-pool.core-threads\",\"type\":\"int\",\"defaultValue\":\"1\",\"docs\":\"The core thread pool size. This number of threads will always be kept alive.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.ThreadPoolConfig#coreThreads\",\"phase\":3},{\"propertyName\":\"quarkus.thread-pool.max-threads\",\"type\":\"java.util.OptionalInt\",\"docs\":\"The maximum number of threads. If this is not specified then\\n it will be automatically sized to 8 * the number of available processors\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.ThreadPoolConfig#maxThreads\",\"phase\":3},{\"propertyName\":\"quarkus.thread-pool.queue-size\",\"type\":\"java.util.OptionalInt\",\"docs\":\"The queue size. For most applications this should be unbounded\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.ThreadPoolConfig#queueSize\",\"phase\":3},{\"propertyName\":\"quarkus.thread-pool.growth-resistance\",\"type\":\"float\",\"defaultValue\":\"0\",\"docs\":\"The executor growth resistance.\\n\\n A resistance factor applied after the core pool is full; values applied here will cause that fraction\\n of submissions to create new threads when no idle thread is available. A value of {@code 0.0f} implies that\\n threads beyond the core size should be created as aggressively as threads within it; a value of {@code 1.0f}\\n implies that threads beyond the core size should never be created.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.ThreadPoolConfig#growthResistance\",\"phase\":3},{\"propertyName\":\"quarkus.thread-pool.shutdown-timeout\",\"type\":\"java.time.Duration\",\"defaultValue\":\"1M\",\"docs\":\"The shutdown timeout. If all pending work has not been completed by this time\\n then additional threads will be spawned to attempt to finish any pending tasks, and the shutdown process will\\n continue\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.ThreadPoolConfig#shutdownTimeout\",\"phase\":3},{\"propertyName\":\"quarkus.thread-pool.shutdown-interrupt\",\"type\":\"java.time.Duration\",\"defaultValue\":\"10\",\"docs\":\"The amount of time to wait for thread pool shutdown before tasks should be interrupted. If this value is\\n greater than or equal to the value for {@link #shutdownTimeout}, then tasks will not be interrupted before\\n the shutdown timeout occurs.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.ThreadPoolConfig#shutdownInterrupt\",\"phase\":3},{\"propertyName\":\"quarkus.thread-pool.shutdown-check-interval\",\"type\":\"java.util.Optional\\u003cjava.time.Duration\\u003e\",\"defaultValue\":\"5\",\"docs\":\"The frequency at which the status of the thread pool should be checked during shutdown. Information about\\n waiting tasks and threads will be checked and possibly logged at this interval. Setting this key to an empty\\n value disables the shutdown check interval.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.ThreadPoolConfig#shutdownCheckInterval\",\"phase\":3},{\"propertyName\":\"quarkus.thread-pool.keep-alive-time\",\"type\":\"java.time.Duration\",\"defaultValue\":\"30\",\"docs\":\"The amount of time a thread will stay alive with no work.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.ThreadPoolConfig#keepAliveTime\",\"phase\":3},{\"propertyName\":\"quarkus.log.category.{*}.min-level\",\"type\":\"java.lang.String\",\"defaultValue\":\"inherit\",\"docs\":\"The minimum level that this category can be set to\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.CategoryConfig#minLevel\",\"phase\":3},{\"propertyName\":\"quarkus.log.category.{*}.level\",\"type\":\"java.lang.String\",\"defaultValue\":\"inherit\",\"docs\":\"The log level level for this category\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.CategoryConfig#level\",\"phase\":3},{\"propertyName\":\"quarkus.log.filter.{*}.if-starts-with\",\"type\":\"java.util.List\\u003cjava.lang.String\\u003e\",\"defaultValue\":\"inherit\",\"docs\":\"The message starts to match\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.CleanupFilterConfig#ifStartsWith\",\"phase\":3},{\"propertyName\":\"quarkus.log.level\",\"type\":\"java.util.Optional\\u003cjava.util.logging.Level\\u003e\",\"docs\":\"The default log level\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.LogConfig#level\",\"phase\":3},{\"propertyName\":\"quarkus.log.min-level\",\"type\":\"java.util.logging.Level\",\"defaultValue\":\"INFO\",\"docs\":\"The default minimum log level\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.LogConfig#minLevel\",\"phase\":3},{\"propertyName\":\"quarkus.log.console.enable\",\"type\":\"boolean\",\"defaultValue\":\"true\",\"docs\":\"If console logging should be enabled\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.ConsoleConfig#enable\",\"phase\":3},{\"propertyName\":\"quarkus.log.console.format\",\"type\":\"java.lang.String\",\"defaultValue\":\"%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c{3.}] (%t) %s%e%n\",\"docs\":\"The log format\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.ConsoleConfig#format\",\"phase\":3},{\"propertyName\":\"quarkus.log.console.level\",\"type\":\"java.util.logging.Level\",\"defaultValue\":\"ALL\",\"docs\":\"The console log level\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.ConsoleConfig#level\",\"phase\":3},{\"propertyName\":\"quarkus.log.console.color\",\"type\":\"boolean\",\"defaultValue\":\"true\",\"docs\":\"If the console logging should be in color\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.ConsoleConfig#color\",\"phase\":3},{\"propertyName\":\"quarkus.log.console.darken\",\"type\":\"int\",\"defaultValue\":\"0\",\"docs\":\"Specify how much the colors should be darkened\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.ConsoleConfig#darken\",\"phase\":3},{\"propertyName\":\"quarkus.log.console.async\",\"type\":\"boolean\",\"docs\":\"Indicates whether to log asynchronously\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.AsyncConfig#enable\",\"phase\":3},{\"propertyName\":\"quarkus.log.console.async.queue-length\",\"type\":\"int\",\"defaultValue\":\"512\",\"docs\":\"The queue length to use before flushing writing\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.AsyncConfig#queueLength\",\"phase\":3},{\"propertyName\":\"quarkus.log.console.async.overflow\",\"type\":\"org.jboss.logmanager.handlers.AsyncHandler$OverflowAction\",\"defaultValue\":\"BLOCK\",\"docs\":\"Determine whether to block the publisher (rather than drop the message) when the queue is full\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.AsyncConfig#overflow\",\"phase\":3,\"enums\":[\"BLOCK\",\"DISCARD\"]},{\"propertyName\":\"quarkus.log.file.default_log_file_name\",\"type\":\"java.lang.String\",\"defaultValue\":\"\\u003c\\u003cno default\\u003e\\u003e\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.FileConfig#DEFAULT_LOG_FILE_NAME\",\"phase\":3},{\"propertyName\":\"quarkus.log.file.enable\",\"type\":\"boolean\",\"docs\":\"If file logging should be enabled\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.FileConfig#enable\",\"phase\":3},{\"propertyName\":\"quarkus.log.file.format\",\"type\":\"java.lang.String\",\"defaultValue\":\"%d{yyyy-MM-dd HH:mm:ss,SSS} %h %N[%i] %-5p [%c{3.}] (%t) %s%e%n\",\"docs\":\"The log format\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.FileConfig#format\",\"phase\":3},{\"propertyName\":\"quarkus.log.file.level\",\"type\":\"java.util.logging.Level\",\"defaultValue\":\"ALL\",\"docs\":\"The level of logs to be written into the file.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.FileConfig#level\",\"phase\":3},{\"propertyName\":\"quarkus.log.file.path\",\"type\":\"java.io.File\",\"defaultValue\":\"quarkus.log\",\"docs\":\"The name of the file in which logs will be written.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.FileConfig#path\",\"phase\":3},{\"propertyName\":\"quarkus.log.file.async\",\"type\":\"boolean\",\"docs\":\"Indicates whether to log asynchronously\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.AsyncConfig#enable\",\"phase\":3},{\"propertyName\":\"quarkus.log.file.async.queue-length\",\"type\":\"int\",\"defaultValue\":\"512\",\"docs\":\"The queue length to use before flushing writing\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.AsyncConfig#queueLength\",\"phase\":3},{\"propertyName\":\"quarkus.log.file.async.overflow\",\"type\":\"org.jboss.logmanager.handlers.AsyncHandler$OverflowAction\",\"defaultValue\":\"BLOCK\",\"docs\":\"Determine whether to block the publisher (rather than drop the message) when the queue is full\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.AsyncConfig#overflow\",\"phase\":3,\"enums\":[\"BLOCK\",\"DISCARD\"]},{\"propertyName\":\"quarkus.log.file.rotation.max-file-size\",\"type\":\"java.util.Optional\\u003cio.quarkus.runtime.configuration.MemorySize\\u003e\",\"docs\":\"The maximum file size of the log file after which a rotation is executed.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.FileConfig$RotationConfig#maxFileSize\",\"phase\":3},{\"propertyName\":\"quarkus.log.file.rotation.max-backup-index\",\"type\":\"int\",\"defaultValue\":\"1\",\"docs\":\"The maximum number of backups to keep.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.FileConfig$RotationConfig#maxBackupIndex\",\"phase\":3},{\"propertyName\":\"quarkus.log.file.rotation.file-suffix\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"File handler rotation file suffix.\\n\\n Example fileSuffix: .yyyy-MM-dd\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.FileConfig$RotationConfig#fileSuffix\",\"phase\":3},{\"propertyName\":\"quarkus.log.file.rotation.rotate-on-boot\",\"type\":\"boolean\",\"defaultValue\":\"true\",\"docs\":\"Indicates whether to rotate log files on server initialization.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.logging.FileConfig$RotationConfig#rotateOnBoot\",\"phase\":3},{\"propertyName\":\"quarkus.transaction-manager.node-name\",\"type\":\"java.lang.String\",\"defaultValue\":\"quarkus\",\"docs\":\"The node name used by the transaction manager\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-narayana-jta/0.19.1/quarkus-narayana-jta-0.19.1.jar\",\"source\":\"io.quarkus.narayana.jta.runtime.TransactionManagerConfiguration#nodeName\",\"phase\":3},{\"propertyName\":\"quarkus.transaction-manager.xa-node-name\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"The XA node name used by the transaction manager\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-narayana-jta/0.19.1/quarkus-narayana-jta-0.19.1.jar\",\"source\":\"io.quarkus.narayana.jta.runtime.TransactionManagerConfiguration#xaNodeName\",\"phase\":3},{\"propertyName\":\"quarkus.transaction-manager.default-transaction-timeout\",\"type\":\"java.util.Optional\\u003cjava.time.Duration\\u003e\",\"defaultValue\":\"60\",\"docs\":\"The default transaction timeout\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-narayana-jta/0.19.1/quarkus-narayana-jta-0.19.1.jar\",\"source\":\"io.quarkus.narayana.jta.runtime.TransactionManagerConfiguration#defaultTransactionTimeout\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.driver\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"The datasource driver class name\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceBuildTimeConfig#driver\",\"phase\":2},{\"propertyName\":\"quarkus.datasource.xa\",\"type\":\"boolean\",\"defaultValue\":\"false\",\"docs\":\"Whether we want to use XA.\\n \\u003cp\\u003e\\n If used, the driver has to support it.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceBuildTimeConfig#xa\",\"phase\":2},{\"propertyName\":\"quarkus.datasource.{*}.driver\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"The datasource driver class name\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceBuildTimeConfig#driver\",\"phase\":2},{\"propertyName\":\"quarkus.datasource.{*}.xa\",\"type\":\"boolean\",\"defaultValue\":\"false\",\"docs\":\"Whether we want to use XA.\\n \\u003cp\\u003e\\n If used, the driver has to support it.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceBuildTimeConfig#xa\",\"phase\":2},{\"propertyName\":\"quarkus.datasource.url\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"The datasource URL\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#url\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.username\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"The datasource username\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#username\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.password\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"The datasource password\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#password\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.initial-size\",\"type\":\"java.util.Optional\\u003cjava.lang.Integer\\u003e\",\"docs\":\"The initial size of the pool\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#initialSize\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.min-size\",\"type\":\"int\",\"defaultValue\":\"5\",\"docs\":\"The datasource pool minimum size\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#minSize\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.max-size\",\"type\":\"int\",\"defaultValue\":\"20\",\"docs\":\"The datasource pool maximum size\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#maxSize\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.background-validation-interval\",\"type\":\"java.util.Optional\\u003cjava.time.Duration\\u003e\",\"defaultValue\":\"2M\",\"docs\":\"The interval at which we validate idle connections in the background\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#backgroundValidationInterval\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.acquisition-timeout\",\"type\":\"java.util.Optional\\u003cjava.time.Duration\\u003e\",\"defaultValue\":\"5\",\"docs\":\"The timeout before cancelling the acquisition of a new connection\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#acquisitionTimeout\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.leak-detection-interval\",\"type\":\"java.util.Optional\\u003cjava.time.Duration\\u003e\",\"docs\":\"The interval at which we check for connection leaks.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#leakDetectionInterval\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.idle-removal-interval\",\"type\":\"java.util.Optional\\u003cjava.time.Duration\\u003e\",\"defaultValue\":\"5M\",\"docs\":\"The interval at which we try to remove idle connections.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#idleRemovalInterval\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.max-lifetime\",\"type\":\"java.util.Optional\\u003cjava.time.Duration\\u003e\",\"docs\":\"The max lifetime of a connection.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#maxLifetime\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.transaction-isolation-level\",\"type\":\"java.util.Optional\\u003cio.quarkus.agroal.runtime.TransactionIsolationLevel\\u003e\",\"docs\":\"The transaction isolation level.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#transactionIsolationLevel\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.enable-metrics\",\"type\":\"boolean\",\"docs\":\"Enable datasource metrics collection.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#enableMetrics\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.{*}.url\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"The datasource URL\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#url\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.{*}.username\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"The datasource username\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#username\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.{*}.password\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"The datasource password\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#password\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.{*}.initial-size\",\"type\":\"java.util.Optional\\u003cjava.lang.Integer\\u003e\",\"docs\":\"The initial size of the pool\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#initialSize\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.{*}.min-size\",\"type\":\"int\",\"defaultValue\":\"5\",\"docs\":\"The datasource pool minimum size\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#minSize\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.{*}.max-size\",\"type\":\"int\",\"defaultValue\":\"20\",\"docs\":\"The datasource pool maximum size\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#maxSize\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.{*}.background-validation-interval\",\"type\":\"java.util.Optional\\u003cjava.time.Duration\\u003e\",\"defaultValue\":\"2M\",\"docs\":\"The interval at which we validate idle connections in the background\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#backgroundValidationInterval\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.{*}.acquisition-timeout\",\"type\":\"java.util.Optional\\u003cjava.time.Duration\\u003e\",\"defaultValue\":\"5\",\"docs\":\"The timeout before cancelling the acquisition of a new connection\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#acquisitionTimeout\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.{*}.leak-detection-interval\",\"type\":\"java.util.Optional\\u003cjava.time.Duration\\u003e\",\"docs\":\"The interval at which we check for connection leaks.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#leakDetectionInterval\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.{*}.idle-removal-interval\",\"type\":\"java.util.Optional\\u003cjava.time.Duration\\u003e\",\"defaultValue\":\"5M\",\"docs\":\"The interval at which we try to remove idle connections.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#idleRemovalInterval\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.{*}.max-lifetime\",\"type\":\"java.util.Optional\\u003cjava.time.Duration\\u003e\",\"docs\":\"The max lifetime of a connection.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#maxLifetime\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.{*}.transaction-isolation-level\",\"type\":\"java.util.Optional\\u003cio.quarkus.agroal.runtime.TransactionIsolationLevel\\u003e\",\"docs\":\"The transaction isolation level.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#transactionIsolationLevel\",\"phase\":3},{\"propertyName\":\"quarkus.datasource.{*}.enable-metrics\",\"type\":\"boolean\",\"docs\":\"Enable datasource metrics collection.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-agroal/0.19.1/quarkus-agroal-0.19.1.jar\",\"source\":\"io.quarkus.agroal.runtime.DataSourceRuntimeConfig#enableMetrics\",\"phase\":3},{\"propertyName\":\"quarkus.http.cors\",\"type\":\"boolean\",\"docs\":\"The CORS config\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-undertow/0.19.1/quarkus-undertow-0.19.1.jar\",\"source\":\"io.quarkus.undertow.runtime.HttpBuildConfig#corsEnabled\",\"phase\":2},{\"propertyName\":\"quarkus.http.port\",\"type\":\"int\",\"defaultValue\":\"8080\",\"docs\":\"The HTTP port\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-undertow/0.19.1/quarkus-undertow-0.19.1.jar\",\"source\":\"io.quarkus.undertow.runtime.HttpConfig#port\",\"phase\":3},{\"propertyName\":\"quarkus.http.ssl-port\",\"type\":\"int\",\"defaultValue\":\"8443\",\"docs\":\"The HTTPS port\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-undertow/0.19.1/quarkus-undertow-0.19.1.jar\",\"source\":\"io.quarkus.undertow.runtime.HttpConfig#sslPort\",\"phase\":3},{\"propertyName\":\"quarkus.http.test-port\",\"type\":\"int\",\"defaultValue\":\"8081\",\"docs\":\"The HTTP port used to run tests\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-undertow/0.19.1/quarkus-undertow-0.19.1.jar\",\"source\":\"io.quarkus.undertow.runtime.HttpConfig#testPort\",\"phase\":3},{\"propertyName\":\"quarkus.http.test-ssl-port\",\"type\":\"int\",\"defaultValue\":\"8444\",\"docs\":\"The HTTPS port used to run tests\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-undertow/0.19.1/quarkus-undertow-0.19.1.jar\",\"source\":\"io.quarkus.undertow.runtime.HttpConfig#testSslPort\",\"phase\":3},{\"propertyName\":\"quarkus.http.host\",\"type\":\"java.lang.String\",\"defaultValue\":\"0.0.0.0\",\"docs\":\"The HTTP host\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-undertow/0.19.1/quarkus-undertow-0.19.1.jar\",\"source\":\"io.quarkus.undertow.runtime.HttpConfig#host\",\"phase\":3},{\"propertyName\":\"quarkus.http.io-threads\",\"type\":\"java.util.OptionalInt\",\"docs\":\"The number if IO threads used to perform IO. This will be automatically set to a reasonable value based on\\n the number of CPU cores if it is not provided\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-undertow/0.19.1/quarkus-undertow-0.19.1.jar\",\"source\":\"io.quarkus.undertow.runtime.HttpConfig#ioThreads\",\"phase\":3},{\"propertyName\":\"quarkus.http.ssl.certificate.file\",\"type\":\"java.util.Optional\\u003cjava.nio.file.Path\\u003e\",\"docs\":\"The file path to a server certificate or certificate chain in PEM format.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.configuration.ssl.CertificateConfig#file\",\"phase\":3},{\"propertyName\":\"quarkus.http.ssl.certificate.key-file\",\"type\":\"java.util.Optional\\u003cjava.nio.file.Path\\u003e\",\"docs\":\"The file path to the corresponding certificate private key file in PEM format.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.configuration.ssl.CertificateConfig#keyFile\",\"phase\":3},{\"propertyName\":\"quarkus.http.ssl.certificate.key-store-file\",\"type\":\"java.util.Optional\\u003cjava.nio.file.Path\\u003e\",\"docs\":\"An optional key store which holds the certificate information instead of specifying separate files.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.configuration.ssl.CertificateConfig#keyStoreFile\",\"phase\":3},{\"propertyName\":\"quarkus.http.ssl.certificate.key-store-file-type\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"An optional parameter to specify type of the key store file. If not given, the type is automatically detected\\n based on the file name.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.configuration.ssl.CertificateConfig#keyStoreFileType\",\"phase\":3},{\"propertyName\":\"quarkus.http.ssl.certificate.key-store-password\",\"type\":\"java.lang.String\",\"defaultValue\":\"password\",\"docs\":\"A parameter to specify the password of the key store file. If not given, the default (\\\"password\\\") is used.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.configuration.ssl.CertificateConfig#keyStorePassword\",\"phase\":3},{\"propertyName\":\"quarkus.http.ssl.cipher-suites\",\"type\":\"java.util.Optional\\u003corg.wildfly.security.ssl.CipherSuiteSelector\\u003e\",\"docs\":\"The cipher suites to use. If none is given, a reasonable default is selected.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.configuration.ssl.ServerSslConfig#cipherSuites\",\"phase\":3},{\"propertyName\":\"quarkus.http.ssl.protocols\",\"type\":\"java.util.List\\u003corg.wildfly.security.ssl.Protocol\\u003e\",\"defaultValue\":\"TLSv1.3,TLSv1.2\",\"docs\":\"The list of protocols to explicitly enable.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.configuration.ssl.ServerSslConfig#protocols\",\"phase\":3},{\"propertyName\":\"quarkus.http.ssl.provider-name\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"The SSL provider name to use. If none is given, the platform default is used.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.configuration.ssl.ServerSslConfig#providerName\",\"phase\":3},{\"propertyName\":\"quarkus.http.ssl.session-cache-size\",\"type\":\"java.util.OptionalInt\",\"docs\":\"The SSL session cache size. If not given, the platform default is used.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.configuration.ssl.ServerSslConfig#sessionCacheSize\",\"phase\":3},{\"propertyName\":\"quarkus.http.ssl.session-timeout\",\"type\":\"java.util.Optional\\u003cjava.time.Duration\\u003e\",\"docs\":\"The SSL session cache timeout. If not given, the platform default is used.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.19.1/quarkus-core-0.19.1.jar\",\"source\":\"io.quarkus.runtime.configuration.ssl.ServerSslConfig#sessionTimeout\",\"phase\":3},{\"propertyName\":\"quarkus.http.cors.origins\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"Origins allowed for CORS\\n\\n Comma separated list of valid URLs. ex: http://www.quarkus.io,http://localhost:3000\\n The filter allows any origin if this is not set.\\n\\n default: returns any requested origin as valid\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-undertow/0.19.1/quarkus-undertow-0.19.1.jar\",\"source\":\"io.quarkus.undertow.runtime.filters.CORSConfig#origins\",\"phase\":3},{\"propertyName\":\"quarkus.http.cors.methods\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"HTTP methods allowed for CORS\\n\\n Comma separated list of valid methods. ex: GET,PUT,POST\\n The filter allows any method if this is not set.\\n\\n default: returns any requested method as valid\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-undertow/0.19.1/quarkus-undertow-0.19.1.jar\",\"source\":\"io.quarkus.undertow.runtime.filters.CORSConfig#methods\",\"phase\":3},{\"propertyName\":\"quarkus.http.cors.headers\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"HTTP headers allowed for CORS\\n\\n Comma separated list of valid headers. ex: X-Custom,Content-Disposition\\n The filter allows any header if this is not set.\\n\\n default: returns any requested header as valid\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-undertow/0.19.1/quarkus-undertow-0.19.1.jar\",\"source\":\"io.quarkus.undertow.runtime.filters.CORSConfig#headers\",\"phase\":3},{\"propertyName\":\"quarkus.http.cors.exposed-headers\",\"type\":\"java.util.Optional\\u003cjava.lang.String\\u003e\",\"docs\":\"HTTP headers exposed in CORS\\n\\n Comma separated list of valid headers. ex: X-Custom,Content-Disposition\\n \\n default: \\u003cempty\\u003e\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-undertow/0.19.1/quarkus-undertow-0.19.1.jar\",\"source\":\"io.quarkus.undertow.runtime.filters.CORSConfig#exposedHeaders\",\"phase\":3},{\"propertyName\":\"quarkus.application.name\",\"type\":\"java.lang.String\",\"docs\":\"The name of the application.\\n If not set, defaults to the name of the project.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core-deployment/0.19.1/quarkus-core-deployment-0.19.1.jar\",\"source\":\"io.quarkus.deployment.ApplicationConfig#name\",\"phase\":1},{\"propertyName\":\"quarkus.application.version\",\"type\":\"java.lang.String\",\"docs\":\"The version of the application.\\n If not set, defaults to the version of the project\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core-deployment/0.19.1/quarkus-core-deployment-0.19.1.jar\",\"source\":\"io.quarkus.deployment.ApplicationConfig#version\",\"phase\":1},{\"propertyName\":\"quarkus.jni.library-paths\",\"type\":\"java.util.List\\u003cjava.lang.String\\u003e\",\"docs\":\"Paths of library to load.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core-deployment/0.19.1/quarkus-core-deployment-0.19.1.jar\",\"source\":\"io.quarkus.deployment.JniProcessor$JniConfig#libraryPaths\",\"phase\":1},{\"propertyName\":\"quarkus.jni.enable\",\"type\":\"boolean\",\"defaultValue\":\"false\",\"docs\":\"Enable JNI support.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core-deployment/0.19.1/quarkus-core-deployment-0.19.1.jar\",\"source\":\"io.quarkus.deployment.JniProcessor$JniConfig#enable\",\"phase\":1},{\"propertyName\":\"quarkus.ssl.native\",\"type\":\"java.util.Optional\\u003cjava.lang.Boolean\\u003e\",\"docs\":\"Enable native SSL support.\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core-deployment/0.19.1/quarkus-core-deployment-0.19.1.jar\",\"source\":\"io.quarkus.deployment.SslProcessor$SslConfig#native_\",\"phase\":1},{\"propertyName\":\"quarkus.index-dependency.{*}.group-id\",\"type\":\"java.lang.String\",\"docs\":\"The maven groupId of the artifact to index\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core-deployment/0.19.1/quarkus-core-deployment-0.19.1.jar\",\"source\":\"io.quarkus.deployment.index.IndexDependencyConfig#groupId\",\"phase\":1},{\"propertyName\":\"quarkus.index-dependency.{*}.artifact-id\",\"type\":\"java.lang.String\",\"docs\":\"The maven artifactId of the artifact to index\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core-deployment/0.19.1/quarkus-core-deployment-0.19.1.jar\",\"source\":\"io.quarkus.deployment.index.IndexDependencyConfig#artifactId\",\"phase\":1},{\"propertyName\":\"quarkus.index-dependency.{*}.classifier\",\"type\":\"java.lang.String\",\"docs\":\"The maven classifier of the artifact to index\",\"location\":\"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core-deployment/0.19.1/quarkus-core-deployment-0.19.1.jar\",\"source\":\"io.quarkus.deployment.index.IndexDependencyConfig#classifier\",\"phase\":1}]}",
					QuarkusProjectInfo.class);
		}
		return DEFAULT_PROJECT;
	}

	// ------------------- Completion assert

	public static void testCompletionFor(String value, boolean snippetSupport, CompletionItem... expectedItems)
			throws BadLocationException {
		testCompletionFor(value, snippetSupport, null, expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, Integer expectedCount,
			CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(value, snippetSupport, null, expectedCount, getDefaultQuarkusProjectInfo(), expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, String fileURI, Integer expectedCount,
			QuarkusProjectInfo projectInfo, CompletionItem... expectedItems) throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		TextDocument document = new TextDocument(value, fileURI != null ? fileURI : "application.properties");
		PropertiesModel model = PropertiesModel.parse(document);
		Position position = model.positionAt(offset);

		// Add snippet support for completion
		QuarkusCompletionSettings completionSettings = new QuarkusCompletionSettings();
		CompletionItemCapabilities completionItemCapabilities = new CompletionItemCapabilities();
		completionItemCapabilities.setSnippetSupport(snippetSupport);
		CompletionCapabilities completionCapabilities = new CompletionCapabilities(completionItemCapabilities);
		completionSettings.setCapabilities(completionCapabilities);

		QuarkusLanguageService languageService = new QuarkusLanguageService();
		CompletionList list = languageService.doComplete(model, position, projectInfo, completionSettings, () -> {
		});

		// no duplicate labels
		List<String> labels = list.getItems().stream().map(i -> i.getLabel()).sorted().collect(Collectors.toList());
		String previous = null;
		for (String label : labels) {
			Assert.assertTrue(
					"Duplicate label " + label + " in " + labels.stream().collect(Collectors.joining(",")) + "}",
					previous != label);
			previous = label;
		}
		if (expectedCount != null) {
			Assert.assertEquals(expectedCount.intValue(), list.getItems().size());
		}
		if (expectedItems != null) {
			for (CompletionItem item : expectedItems) {
				assertCompletion(list, item, document, offset);
			}
		}
	}

	private static void assertCompletion(CompletionList completions, CompletionItem expected, TextDocument document,
			int offset) {
		List<CompletionItem> matches = completions.getItems().stream().filter(completion -> {
			return expected.getLabel().equals(completion.getLabel());
		}).collect(Collectors.toList());

		Assert.assertEquals(
				expected.getLabel() + " should only exist once: Actual: "
						+ completions.getItems().stream().map(c -> c.getLabel()).collect(Collectors.joining(",")),
				1, matches.size());

		CompletionItem match = matches.get(0);
		/*
		 * if (expected.documentation != null) {
		 * Assert.assertEquals(match.getDocumentation().getRight().getValue(),
		 * expected.getd); } if (expected.kind) { Assert.assertEquals(match.kind,
		 * expected.kind); }
		 */
		// if (expected.getTextEdit() != null && match.getTextEdit() != null) {
		if (expected.getTextEdit().getNewText() != null) {
			Assert.assertEquals(expected.getTextEdit().getNewText(), match.getTextEdit().getNewText());
		}
		Range r = expected.getTextEdit().getRange();
		if (r != null && r.getStart() != null && r.getEnd() != null) {
			Assert.assertEquals(expected.getTextEdit().getRange(), match.getTextEdit().getRange());
		}
		// }
		if (expected.getFilterText() != null && match.getFilterText() != null) {
			Assert.assertEquals(expected.getFilterText(), match.getFilterText());
		}

		if (expected.getDocumentation() != null) {
			Assert.assertEquals(expected.getDocumentation(), match.getDocumentation());
		}

	}

	public static CompletionItem c(String label, TextEdit textEdit, String filterText, String documentation) {
		return c(label, textEdit, filterText, documentation, null);
	}

	public static CompletionItem c(String label, TextEdit textEdit, String filterText, String documentation,
			String kind) {
		CompletionItem item = new CompletionItem();
		item.setLabel(label);
		item.setFilterText(filterText);
		item.setTextEdit(textEdit);
		if (kind == null) {
			item.setDocumentation(documentation);
		} else {
			item.setDocumentation(new MarkupContent(kind, documentation));
		}
		return item;
	}

	public static CompletionItem c(String label, TextEdit textEdit, String filterText) {
		CompletionItem item = new CompletionItem();
		item.setLabel(label);
		item.setFilterText(filterText);
		item.setTextEdit(textEdit);
		return item;
	}

	public static CompletionItem c(String label, String newText, Range range) {
		return c(label, new TextEdit(range, newText), null);
	}

	public static Range r(int line, int startChar, int endChar) {
		Position start = new Position(line, startChar);
		Position end = new Position(line, endChar);
		return new Range(start, end);
	}

	// ------------------- Hover assert

	public static void assertHoverMarkdown(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {

		QuarkusHoverSettings hoverSettings = new QuarkusHoverSettings();
		HoverCapabilities capabilities = new HoverCapabilities(Arrays.asList(MarkupKind.MARKDOWN), false);
		hoverSettings.setCapabilities(capabilities);

		assertHover(value, null, getDefaultQuarkusProjectInfo(), hoverSettings, expectedHoverLabel,
				expectedHoverOffset);
	}

	public static void assertHoverPlaintext(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {

		QuarkusHoverSettings hoverSettings = new QuarkusHoverSettings();
		HoverCapabilities capabilities = new HoverCapabilities(Arrays.asList(MarkupKind.PLAINTEXT), false);
		hoverSettings.setCapabilities(capabilities);

		assertHover(value, null, getDefaultQuarkusProjectInfo(), hoverSettings, expectedHoverLabel,
				expectedHoverOffset);
	}

	public static void assertHover(String value, String fileURI, QuarkusProjectInfo projectInfo,
			QuarkusHoverSettings hoverSettings, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {

		int offset = value.indexOf("|");
		value = value.substring(0, offset) + value.substring(offset + 1);

		TextDocument document = new TextDocument(value, fileURI != null ? fileURI : "test://test/test.html");
		PropertiesModel model = PropertiesModel.parse(document);
		Position position = model.positionAt(offset);

		QuarkusLanguageService languageService = new QuarkusLanguageService();

		Hover hover = languageService.doHover(model, position, projectInfo, hoverSettings);
		if (expectedHoverLabel == null) {
			Assert.assertNull(hover);
		} else {
			String actualHoverLabel = getHoverLabel(hover);
			Assert.assertEquals(expectedHoverLabel, actualHoverLabel);
			if (expectedHoverOffset != null) {
				Assert.assertNotNull(hover.getRange());
				Assert.assertNotNull(hover.getRange().getStart());
				Assert.assertEquals(expectedHoverOffset.intValue(), hover.getRange().getStart().getCharacter());
			}
		}
	}

	private static String getHoverLabel(Hover hover) {
		Either<List<Either<String, MarkedString>>, MarkupContent> contents = hover != null ? hover.getContents() : null;
		if (contents == null) {
			return null;
		}
		return contents.getRight().getValue();
	}

}
