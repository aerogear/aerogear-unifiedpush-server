package org.jboss.aerogear.unifiedpush.rest.shortlinks;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.document.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.document.QueryOptions;
import org.jboss.aerogear.unifiedpush.cassandra.dao.NullUUID;
import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.DocumentKey;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;
import org.jboss.aerogear.unifiedpush.rest.AbstractEndpoint;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IConfigurationService;
import org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.qmino.miredot.annotations.ReturnType;

@Controller
@Path("/shortlinks")
public class ShortLinksEndpoint extends AbstractEndpoint {
	private final Logger logger = LoggerFactory.getLogger(ShortLinksEndpoint.class);
	private final static int VERIFICATION_CODE_LENGTH = 5;
	private final static int VERIFICATION_LINK_LENGTH = 10;

	@Inject
	private DocumentService documentService;
	@Inject
	private AliasService aliasService;
	@Autowired
	private IConfigurationService configurationService;

	private final static ClickatellSMSSender sender = new ClickatellSMSSender();

	/**
	 * Cross Origin for shortlinks requests.
	 *
	 * @param headers
	 *            "Origin" header
	 * @return "Access-Control-Allow-Origin" header for your response
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin" header
	 * @responseheader Access-Control-Allow-Methods POST, PUT, DELETE, HEAD
	 * @responseheader Access-Control-Allow-Headers accept, origin, content-type,
	 *                 authorization
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader Access-Control-Max-Age 604800
	 *
	 * @statuscode 200 Successful response for your request
	 */
	@OPTIONS
	@ReturnType("java.lang.Void")
	public Response crossOriginForApplication(@Context HttpHeaders headers) {
		return appendPreflightResponseHeaders(headers, Response.ok()).build();
	}

	/**
	 * Cross Origin for shortlink new request.
	 *
	 * @param headers
	 *            "Origin" header
	 * @return "Access-Control-Allow-Origin" header for your response
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin" header
	 * @responseheader Access-Control-Allow-Methods POST, DELETE
	 * @responseheader Access-Control-Allow-Headers accept, origin, content-type,
	 *                 authorization
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader Access-Control-Max-Age 604800
	 *
	 * @statuscode 200 Successful response for your request
	 */
	@OPTIONS
	@Path("/type/{type}/username/{alias}")
	@ReturnType("java.lang.Void")
	public Response crossOriginForApplicationPut(@Context HttpHeaders headers) {
		return appendPreflightResponseHeaders(headers, Response.ok()).build();
	}

	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	@ReturnType("java.lang.Void")
	@Path("/type/{type}/username/{alias}")
	public Response code(@PathParam("type") String type, //
			@PathParam("alias") String alias, //
			String link, //
			@Context HttpServletRequest request) { //

		long ttl = 3600;
		String code = getCode(type);
		String shortLink = getShortLink(code, request);

		// TODO - Validate JWT and use expire as TTL
		// JWT validation should be enabled/disabled using -D to allow unit test

		Alias aliasObj = aliasService.find(null, alias);
		if (aliasObj == null) {
			logger.warn("Unable to find alias " + alias + " for shortlinks generation");
			return appendAllowOriginHeader(Response.status(Status.BAD_REQUEST), request);
		}

		DocumentContent doc = new DocumentContent(new DocumentKey(NullUUID.NULL.getUuid(), "SHORTLINKS"), link, code);
		documentService.save(doc, Long.valueOf(ttl).intValue());

		try {
			return appendAllowOriginHeader(Response.ok(shortLink), request);
		} catch (Exception ex) {
			return appendAllowOriginHeader(Response.status(Status.INTERNAL_SERVER_ERROR), request);
		}
	}

	@GET
	@Path("/{code}")
	public Response validate(@PathParam("code") String code, //
			@Context HttpServletRequest request) { //

		Stream<DocumentContent> docs = documentService.find(
				new DocumentMetadata(NullUUID.NULL.getUuid(), "SHORTLINKS", NullUUID.NULL.getUuid()),
				new QueryOptions(code));
		List<DocumentContent> links = docs.collect(Collectors.toList());
		if (links.isEmpty()) {
			return create401Response(request);
		}

		try {
			return Response.temporaryRedirect(new URI(links.get(0).getContent())).build();
		} catch (URISyntaxException e) {
			logger.error("Unable to redirect request", e);
			return create401Response(request);
		}
	}

	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	@Path("/sms/{number}")
	public Response send(@PathParam("number") String number, //
			String shortlink, //
			@Context HttpServletRequest request) { //

		try {
			// extract code from short link
			URI uri = new URI(shortlink);

			Stream<DocumentContent> docs = documentService.find(
					new DocumentMetadata(NullUUID.NULL.getUuid(), "SHORTLINKS", NullUUID.NULL.getUuid()),
					new QueryOptions(uri.getPath()));
			List<DocumentContent> links = docs.collect(Collectors.toList());
			if (links.isEmpty()) {
				return create401Response(request);
			}

			sender.send(number, shortlink, configurationService.getProperties());
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Unable to send SMS request", e);
			return create401Response(request);
		}
	}

	private String getCode(String type) {
		if (type.equalsIgnoreCase("link"))
			return RandomStringUtils.random(VERIFICATION_LINK_LENGTH, true, true);
		else
			return RandomStringUtils.random(VERIFICATION_CODE_LENGTH, false, true);
	}

	private String getShortLink(String code, HttpServletRequest request) {
		// SL links requires rewrite rule from https://xxx.com/sl/code to
		// https://xxx.com/unifiedpus-server/rest/shortlinks/code
		return request.getScheme() + "://" + request.getServerName() + "/sl/" + code;
	}
}
