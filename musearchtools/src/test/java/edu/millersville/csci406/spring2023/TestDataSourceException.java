package edu.millersville.csci406.spring2023;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * A collection of unit tests for the DataSourceException class.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class TestDataSourceException {

	/**
	 * Tests all features of DataSourceException.
	 */
	@Test
	public void testAll() {
		IllegalStateException realException = new IllegalStateException("nope!");
		DataSourceException exception = new DataSourceException(realException);
		assertEquals(exception.getCause(), realException);
	}
}
