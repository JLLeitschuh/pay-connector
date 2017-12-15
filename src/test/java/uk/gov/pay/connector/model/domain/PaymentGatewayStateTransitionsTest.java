package uk.gov.pay.connector.model.domain;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.connector.model.domain.ChargeStatus.*;

public class PaymentGatewayStateTransitionsTest {
    PaymentGatewayStateTransitions transitions = PaymentGatewayStateTransitions.getInstance();

    @Test
    public void allStatuses_hasEveryValidChargeStatus() throws Exception {
        Set<ChargeStatus> expected = new HashSet<>(Arrays.asList(ChargeStatus.values()));
        assertThat(transitions.allStatuses(), is(expected));
    }

    @Test
    public void allTransitions_containsAValidTransitionAnnotatedWithEventDescription() throws Exception {
        Set<Triple<ChargeStatus, ChargeStatus, String>> actual = transitions.allTransitions();
        assertThat(actual, hasItem(Triple.of(CREATED, EXPIRED, "ChargeExpiryService")));
    }

    @Test
    public void isValidTransition_indicatesValidAndInvalidTransition() throws Exception {
        assertThat(transitions.isValidTransition(CAPTURE_READY, CAPTURE_SUBMITTED), is(true));
        assertThat(transitions.isValidTransition(CREATED, AUTHORISATION_READY), is(false));
    }
}
