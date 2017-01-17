package uk.gov.pay.connector.service.epdq;

import com.google.common.collect.ImmutableList;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.pay.connector.service.epdq.EpdqPayloadDefinitionForMaintenanceOrder.*;

@RunWith(MockitoJUnitRunner.class)
public class EpdqPayloadDefinitionMaintenanceOrderTest {

    private static final String OPERATION_TYPE = "RES";
    private static final String PAY_ID = "PayId";
    private static final String PSP_ID = "PspId";
    private static final String PASSWORD = "password";
    private static final String USER_ID = "User";

    @Mock
    private EpdqOrderRequestBuilder.EpdqTemplateData mockTemplateData;

    private final EpdqPayloadDefinitionForMaintenanceOrder epdqPayloadDefinitionForMaintenanceOrder = new EpdqPayloadDefinitionForMaintenanceOrder();

    @Test
    public void shouldExtractParametersFromTemplate() {
        when(mockTemplateData.getOperationType()).thenReturn(OPERATION_TYPE);
        when(mockTemplateData.getPayId()).thenReturn(PAY_ID);
        when(mockTemplateData.getPspId()).thenReturn(PSP_ID);
        when(mockTemplateData.getPassword()).thenReturn(PASSWORD);
        when(mockTemplateData.getUserId()).thenReturn(USER_ID);

        ImmutableList<NameValuePair> result = epdqPayloadDefinitionForMaintenanceOrder.extract(mockTemplateData);

        assertThat(result, is(ImmutableList.builder().add(
                new BasicNameValuePair(OPERATION_KEY, OPERATION_TYPE),
                new BasicNameValuePair(PAYID_KEY, PAY_ID),
                new BasicNameValuePair(PSPID_KEY, PSP_ID),
                new BasicNameValuePair(PSWD_KEY, PASSWORD),
                new BasicNameValuePair(USERID_KEY, USER_ID))
                .build()));
    }


}