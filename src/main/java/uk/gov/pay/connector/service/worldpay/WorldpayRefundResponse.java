package uk.gov.pay.connector.service.worldpay;

import org.apache.commons.lang3.StringUtils;
import uk.gov.pay.connector.service.BaseRefundResponse;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Optional;
import java.util.StringJoiner;

@XmlRootElement(name = "paymentService")
public class WorldpayRefundResponse extends WorldpayBaseResponse implements BaseRefundResponse {

    @Override
    public Optional<String> getReference() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        if (!StringUtils.isNotBlank(getErrorCode()) && !StringUtils.isNotBlank(getErrorMessage())) {
            return "Worldpay refund response";
        }

        StringJoiner joiner = new StringJoiner(", ", "Worldpay refund response (", ")");
        if (StringUtils.isNotBlank(getErrorCode())) {
            joiner.add("error code: " + getErrorCode());
        }
        if (StringUtils.isNotBlank(getErrorMessage())) {
            joiner.add("error: " + getErrorMessage());
        }
        return joiner.toString();
    }

}
