package com.redhat.quarkus.ls.commons;

import java.util.function.Function;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.services.LanguageServer;

public class ParentProcessWatcher implements Runnable, Function<MessageConsumer, MessageConsumer> {

	private static final boolean isJava1x = System.getProperty("java.version").startsWith("1.");

	/**
	 * Exit code returned when XML Language Server is forced to exit.
	 */
	private static final int FORCED_EXIT_CODE = 1;

	private static final long INACTIVITY_DELAY_SECS = 30 * 1000;
	private static final int POLL_DELAY_SECS = 10;
	private volatile long lastActivityTime;
	private final ProcessLanguageServer server;
	private ScheduledFuture<?> task;
	private ScheduledExecutorService service;
	
	public interface ProcessLanguageServer extends LanguageServer {

		long getParentProcessId();

		void exit(int exitCode);
	}
	
	public ParentProcessWatcher(ProcessLanguageServer server) {
		this.server = server;
		service = Executors.newScheduledThreadPool(1);
		task = service.scheduleWithFixedDelay(this, POLL_DELAY_SECS, POLL_DELAY_SECS, TimeUnit.SECONDS);
	}
	
	@Override
	public MessageConsumer apply(final MessageConsumer consumer) {
		//inject our own consumer to refresh the timestamp
		return message -> {
			lastActivityTime=System.currentTimeMillis();
			consumer.consume(message);
		};
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
