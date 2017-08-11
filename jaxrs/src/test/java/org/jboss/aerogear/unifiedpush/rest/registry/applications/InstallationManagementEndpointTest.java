package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import org.jboss.resteasy.spi.Link;
import org.jboss.resteasy.spi.LinkHeader;
import org.jboss.resteasy.spi.ResteasyUriInfo;

import org.junit.Test;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class InstallationManagementEndpointTest {

    private final InstallationManagementEndpoint endpoint = new InstallationManagementEndpoint();

    @Test
    public void shouldGenerateHeaderLinksFirstPage() throws URISyntaxException {
        //given
        final ResteasyUriInfo uriInfo = getUriInfo();

        //when
        final LinkHeader linkHeader = endpoint.getLinkHeader(0, 3, uriInfo);

        //then
        assertThat(findLinkByRel(linkHeader, "prev")).isNull();
        final Link next = findLinkByRel(linkHeader, "next");
        assertThat(next).isNotNull();
        assertThat(next.getHref()).isEqualTo("/?page=1");
        assertThat(findLinkByRel(linkHeader, "first")).isNull();
    }

    @Test
    public void shouldGenerateHeaderLinksNormalPage() throws URISyntaxException {
        //given
        final ResteasyUriInfo uriInfo = getUriInfo();

        //when
        final LinkHeader linkHeader = endpoint.getLinkHeader(2, 3, uriInfo);

        final Link prev = findLinkByRel(linkHeader, "prev");
        assertThat(prev).isNotNull();
        assertThat(prev.getHref()).isEqualTo("/?page=1");
        final Link next = findLinkByRel(linkHeader, "next");
        assertThat(next).isNotNull();
        assertThat(next.getHref()).isEqualTo("/?page=3");

    }

    @Test
    public void shouldGenerateHeaderLinksLastPage() throws URISyntaxException {
        //given
        final ResteasyUriInfo uriInfo = getUriInfo();

        //when
        final LinkHeader linkHeader = endpoint.getLinkHeader(3, 3, uriInfo);

        final Link prev = findLinkByRel(linkHeader, "prev");
        assertThat(prev).isNotNull();
        assertThat(prev.getHref()).isEqualTo("/?page=2");
        final Link first = findLinkByRel(linkHeader, "first");
        assertThat(first).isNotNull();
        assertThat(first.getHref()).isEqualTo("/?page=0");
        assertThat(findLinkByRel(linkHeader, "last")).isNull();
    }

    private Link findLinkByRel(LinkHeader linkHeader, String rel) {
        return linkHeader.getLinks().stream()
                .filter(link -> link.getRelationship().equals(rel))
                .findFirst()
                .orElse(null);
    }

    private ResteasyUriInfo getUriInfo() throws URISyntaxException {
        return new ResteasyUriInfo(new URI("/"), new URI("http://localhost"));
    }
}
