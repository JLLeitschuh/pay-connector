package uk.gov.pay.connector.service.epdq;

import org.junit.Test;
import uk.gov.pay.connector.model.OrderRequestType;
import uk.gov.pay.connector.model.domain.Address;
import uk.gov.pay.connector.model.domain.AuthCardDetails;
import uk.gov.pay.connector.service.GatewayOrder;
import uk.gov.pay.connector.util.AuthUtils;
import uk.gov.pay.connector.util.TestTemplateResourceLoader;

import static org.junit.Assert.assertEquals;
import static uk.gov.pay.connector.service.epdq.EpdqOrderRequestBuilder.*;
import static uk.gov.pay.connector.util.TestTemplateResourceLoader.*;

public class EpdqOrderRequestBuilderTest {

    @Test
    public void shouldGenerateValidAuthoriseOrderRequestForAddressWithAllFields() throws Exception {
        Address address = Address.anAddress();
        address.setLine1("41");
        address.setLine2("Scala Street");
        address.setCity("London");
        address.setCounty("London");
        address.setPostcode("EC2A 1AE");
        address.setCountry("GB");

        AuthCardDetails authCardDetails = AuthUtils.buildAuthCardDetails("Mr. Payment", "5555444433331111", "737", "08/18", "visa", address);

        GatewayOrder actualRequest = anEpdqAuthoriseOrderRequestBuilder()
                .withOrderId("mq4ht90j2oir6am585afk58kml")
                .withPassword("password")
                .withUserId("username")
                .withShaInPassphrase("sha-passphrase")
                .withMerchantCode("merchant-id")
                .withDescription("MyDescription")
                .withPaymentPlatformReference("MyPlatformReference")
                .withAmount("500")
                .withAuthorisationDetails(authCardDetails)
                .build();

        assertEquals(TestTemplateResourceLoader.load(EPDQ_AUTHORISATION_REQUEST), actualRequest.getPayload());
        assertEquals(OrderRequestType.AUTHORISE, actualRequest.getOrderRequestType());
    }

    @Test
    public void shouldGenerateValidCaptureOrderRequest() throws Exception {
        GatewayOrder actualRequest = anEpdqCaptureOrderRequestBuilder()
                .withPassword("password")
                .withUserId("username")
                .withShaInPassphrase("sha-passphrase")
                .withMerchantCode("merchant-id")
                .withTransactionId("payId")
                .build();

        assertEquals(TestTemplateResourceLoader.load(EPDQ_CAPTURE_REQUEST), actualRequest.getPayload());
        assertEquals(OrderRequestType.CAPTURE, actualRequest.getOrderRequestType());
    }

    @Test
    public void shouldGenerateValidCancelOrderRequest() throws Exception {
        GatewayOrder actualRequest = anEpdqCancelOrderRequestBuilder()
                .withPassword("password")
                .withUserId("username")
                .withShaInPassphrase("sha-passphrase")
                .withMerchantCode("merchant-id")
                .withTransactionId("payId")
                .build();

        assertEquals(TestTemplateResourceLoader.load(EPDQ_CANCEL_REQUEST), actualRequest.getPayload());
        assertEquals(OrderRequestType.CANCEL, actualRequest.getOrderRequestType());
    }

    @Test
    public void shouldGenerateValidRefundOrderRequest() {

        GatewayOrder gatewayOrder = anEpdqRefundOrderRequestBuilder()
                .withPassword("password")
                .withUserId("username")
                .withShaInPassphrase("sha-passphrase")
                .withMerchantCode("merchant-id")
                .withTransactionId("payId")
                .withAmount("400")
                .build();

        assertEquals(TestTemplateResourceLoader.load(EPDQ_REFUND_REQUEST), gatewayOrder.getPayload());
        assertEquals(OrderRequestType.REFUND, gatewayOrder.getOrderRequestType());
    }
}
