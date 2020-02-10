package uk.gov.pay.connector.expunge.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.connector.app.ConnectorConfiguration;
import uk.gov.pay.connector.app.config.ExpungeConfig;
import uk.gov.pay.connector.charge.dao.ChargeDao;
import uk.gov.pay.connector.charge.model.domain.ChargeEntity;
import uk.gov.pay.connector.charge.model.domain.ChargeStatus;
import uk.gov.pay.connector.tasks.ParityCheckService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.connector.charge.model.domain.ChargeEntity.WebChargeEntityBuilder.aWebChargeEntity;
import static uk.gov.pay.connector.gatewayaccount.model.GatewayAccountEntityFixture.aGatewayAccountEntity;

@RunWith(MockitoJUnitRunner.class)
public class ChargeExpungeServiceTest {

    private ChargeEntity chargeEntity = new ChargeEntity();
    private ChargeExpungeService chargeExpungeService;
    private int minimumAgeOfChargeInDays = 3;
    private int defaultNumberOfChargesToExpunge = 10;
    private int defaultExcludeChargesParityCheckedWithInDays = 1;

    @Mock
    private ExpungeConfig mockExpungeConfig;
    @Mock
    private ChargeDao mockChargeDao;
    @Mock
    private ConnectorConfiguration mockConnectorConfiguration;
    @Mock
    private ParityCheckService parityCheckService;

    @Before
    public void setUp() {
        when(mockConnectorConfiguration.getExpungeConfig()).thenReturn(mockExpungeConfig);
        when(mockExpungeConfig.getNumberOfChargesToExpunge()).thenReturn(defaultNumberOfChargesToExpunge);
        when(mockExpungeConfig.getMinimumAgeOfChargeInDays()).thenReturn(minimumAgeOfChargeInDays);
        when(mockExpungeConfig.getExcludeChargesParityCheckedWithInDays()).thenReturn(defaultExcludeChargesParityCheckedWithInDays);
        when(mockExpungeConfig.isExpungeChargesEnabled()).thenReturn(true);

        when(mockChargeDao.findChargeToExpunge(minimumAgeOfChargeInDays, defaultExcludeChargesParityCheckedWithInDays))
                .thenReturn(Optional.of(chargeEntity));
        chargeExpungeService = new ChargeExpungeService(mockChargeDao, mockConnectorConfiguration, parityCheckService);
    }

    @Test
    public void expunge_shouldExpungeCharges() {
        chargeExpungeService.expunge(2);
        verify(mockChargeDao, times(2)).findChargeToExpunge(minimumAgeOfChargeInDays, 1);
    }

    @Test
    public void expunge_shouldExpungeNoOfChargesAsPerConfiguration() {
        chargeExpungeService.expunge(null);
        verify(mockChargeDao, times(defaultNumberOfChargesToExpunge)).findChargeToExpunge(minimumAgeOfChargeInDays,
                defaultExcludeChargesParityCheckedWithInDays);
    }

    @Test
    public void expunge_shouldNotExpungeChargesIfFeatureIsNotEnabled() {
        when(mockExpungeConfig.isExpungeChargesEnabled()).thenReturn(false);
        chargeExpungeService.expunge(null);
        verify(mockChargeDao, never()).findChargeToExpunge(anyInt(), anyInt());
    }

    @Test
    public void expunge_whenChargeMeetsTheConditions() {
        ChargeEntity chargeEntity = aWebChargeEntity()
                .withGatewayAccount(aGatewayAccountEntity().withId(1L).build())
                .withAmount(120L)
                .build();
        chargeEntity.setStatus(ChargeStatus.CREATED);
        chargeEntity.setStatus(ChargeStatus.ENTERING_CARD_DETAILS);
        chargeEntity.setStatus(ChargeStatus.AUTHORISATION_READY);
        chargeEntity.setStatus(ChargeStatus.AUTHORISATION_SUCCESS);
        chargeEntity.setStatus(ChargeStatus.CAPTURE_READY);
        chargeEntity.setStatus(ChargeStatus.CAPTURE_SUBMITTED);
        when(mockExpungeConfig.isExpungeChargesEnabled()).thenReturn(true);
        when(mockExpungeConfig.getNumberOfChargesToExpunge()).thenReturn(defaultNumberOfChargesToExpunge);
        when(mockExpungeConfig.getMinimumAgeOfChargeInDays()).thenReturn(minimumAgeOfChargeInDays);
        when(mockChargeDao.findChargeToExpunge(minimumAgeOfChargeInDays, defaultExcludeChargesParityCheckedWithInDays))
                .thenReturn(Optional.of(chargeEntity));
        when(parityCheckService.parityCheckChargeForExpunger(chargeEntity)).thenReturn(true);
        chargeExpungeService.expunge(2);
    }
}
