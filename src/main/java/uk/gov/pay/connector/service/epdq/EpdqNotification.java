package uk.gov.pay.connector.service.epdq;

import com.google.common.base.Strings;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import uk.gov.pay.connector.model.ChargeStatusRequest;
import uk.gov.pay.connector.model.domain.ChargeStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

public class EpdqNotification implements ChargeStatusRequest {

    public static final String SHASIGN = "SHASIGN";
    public static final String PAYID = "PAYID";
    public static final String PAYIDSUB = "PAYIDSUB";
    public static final String STATUS = "STATUS";

    private final List<NameValuePair> paramsList;

    private final String status;
    private final String payId;
    private final String payIdSub;
    private final String shaSign;

    private Optional<ChargeStatus> chargeStatus = Optional.empty();

    public EpdqNotification(String payload) {
        try {
            paramsList = URLEncodedUtils.parse(payload, StandardCharsets.UTF_8);

            Map<String, String> params = paramsList.stream()
                    .collect(toMap(NameValuePair::getName, NameValuePair::getValue));

            status = params.get(STATUS);
            payId = params.get(PAYID);
            payIdSub = params.get(PAYIDSUB);
            shaSign = params.get(SHASIGN);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Could not decode ePDQ notification payload as UTF-8 application/x-www-form-urlencoded");
        }
    }

    public List<NameValuePair> getParams() {
        return paramsList;
    }

    public String getShaSign() {
        return shaSign;
    }

    public String getPayId() {
        return payId;
    }

    public String getPayIdSub() {
        return payIdSub;
    }

    public String getReference() {
        if (Strings.isNullOrEmpty(payId) || Strings.isNullOrEmpty(payIdSub)) {
            return "";
        }
        return payId + "/" + payIdSub;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String getTransactionId() {
        return getPayId();
    }

    @Override
    public Optional<ChargeStatus> getChargeStatus() {
        return chargeStatus;
    }

    public void setChargeStatus(Optional<ChargeStatus> chargeStatus) {
        this.chargeStatus = chargeStatus;
    }

}
