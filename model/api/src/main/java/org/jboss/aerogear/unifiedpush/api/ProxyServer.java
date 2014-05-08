package org.jboss.aerogear.unifiedpush.api;

public class ProxyServer extends BaseModel {
	private static final long serialVersionUID = -5160851795066898019L;
	
	private String host;
	private int port;
	
	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
}