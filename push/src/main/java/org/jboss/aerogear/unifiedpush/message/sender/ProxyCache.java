/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.sender;

import org.jboss.aerogear.unifiedpush.api.ProxyServer;

public final class ProxyCache {
	// Singleton DP
	private ProxyCache() {
	}
	
	private static ProxyCache instance = null;
	
	public static ProxyCache getInstance() {
		if (instance == null) {
			instance = new ProxyCache();
		}
		return instance;
	}

	// Proxy Cache class
	private ProxyServer proxyServer;
	
	public ProxyServer getProxyServer() {
		return proxyServer;
	}

	public void setProxyServer(ProxyServer proxyServer) {
		this.proxyServer = proxyServer;
	}
}
