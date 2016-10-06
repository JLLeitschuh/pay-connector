package uk.gov.pay.connector.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.pay.connector.app.ConnectorConfiguration;
import uk.gov.pay.connector.dao.CardTypeDao;
import uk.gov.pay.connector.dao.GatewayAccountDao;
import uk.gov.pay.connector.exception.CredentialsException;
import uk.gov.pay.connector.model.builder.EntityBuilder;
import uk.gov.pay.connector.model.domain.GatewayAccountEntity;
import uk.gov.pay.connector.model.domain.NotificationCredentials;
import uk.gov.pay.connector.util.HashUtil;

import java.util.Map;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class GatewayAccountNotificationCredentialsServiceTest {

    private GatewayAccountNotificationCredentialsService gatewayAccountNotificationCredentialsService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    GatewayAccountDao gatewayDao;

    @Mock
    CardTypeDao cardTypeDao;

    @Mock
    ConnectorConfiguration conf;

    @Mock
    EntityBuilder entityBuilder;

    @Mock
    HashUtil hashUtil;

    @Before
    public void setup() {
        gatewayAccountNotificationCredentialsService = new GatewayAccountNotificationCredentialsService(gatewayDao, entityBuilder, hashUtil);
    }

    @Test
    public void shouldCreateNotificationCredentialsIfNotPresent() throws CredentialsException {
        GatewayAccountEntity gatewayAccount = mock(GatewayAccountEntity.class);
        NotificationCredentials notificationCredentials = mock(NotificationCredentials.class);
        Map<String, String> credentials = ImmutableMap.of("username", "bob", "password", "bobssecret");


        when(gatewayAccount.getNotificationCredentials()).thenReturn(null);
        when(entityBuilder.newNotificationCredentials(gatewayAccount)).thenReturn(notificationCredentials);

        gatewayAccountNotificationCredentialsService.setCredentialsForAccount(credentials, gatewayAccount);

        verify(gatewayAccount).setNotificationCredentials(notificationCredentials);
        verify(gatewayDao).merge(gatewayAccount);
    }


    @Test
    public void shouldEncryptPasswordWhenCreatingNotificationCredentials() throws CredentialsException {
        GatewayAccountEntity gatewayAccount = mock(GatewayAccountEntity.class);
        NotificationCredentials notificationCredentials = mock(NotificationCredentials.class);
        Map<String, String> credentials = ImmutableMap.of("username", "bob", "password", "bobssecret");


        when(gatewayAccount.getNotificationCredentials()).thenReturn(null);
        when(entityBuilder.newNotificationCredentials(gatewayAccount)).thenReturn(notificationCredentials);
        when(hashUtil.hash("bobssecret")).thenReturn("bobshashedsecret");

        gatewayAccountNotificationCredentialsService.setCredentialsForAccount(credentials, gatewayAccount);

        InOrder inOrder = Mockito.inOrder(hashUtil, notificationCredentials, gatewayAccount);
        inOrder.verify(hashUtil).hash("bobssecret");
        inOrder.verify(notificationCredentials).setPassword("bobshashedsecret");
        inOrder.verify(gatewayAccount).setNotificationCredentials(notificationCredentials);
    }

    @Test
    public void shouldUpdateExistingNotificationCredentialIfPresent() throws CredentialsException {
        GatewayAccountEntity gatewayAccount = mock(GatewayAccountEntity.class);
        NotificationCredentials notificationCredentials = mock(NotificationCredentials.class);
        Map<String, String> credentials = ImmutableMap.of("username", "bob", "password", "bobssecret");

        when(gatewayAccount.getNotificationCredentials()).thenReturn(notificationCredentials);
        when(hashUtil.hash("bobssecret")).thenReturn("bobshashedsecret");

        gatewayAccountNotificationCredentialsService.setCredentialsForAccount(credentials, gatewayAccount);

        InOrder inOrder = Mockito.inOrder(hashUtil, notificationCredentials, gatewayAccount);
        inOrder.verify(notificationCredentials).setUserName("bob");
        inOrder.verify(hashUtil).hash("bobssecret");
        inOrder.verify(notificationCredentials).setPassword("bobshashedsecret");
        inOrder.verify(gatewayAccount).setNotificationCredentials(notificationCredentials);

        verifyZeroInteractions(entityBuilder);
    }

    @Test
    public void shouldValidateThatPasswordisAtLeaset10Characters() throws CredentialsException {
        expectedException.expect(CredentialsException.class);
        expectedException.expectMessage("Invalid password length");

        GatewayAccountEntity gatewayAccount = mock(GatewayAccountEntity.class);
        NotificationCredentials notificationCredentials = mock(NotificationCredentials.class);
        Map<String, String> credentials = ImmutableMap.of("username", "bob", "password", "bobsecret");

        when(gatewayAccount.getNotificationCredentials()).thenReturn(null);
        when(entityBuilder.newNotificationCredentials(gatewayAccount)).thenReturn(notificationCredentials);
        when(hashUtil.hash("bobsecret")).thenReturn("bobshashedsecret");

        gatewayAccountNotificationCredentialsService.setCredentialsForAccount(credentials, gatewayAccount);

        verifyZeroInteractions(hashUtil);
        verifyZeroInteractions(entityBuilder);
    }
}
