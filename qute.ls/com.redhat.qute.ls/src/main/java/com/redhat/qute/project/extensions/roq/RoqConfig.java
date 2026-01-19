package com.redhat.qute.project.extensions.roq;

import com.redhat.qute.project.config.PropertyConfig;

public class RoqConfig {
	
	public static final PropertyConfig ROG_DIR = new PropertyConfig("quarkus.roq.dir", "");
	
	public static final PropertyConfig ROG_DATA_DIR = new PropertyConfig("quarkus.roq.data.dir", "data");
}
