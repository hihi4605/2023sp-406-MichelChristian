package edu.millersville.csci406.spring2023;

/**
 * A helper class needed because different DataSource implementations will throw different types of checked exceptions.
 * It is a simple wrapper around whatever the implementation would have natively thrown.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class DataSourceException extends Exception {
	/** A version for serializibility. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new DataSourceException wrapping some other Throwable.
	 * 
	 * @param cause The original object that was thrown.
	 */
	public DataSourceException(Throwable cause) {
		super(cause);
	}
	
}
