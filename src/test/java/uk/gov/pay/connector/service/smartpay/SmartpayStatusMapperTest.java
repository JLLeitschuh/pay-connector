package uk.gov.pay.connector.service.smartpay;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import uk.gov.pay.connector.service.InterpretedStatus;
import uk.gov.pay.connector.service.MappedChargeStatus;
import uk.gov.pay.connector.service.MappedRefundStatus;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.connector.model.domain.ChargeStatus.AUTHORISATION_SUCCESS;
import static uk.gov.pay.connector.model.domain.ChargeStatus.CAPTURED;
import static uk.gov.pay.connector.model.domain.ChargeStatus.CAPTURE_ERROR;
import static uk.gov.pay.connector.model.domain.ChargeStatus.CAPTURE_SUBMITTED;
import static uk.gov.pay.connector.model.domain.RefundStatus.REFUNDED;
import static uk.gov.pay.connector.model.domain.RefundStatus.REFUND_ERROR;

public class SmartpayStatusMapperTest {

    @Test
    public void shouldReturnAStatusForCaptureTrue() throws Exception {
        Pair<String, Boolean> value = Pair.of("CAPTURE", true);
        InterpretedStatus status = SmartpayStatusMapper.get().from(value, CAPTURE_SUBMITTED);

        assertThat(status.getType(), is(InterpretedStatus.Type.CHARGE_STATUS));
        assertThat(((MappedChargeStatus) status).getChargeStatus(), is(CAPTURED));
    }

    @Test
    public void shouldReturnAStatusForCaptureFalse() throws Exception {
        Pair<String, Boolean> value = Pair.of("CAPTURE", false);
        InterpretedStatus status = SmartpayStatusMapper.get().from(value, CAPTURE_SUBMITTED);

        assertThat(status.getType(), is(InterpretedStatus.Type.CHARGE_STATUS));
        assertThat(((MappedChargeStatus) status).getChargeStatus(), is(CAPTURE_ERROR));
    }

    @Test
    public void shouldReturnAStatusForRefundedTrue() throws Exception {
        Pair<String, Boolean> value = Pair.of("REFUND", true);
        InterpretedStatus status = SmartpayStatusMapper.get().from(value, CAPTURED);

        assertThat(status.getType(), is(InterpretedStatus.Type.REFUND_STATUS));
        assertThat(((MappedRefundStatus) status).getRefundStatus(), is(REFUNDED));
    }

    @Test
    public void shouldReturnAStatusForRefundedFalse() throws Exception {
        Pair<String, Boolean> value = Pair.of("REFUND", false);
        InterpretedStatus status = SmartpayStatusMapper.get().from(value, CAPTURED);

        assertThat(status.getType(), is(InterpretedStatus.Type.REFUND_STATUS));
        assertThat(((MappedRefundStatus) status).getRefundStatus(), is(REFUND_ERROR));
    }

    @Test
    public void shouldReturnAStatusForRefundFailedTrue() throws Exception {
        Pair<String, Boolean> value = Pair.of("REFUND_FAILED", true);
        InterpretedStatus status = SmartpayStatusMapper.get().from(value, CAPTURED);

        assertThat(status.getType(), is(InterpretedStatus.Type.REFUND_STATUS));
        assertThat(((MappedRefundStatus) status).getRefundStatus(), is(REFUND_ERROR));
    }

    @Test
    public void shouldReturnAStatusForRefundFailedFalse() throws Exception {
        Pair<String, Boolean> value = Pair.of("REFUND_FAILED", false);
        InterpretedStatus status = SmartpayStatusMapper.get().from(value, CAPTURED);

        assertThat(status.getType(), is(InterpretedStatus.Type.REFUND_STATUS));
        assertThat(((MappedRefundStatus) status).getRefundStatus(), is(REFUND_ERROR));
    }

    @Test
    public void shouldReturnEmptyWhenStatusIsUnknown() throws Exception {
        InterpretedStatus status = SmartpayStatusMapper.get().from(Pair.of("UNKNOWN", true), AUTHORISATION_SUCCESS);

        assertThat(status.getType(), is(InterpretedStatus.Type.UNKNOWN));
    }

    @Test
    public void shouldReturnEmptyWhenStatusIsIgnored() throws Exception {
        InterpretedStatus status = SmartpayStatusMapper.get().from(Pair.of("AUTHORISATION", true), AUTHORISATION_SUCCESS);

        assertThat(status.getType(), is(InterpretedStatus.Type.IGNORED));
    }

}
