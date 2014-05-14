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
package org.jboss.aerogear.unifiedpush.rest.settings;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.security.authz.Secure;
import org.jboss.aerogear.unifiedpush.api.ProxyServer;
import org.jboss.aerogear.unifiedpush.message.sender.ProxyCache;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.service.ProxyServerService;

@Stateless
@TransactionAttribute
@Path("/proxyServer")
@Secure( { "developer", "admin" })
public class ProxyServerEndpoint extends AbstractBaseEndpoint {

	@Inject
	private ProxyServerService proxyService;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerProxy(ProxyServer entity, @Context HttpServletRequest request) {

		ProxyServer proxyServer = proxyService.findProxy();
		
		// update db
		// new proxy ?
		if (proxyServer == null) {
			proxyService.addProxy(entity);
		} else {
			// get id from previous proxy to update it
			entity.setId(proxyServer.getId());
			proxyService.updateProxy(entity);
		}
		
		// update proxy cache
		ProxyCache.getInstance().setProxyServer(proxyServer);

		return Response.ok().build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
    public Response getProxy() {
		return Response.ok(proxyService.findProxy()).build();
    }
	
	@DELETE
	public Response unregisterProxy() {
		ProxyServer proxy = proxyService.findProxy();
		
		if (proxy == null) {
			// return error 'no proxy set' ?
		} else {
			proxyService.removeProxy(proxy);
		}
		
		return Response.ok().build();
	}
}
