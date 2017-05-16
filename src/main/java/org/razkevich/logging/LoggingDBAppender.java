package org.razkevich.logging;

import ch.qos.logback.classic.db.DBAppender;
import ch.qos.logback.classic.db.DBHelper;
import ch.qos.logback.classic.db.names.DBNameResolver;
import ch.qos.logback.classic.db.names.TableName;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LoggingDBAppender extends DBAppender {

	public static final int LENGTH_THRESHOLD = 254;
	public static final String DEFAULT_LOG_TABLE_NAME = "LOGGING_EVENT";

	@Override
	public void start() {
		super.start();
		String logTableName;
		try {
			logTableName = ((DBNameResolver) FieldUtils.readField(this, "dbNameResolver", true)).getTableName(TableName.LOGGING_EVENT);
		} catch (IllegalAccessException e) {
			logTableName = DEFAULT_LOG_TABLE_NAME;
		}
		insertSQL = "INSERT INTO " + logTableName +
				" (TIMESTMP, FORMATTED_MESSAGE, LOGGER_NAME, LEVEL_STRING, REFERENCE_FLAG, CALLER_FILENAME, CALLER_CLASS, CALLER_METHOD, CALLER_LINE, EVENT_ID)" +
				" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ST_LOGGING_EVENT_SEQ.nextVal)";
	}

	@Override
	protected void subAppend(ILoggingEvent event, Connection connection, PreparedStatement stmt) throws SQLException {
		stmt.setLong(1, event.getTimeStamp());
		stmt.setString(2, event.getFormattedMessage() != null ? event.getFormattedMessage() : "NULL");
		stmt.setString(3, StringUtils.left(event.getLoggerName(), LENGTH_THRESHOLD));
		stmt.setString(4, StringUtils.left(event.getLevel().toString(), LENGTH_THRESHOLD));
		stmt.setShort(5, DBHelper.computeReferenceMask(event));
		StackTraceElement callerData = event.getCallerData()[0];
		if (callerData != null) {
			stmt.setString(6, StringUtils.left(callerData.getFileName(), LENGTH_THRESHOLD));
			stmt.setString(7, StringUtils.left(callerData.getClassName(), LENGTH_THRESHOLD));
			stmt.setString(8, StringUtils.left(callerData.getMethodName(), LENGTH_THRESHOLD));
			stmt.setString(9, Integer.toString(callerData.getLineNumber()));
		}
		if (stmt.executeUpdate() != 1) {
			addWarn("Failed to insert loggingEvent");
		}
	}
}
