package uk.gov.pay.connector.expunge.service;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class LedgerStub {

    public void returnLedgerTransaction(String id) {
        ResponseDefinitionBuilder responseDefBuilder = aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withStatus(200)
                .withBody("{ \"hi\" : \"hello\" }");

        stubFor(
                any(anyUrl())
//                        .withQueryParam("override_account_id_restriction", equalTo("true"))
                        .willReturn(
                                responseDefBuilder
                        )
        );
    }

}
