/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.notnoop.apns;

import java.io.InputStream;
import java.net.Proxy;
import java.net.Socket;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLContext;

import com.notnoop.apns.internal.ApnsServiceImpl;
import com.notnoop.exceptions.InvalidSSLConfig;
import com.notnoop.exceptions.RuntimeIOException;

public class ApnsServiceBuilder {
    
    public ApnsServiceBuilder() { }

    public ApnsServiceBuilder withCert(String fileName, String password)
    throws RuntimeIOException, InvalidSSLConfig {
        
        return this;
    }

    public ApnsServiceBuilder withCert(InputStream stream, String password)
    throws InvalidSSLConfig {
        return this;
    }

    
    public ApnsServiceBuilder withCert(KeyStore keyStore, String password)
    throws InvalidSSLConfig {
        return this;
    }
    
    
    public ApnsServiceBuilder withSSLContext(SSLContext sslContext) {
        return this;
    }

    public ApnsServiceBuilder withGatewayDestination(String host, int port) {
        return this;
    }

    public ApnsServiceBuilder withFeedbackDestination(String host, int port) {
        return this;
    }

    public ApnsServiceBuilder withAppleDestination(boolean isProduction) {
        return this;
    }

    public ApnsServiceBuilder withSandboxDestination() {
        return this;
    }

    public ApnsServiceBuilder withProductionDestination() {
        return this;
    }

    public ApnsServiceBuilder withReconnectPolicy(ReconnectPolicy rp) {
        return this;
    }
    
    public ApnsServiceBuilder withAutoAdjustCacheLength(boolean autoAdjustCacheLength) {
        return this;
    }

    public ApnsServiceBuilder withReconnectPolicy(ReconnectPolicy.Provided rp) {
        return this;
    }

    public ApnsServiceBuilder withSocksProxy(String host, int port) {
        return this;
    }

    public ApnsServiceBuilder withProxy(Proxy proxy) {
        return this;
    }
    
    public ApnsServiceBuilder withCacheLength(int cacheLength) {
        return this;
    }

    @Deprecated
    public ApnsServiceBuilder withProxySocket(Socket proxySocket) {
        return this;
    }

    public ApnsServiceBuilder asPool(int maxConnections) {
        return this;
    }

    public ApnsServiceBuilder asPool(ExecutorService executor, int maxConnections) {
        return this;
    }

    public ApnsServiceBuilder asQueued() {
        return this;
    }
    
    public ApnsServiceBuilder asBatched() {
        return this;
    }
    
    public ApnsServiceBuilder asBatched(int waitTimeInSec, int maxWaitTimeInSec) {
        return this;
    }
    
    public ApnsServiceBuilder asBatched(int waitTimeInSec, int maxWaitTimeInSec, ThreadFactory threadFactory) {
        return this;
    }
    
    public ApnsServiceBuilder withDelegate(ApnsDelegate delegate) {
        return this;
    }

    public ApnsServiceBuilder withNoErrorDetection() {
        return this;
    }

    public ApnsService build() {
        return new ApnsServiceImpl();
    }
}
