package uk.gov.pay.connector.service.worldpay;

import org.eclipse.persistence.oxm.annotations.XmlPath;
import uk.gov.pay.connector.service.BaseAuthoriseResponse;
import uk.gov.pay.connector.service.BaseInquiryResponse;

import javax.xml.bind.annotation.XmlRootElement;

import static org.apache.commons.lang3.StringUtils.trim;

@XmlRootElement(name = "paymentService")
public class WorldpayOrderStatusResponse implements BaseAuthoriseResponse, BaseInquiryResponse {

    private static final String AUTHORISED = "AUTHORISED";

    @XmlPath("reply/orderStatus/@orderCode")
    private String transactionId;

    @XmlPath("reply/orderStatus/payment/lastEvent/text()")
    private String lastEvent;

    @XmlPath("reply/orderStatus/payment/ISO8583ReturnCode/@description")
    private String refusedReturnCodeDescription;

    @XmlPath("reply/orderStatus/payment/ISO8583ReturnCode/@code")
    private String refusedReturnCode;

    private String errorCode;

    private String errorMessage;

    @XmlPath("reply/error/@code")
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @XmlPath("reply/orderStatus/error/@code")
    public void setOrderStatusErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @XmlPath("reply/error/text()")
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @XmlPath("reply/orderStatus/error/text()")
    public void setOrderStatusErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getLastEvent() {
        return lastEvent;
    }

    public String getRefusedReturnCodeDescription() {
        return refusedReturnCodeDescription;
    }

    public String getRefusedReturnCode() {
        return refusedReturnCode;
    }

    @Override
    public boolean isAuthorised() {
        return AUTHORISED.equals(lastEvent);
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public String getErrorCode() {
        return trim(errorCode);
    }

    @Override
    public String getErrorMessage() {
        return trim(errorMessage);
    }
}
