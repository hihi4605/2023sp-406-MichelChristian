package edu.millersville.csci406.spring2023;

/**
 * A rule found in a robots.txt file.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class RobotsRule {

	/** The protocol on which this rule applies. */
	private final String protocol;
	/** The name of the host to which this rule applies. */
	private final String hostName;
	/** The path prefix to which this rule applies. */
	private final String pathPrefix;
	/** Whether the rule explicitly allows (true) or disallows (false) paths matching the prefix. */
	private final boolean allowed;
	
	/**
	 * Constructs a new RobotsRule.
	 * 
	 * @param protocol The protocol on which the new rule applies.
	 * @param hostName The name of the host to which the new rule applies.
	 * @param pathPrefix The path prefix on which the new rule applies.
	 * @param allowed Whether the new rule explicitly allows (true) or disallows (false) paths matching the prefix.
	 */
	public RobotsRule(String protocol, String hostName, String pathPrefix, boolean allowed) {
		this.protocol = protocol;
		this.hostName = hostName;
		this.pathPrefix = pathPrefix;
		this.allowed = allowed;
	}
	
	/**
	 * Gets the protocol on which this RobotsRule applies.
	 * 
	 * @return The protocol on which this RobotsRule applies.
	 */
	public String getProtocol() {
		return protocol;
	}
	
	/**
	 * Gets the name of the host to which this RobotsRule applies.
	 * 
	 * @return The name of the host to which this RobotsRule applies.
	 */
	public String getHostName() {
		return hostName;
	}
	
	/**
	 * Gets the path prefix to which this RobotsRule applies.
	 * 
	 * @return The path prefix to which this RobotsRule applies.
	 */
	public String getPathPrefix() {
		return pathPrefix;
	}
	
	/**
	 * Gets whether this RobotsRule allows (true) or disallows (false) paths starting with the prefix.
	 * 
	 * @return Whether this RobotsRule allows (true) or disallows (false) paths starting with the prefix.
	 */
	public boolean isAllowed() {
		return allowed;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(protocol);
		str.append("://");
		str.append(hostName);
		str.append(pathPrefix);
		str.append(" ");
		str.append(allowed);
		return str.toString();
	}
	
	@Override
	public boolean equals(Object arg0) {
		boolean returnValue;
		if(this == arg0) {
			returnValue = true;
		}
		else if(arg0 == null) {
			returnValue = false;
		}
		else if(arg0 instanceof RobotsRule) {
			RobotsRule other = (RobotsRule)arg0;
			if(protocol.equals(other.protocol) && hostName.equals(other.hostName) && pathPrefix.equals(other.pathPrefix) && allowed == other.allowed) {
				returnValue = true;
			}
			else {
				returnValue = false;
			}
		}
		else {
			returnValue = false;
		}
		return returnValue;
	}
	
	@Override
	public int hashCode() {
		return protocol.hashCode() + (hostName.hashCode() * 3) + (pathPrefix.hashCode() * 7);
	}

}
