package org.razkevich.logging;

import ch.qos.logback.classic.db.names.SimpleDBNameResolver;

public class CustomNameResolver extends SimpleDBNameResolver {
	public CustomNameResolver() {
		setTableNamePrefix("ST_");
	}
}
