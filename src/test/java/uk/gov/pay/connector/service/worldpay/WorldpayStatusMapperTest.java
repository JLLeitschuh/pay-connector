package uk.gov.pay.connector.service.worldpay;

import org.junit.Test;
import uk.gov.pay.connector.service.InterpretedStatus;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.connector.model.domain.ChargeStatus.AUTHORISATION_SUCCESS;
import static uk.gov.pay.connector.model.domain.ChargeStatus.CAPTURED;
import static uk.gov.pay.connector.model.domain.ChargeStatus.CAPTURE_SUBMITTED;
import static uk.gov.pay.connector.model.domain.RefundStatus.REFUNDED;

public class WorldpayStatusMapperTest {

    @Test
    public void shouldReturnAChargeStatus() throws Exception {
        InterpretedStatus status = WorldpayStatusMapper.get().from("CAPTURED", CAPTURE_SUBMITTED);

        assertThat(status.getType(), is(InterpretedStatus.Type.CHARGE_STATUS));
        assertThat(status.getChargeStatus(), is(CAPTURED));
    }

    @Test
    public void shouldReturnARefundStatusFromRefunded() throws Exception {
        InterpretedStatus status = WorldpayStatusMapper.get().from("REFUNDED", CAPTURED);

        assertThat(status.getType(), is(InterpretedStatus.Type.REFUND_STATUS));
        assertThat(status.getRefundStatus(), is(REFUNDED));
    }

    @Test
    public void shouldReturnARefundStatusFromRefundedByMerchant() throws Exception {
        InterpretedStatus status = WorldpayStatusMapper.get().from("REFUNDED_BY_MERCHANT", CAPTURED);

        assertThat(status.getType(), is(InterpretedStatus.Type.REFUND_STATUS));
        assertThat(status.getRefundStatus(), is(REFUNDED));
    }


    @Test
    public void shouldReturnEmptyWhenStatusIsUnknown() throws Exception {
        InterpretedStatus status = WorldpayStatusMapper.get().from("unknown", AUTHORISATION_SUCCESS);

        assertThat(status.getType(), is(InterpretedStatus.Type.UNKNOWN));
    }

    @Test
    public void shouldReturnEmptyWhenStatusIsIgnored() throws Exception {
        InterpretedStatus status = WorldpayStatusMapper.get().from("AUTHORISED", AUTHORISATION_SUCCESS);

        assertThat(status.getType(), is(InterpretedStatus.Type.IGNORED));
    }

}
