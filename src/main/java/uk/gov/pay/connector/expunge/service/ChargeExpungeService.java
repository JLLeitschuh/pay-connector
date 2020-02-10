package uk.gov.pay.connector.expunge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.connector.app.ConnectorConfiguration;
import uk.gov.pay.connector.app.config.ExpungeConfig;
import uk.gov.pay.connector.charge.dao.ChargeDao;
import uk.gov.pay.connector.charge.model.domain.ChargeEntity;
import uk.gov.pay.connector.charge.model.domain.ChargeStatus;
import uk.gov.pay.connector.tasks.ParityCheckService;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static uk.gov.pay.logging.LoggingKeys.PAYMENT_EXTERNAL_ID;

public class ChargeExpungeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ChargeDao chargeDao;
    private final ExpungeConfig expungeConfig;
    private final ParityCheckService parityCheckService;
    
    @Inject
    public ChargeExpungeService(ChargeDao chargeDao, ConnectorConfiguration connectorConfiguration,
                                ParityCheckService parityCheckService) {
        this.chargeDao = chargeDao;
        expungeConfig = connectorConfiguration.getExpungeConfig();
        this.parityCheckService = parityCheckService;
    }

    public void expunge(Integer noOfChargesToExpungeQueryParam) {
        if (expungeConfig.isExpungeChargesEnabled()) {
            int noOfChargesToExpunge = getNumberOfChargesToExpunge(noOfChargesToExpungeQueryParam);

            int noOfChargesProcessed = 0;

            while (noOfChargesProcessed < noOfChargesToExpunge) {
                Optional<ChargeEntity> mayBeChargeEntity =
                        chargeDao.findChargeToExpunge(expungeConfig.getMinimumAgeOfChargeInDays(),
                                expungeConfig.getExcludeChargesParityCheckedWithInDays()
                        );

                mayBeChargeEntity.ifPresent(chargeEntity -> {
                    if (hasFinalisedState(chargeEntity)
                            && parityCheckService.parityCheckChargeForExpunger(chargeEntity)) {
                        chargeDao.expungeCharge(chargeEntity.getId());
                        logger.info("Charge expunged from connector {}", 
                                kv(PAYMENT_EXTERNAL_ID, chargeEntity.getExternalId()));
                    } else {
                        chargeDao.updateChargeParityCheckDate(chargeEntity.getId(), ZonedDateTime.now());
                        logger.info("Charge does not meet expunging criteria from connector {}", 
                                kv(PAYMENT_EXTERNAL_ID, chargeEntity.getExternalId()));
                    }
                }); 

                if (mayBeChargeEntity.isEmpty()) {
                    break;
                }
                noOfChargesProcessed++;
            }
        } else {
            logger.info("Charge expunging feature is disabled. No charges have been expunged");
        }
    }

    private int getNumberOfChargesToExpunge(Integer noOfChargesToExpungeQueryParam) {
        if (noOfChargesToExpungeQueryParam != null && noOfChargesToExpungeQueryParam > 0) {
            return noOfChargesToExpungeQueryParam;
        }
        return expungeConfig.getNumberOfChargesToExpunge();
    }

    private boolean hasFinalisedState(ChargeEntity chargeEntity) {
        return ChargeStatus.fromString(chargeEntity.getStatus()).toExternal().isFinished();
    }
}
