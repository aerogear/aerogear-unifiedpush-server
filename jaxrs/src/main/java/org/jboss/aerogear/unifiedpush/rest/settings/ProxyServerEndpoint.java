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

		ProxyServer proxy = proxyService.findProxy();
		
		// new proxy ?
		if (proxy == null) {
			proxyService.addProxy(entity);
		} else {
			// get id from previous proxy to update it
			entity.setId(proxy.getId());
			proxyService.updateProxy(entity);
		}

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
