package org.jboss.aerogear.unifiedpush.message.sender;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import org.jboss.aerogear.unifiedpush.api.ProxyServer;

import com.google.android.gcm.server.Sender;

public class GCMProxySender extends Sender {
	
	public GCMProxySender(String key) {
		super(key);
	}

	@Override
    protected HttpURLConnection getConnection(String url) throws IOException {
		HttpURLConnection conn;
		
		ProxyServer proxyServer = ProxyCache.getInstance().getProxyServer();
		
		// set proxy
		if (proxyServer != null) {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyServer.getHost(), proxyServer.getPort()));
			conn = (HttpURLConnection) new URL(url).openConnection(proxy);
		} else {
			conn = (HttpURLConnection) new URL(url).openConnection();
		}
		
		return conn;
    }

}
