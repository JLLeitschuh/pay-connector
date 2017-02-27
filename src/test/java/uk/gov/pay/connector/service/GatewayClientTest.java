package uk.gov.pay.connector.service;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.pay.connector.model.GatewayError;
import uk.gov.pay.connector.model.OrderRequestType;
import uk.gov.pay.connector.model.domain.GatewayAccountEntity;
import uk.gov.pay.connector.service.worldpay.WorldpayPaymentProvider;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static uk.gov.pay.connector.model.domain.GatewayAccount.CREDENTIALS_PASSWORD;
import static uk.gov.pay.connector.model.domain.GatewayAccount.CREDENTIALS_USERNAME;
import static uk.gov.pay.connector.util.AuthUtil.encode;

@RunWith(MockitoJUnitRunner.class)
public class GatewayClientTest {

    public static final String WORLDPAY_API_ENDPOINT = "http://www.example.com/worldpay/order";
    GatewayClient gatewayClient;

    private String orderPayload = "a-sample-payload";

    @Mock Client mockClient;
    @Mock Response mockResponse;
    @Mock Builder mockBuilder;

    @Mock MetricRegistry mockMetricRegistry;
    @Mock Histogram mockHistogram;
    @Mock Counter mockCounter;

    @Mock GatewayAccountEntity mockGatewayAccountEntity;

    @Mock GatewayOrder mockGatewayOrder;

    @Mock
    BiFunction<GatewayOrder, Builder, Builder> mockSessionIdentifier;

    @Before
    public void setup() {
        Map<String, String> urlMap = Collections.singletonMap("worldpay", WORLDPAY_API_ENDPOINT);
        Map<String, String> credentialMap = new HashMap<>();
        credentialMap.put(CREDENTIALS_USERNAME, "user");
        credentialMap.put(CREDENTIALS_PASSWORD, "password");

        gatewayClient = GatewayClient.createGatewayClient(mockClient, urlMap, MediaType.APPLICATION_XML_TYPE,
                mockSessionIdentifier, mockMetricRegistry);
        when(mockMetricRegistry.histogram(anyString())).thenReturn(mockHistogram);
        when(mockMetricRegistry.counter(anyString())).thenReturn(mockCounter);
        doAnswer(invocationOnMock -> null).when(mockHistogram).update(anyInt());
        doAnswer(invocationOnMock -> null).when(mockCounter).inc();

        WebTarget mockWebTarget = mock(WebTarget.class);

        when(mockClient.target(WORLDPAY_API_ENDPOINT)).thenReturn(mockWebTarget);
        when(mockWebTarget.request()).thenReturn(mockBuilder).thenReturn(mockBuilder);
        when(mockBuilder.header(AUTHORIZATION, encode("user", "password"))).thenReturn(mockBuilder);
        when(mockSessionIdentifier.apply(mockGatewayOrder, mockBuilder)).thenReturn(mockBuilder);
        when(mockBuilder.post(Entity.entity(orderPayload, MediaType.APPLICATION_XML_TYPE))).thenReturn(mockResponse);

        when(mockGatewayAccountEntity.getGatewayName()).thenReturn("worldpay");
        when(mockGatewayAccountEntity.getType()).thenReturn("worldpay");
        when(mockGatewayAccountEntity.getCredentials()).thenReturn(credentialMap);

        when(mockGatewayOrder.getOrderRequestType()).thenReturn(OrderRequestType.AUTHORISE);
        when(mockGatewayOrder.getPayload()).thenReturn(orderPayload);
        when(mockGatewayOrder.getProviderSessionId()).thenReturn(Optional.empty());
    }

    @Test
    public void shouldReturnAGatewayResponseWhenProviderReturnsOk() {
        when(mockResponse.getStatus()).thenReturn(200);

        Either<GatewayError, GatewayClient.Response> gatewayResponse = gatewayClient.postRequestFor(null, mockGatewayAccountEntity, mockGatewayOrder);

        assertTrue(gatewayResponse.isRight());
        assertFalse(gatewayResponse.isLeft());
        verify(mockResponse).close();
    }

    @Test
    public void shouldReturnGatewayErrorWhenProviderFails() {
        when(mockResponse.getStatus()).thenReturn(500);

        Either<GatewayError, GatewayClient.Response> gatewayResponse = gatewayClient.postRequestFor(null, mockGatewayAccountEntity, mockGatewayOrder);

        assertTrue(gatewayResponse.isLeft());
        assertFalse(gatewayResponse.isRight());
        verify(mockResponse).close();
    }

    @Test
    public void shouldReturnGatewayErrorWhenProviderFailsWithAProcessingException() {
        when(mockBuilder.post(Entity.entity(orderPayload, MediaType.APPLICATION_XML_TYPE))).thenThrow(new ProcessingException(new SocketException("socket failed")));

        Either<GatewayError, GatewayClient.Response> gatewayResponse = gatewayClient.postRequestFor(null, mockGatewayAccountEntity, mockGatewayOrder);

        assertTrue(gatewayResponse.isLeft());
        assertFalse(gatewayResponse.isRight());
    }

    @Test
    public void shouldIncludeCookieIfSessionIdentifierAvailableInOrder() {
        String providerSessionid = "provider-session-id";

        when(mockGatewayOrder.getProviderSessionId()).thenReturn(Optional.of(providerSessionid));
        when(mockResponse.getStatus()).thenReturn(200);

        gatewayClient.postRequestFor(null, mockGatewayAccountEntity, mockGatewayOrder);

        InOrder inOrder = Mockito.inOrder(mockSessionIdentifier, mockBuilder);
        inOrder.verify(mockSessionIdentifier).apply(mockGatewayOrder, mockBuilder);
        inOrder.verify(mockBuilder).post(Entity.entity(orderPayload, MediaType.APPLICATION_XML_TYPE));
    }
}
