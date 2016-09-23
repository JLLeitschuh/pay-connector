package uk.gov.pay.connector.resources;

import black.door.hate.HalRepresentation;
import uk.gov.pay.connector.dao.ChargeSearchParams;
import uk.gov.pay.connector.model.ChargeResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

import static javax.ws.rs.core.Response.ok;
import static uk.gov.pay.connector.resources.ApiPaths.CHARGES_API_PATH;

public class ChargesPaginationResponseBuilder {

    private ChargeSearchParams searchParams;
    private UriInfo uriInfo;
    private List<ChargeResponse> chargeResponses;

    private Long totalCount;
    private Long selfPageNum;
    private URI selfLink;
    private URI firstLink;
    private URI lastLink;
    private URI prevLink;
    private URI nextLink;

    public ChargesPaginationResponseBuilder(ChargeSearchParams searchParams, UriInfo uriInfo) {
        this.searchParams = searchParams;
        this.uriInfo = uriInfo;
        selfPageNum = searchParams.getPage();
        selfLink = uriWithParams(searchParams.buildQueryParams());
    }

    public ChargesPaginationResponseBuilder withChargeResponses(List<ChargeResponse> chargeResponses) {
        this.chargeResponses = chargeResponses;
        return this;
    }

    public ChargesPaginationResponseBuilder withTotalCount(Long total) {
        this.totalCount = total;
        return this;
    }

    public Response buildResponse() {
        Long size = searchParams.getDisplaySize();
        long lastPage = totalCount > 0 ? (totalCount + size - 1) / size : 1;
        buildLinks(lastPage);

        HalRepresentation.HalRepresentationBuilder halRepresentationBuilder = HalRepresentation.builder()
                .addProperty("results", chargeResponses)
                .addProperty("count", chargeResponses.size())
                .addProperty("total", totalCount)
                .addProperty("page", selfPageNum)
                .addLink("self", selfLink)
                .addLink("first_page", firstLink)
                .addLink("last_page", lastLink);

        addLinkNotNull(halRepresentationBuilder, "prev_page", prevLink);
        addLinkNotNull(halRepresentationBuilder, "next_page", nextLink);

        return ok(halRepresentationBuilder.build().toString()).build();
    }

    private void addLinkNotNull(HalRepresentation.HalRepresentationBuilder halRepresentationBuilder, String name, URI uri) {
        if (uri != null) {
            halRepresentationBuilder.addLink(name, uri);
        }
    }

    private void buildLinks(long lastPage) {
        searchParams.withPage(1L);
        firstLink = uriWithParams(searchParams.buildQueryParams());

        searchParams.withPage(lastPage);
        lastLink = uriWithParams(searchParams.buildQueryParams());

        searchParams.withPage(selfPageNum - 1);
        prevLink = selfPageNum == 1L ? null : uriWithParams(searchParams.buildQueryParams());

        searchParams.withPage(selfPageNum + 1);
        nextLink = selfPageNum == lastPage ? null : uriWithParams(searchParams.buildQueryParams());
    }

    private URI uriWithParams(String params) {
        return uriInfo.getBaseUriBuilder()
                .path(CHARGES_API_PATH)
                .replaceQuery(params)
                .build(searchParams.getGatewayAccountId());
    }

}
