package uk.gov.pay.connector.service;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.connector.app.CaptureProcessConfig;
import uk.gov.pay.connector.app.ConnectorConfiguration;
import uk.gov.pay.connector.dao.ChargeDao;
import uk.gov.pay.connector.model.domain.ChargeEntity;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CardCaptureProcess {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ChargeDao chargeDao;
    private final CardCaptureService captureService;
    private final MetricRegistry metricRegistry;
    private final CaptureProcessConfig captureConfig;
    private volatile long queueSize;

    @Inject
    public CardCaptureProcess(Environment environment, ChargeDao chargeDao, CardCaptureService cardCaptureService, ConnectorConfiguration connectorConfiguration) {
        this.chargeDao = chargeDao;
        this.captureService = cardCaptureService;
        this.captureConfig = connectorConfiguration.getCaptureProcessConfig();
        metricRegistry = environment.metrics();

        metricRegistry.register("gateway-operations.capture-process.queue-size", (Gauge<Long>) this::getQueueSize);
    }

    public void runCapture() {
        Stopwatch responseTimeStopwatch = Stopwatch.createStarted();
        try {
            queueSize = chargeDao.countChargesForCapture();

            List<ChargeEntity> chargesToCapture = chargeDao.findChargesForCapture(captureConfig.getBatchSize(), captureConfig.getRetryFailuresEveryAsJavaDuration());

            logger.info("Capturing : "+ chargesToCapture.size() + " of " + queueSize + " charges");
            metricRegistry.counter("gateway-operations.capture-process").inc();

            chargesToCapture.forEach((charge) ->  captureService.doCapture(charge.getExternalId()));
        } catch (Exception e) {
            logger.error("Exception when running capture", e);
        } finally {
            responseTimeStopwatch.stop();
            metricRegistry.histogram("gateway-operations.capture-process.running_time").update(responseTimeStopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    public long getQueueSize() {
        return queueSize;
    }
}
