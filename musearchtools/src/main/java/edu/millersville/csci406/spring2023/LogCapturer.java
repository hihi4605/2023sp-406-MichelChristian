package edu.millersville.csci406.spring2023;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * A class that allows you to override the normal logging handlers to instead read all logged information from a String.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class LogCapturer {

	/** An OutputStream to which logged messages will be printed. */
	private ByteArrayOutputStream outputStream;
	/** A Handler that redirects logged messages to outputStream. */
	private StreamHandler logHandler;
	/** An array of the old Handlers. */
	private Handler [] oldHandlers;
	/** The root Logger. */
	private Logger rootLogger;
	/** Whether or not this LogCapturer is initialized. */
	private boolean initialized;

	/**
	 * Constructs a new LogCapturer.
	 */
	public LogCapturer() {
		outputStream = new ByteArrayOutputStream();
		rootLogger = Logger.getLogger("");
		logHandler = new StreamHandler(outputStream, rootLogger.getHandlers()[0].getFormatter());
		initialized = false;
	}
	
	/**
	 * Begins capturing all logged messages to this LogCapturer.
	 */
	public void startCapture() {
		if(initialized) {
			throw new IllegalStateException("The LogCapturer has already started!");
		}
		oldHandlers = Arrays.copyOf(rootLogger.getHandlers(), rootLogger.getHandlers().length);
		for(Handler oldHandler : oldHandlers) {
			rootLogger.removeHandler(oldHandler);
		}
		rootLogger.addHandler(logHandler);
		initialized = true;
		outputStream.reset();
	}
	
	/**
	 * Gets a String containing all messages captured since this method was last called.
	 * 
	 * @return A String containing all messages that were captured since this method was last called.
	 */
	public String getMessages() {
		logHandler.flush();
		String loggedOutput = outputStream.toString();
		outputStream.reset();
		return loggedOutput;
	}
	
	/**
	 * Stops capturing logged messages to this LogCapturer.
	 */
	public void endCapture() {
		if(!initialized) {
			throw new IllegalStateException("The LogCapturer was not started!");
		}
		rootLogger.removeHandler(logHandler);
		for(Handler oldHandler : oldHandlers) {
			rootLogger.addHandler(oldHandler);
		}
		initialized = false;
	}
}
