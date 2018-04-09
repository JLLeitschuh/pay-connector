package uk.gov.pay.connector.service.worldpay;

import org.eclipse.persistence.oxm.annotations.XmlPath;
import uk.gov.pay.connector.model.ChargeStatusRequest;
import uk.gov.pay.connector.model.domain.ChargeStatus;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

@XmlRootElement(name = "paymentService")
public class WorldpayNotification implements ChargeStatusRequest {

    public WorldpayNotification() {};

    public WorldpayNotification(String merchantCode, String status, int dayOfMonth, int month, int year, String transactionId, String reference) {
        this.merchantCode = merchantCode;
        this.status = status;
        this.dayOfMonth = dayOfMonth;
        this.month = month;
        this.year = year;
        this.transactionId = transactionId;
        this.reference = reference;
    }

    @XmlPath("@merchantCode")
    private String merchantCode;

    @XmlPath("notify/orderStatusEvent/journal/@journalType")
    private String status;

    @XmlPath("notify/orderStatusEvent/journal/bookingDate/date/@dayOfMonth")
    private int dayOfMonth;

    @XmlPath("notify/orderStatusEvent/journal/bookingDate/date/@month")
    private int month;

    @XmlPath("notify/orderStatusEvent/journal/bookingDate/date/@year")
    private int year;

    @XmlPath("notify/orderStatusEvent/@orderCode")
    private String transactionId;

    @XmlPath("notify/orderStatusEvent/journal/journalReference/@reference")
    private String reference;

    private Optional<ChargeStatus> chargeStatus = Optional.empty();

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public Optional<ChargeStatus> getChargeStatus() {
        return chargeStatus;
    }

    public void setChargeStatus(Optional<ChargeStatus> chargeStatus) {
        this.chargeStatus = chargeStatus;
    }

    public String getStatus() {
        return status;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public String getReference() {
        return reference;
    }

    public LocalDate getBookingDate() {
        return LocalDate.of(year, month, dayOfMonth);
    }

    public ZonedDateTime getGatewayEventDate() {
        return getBookingDate().atStartOfDay(ZoneOffset.UTC);
    }

    @Override
    public String toString() {
        return "WorldpayNotification{" +
                "merchantCode='" + merchantCode + '\'' +
                ", status='" + status + '\'' +
                ", dayOfMonth=" + dayOfMonth +
                ", month=" + month +
                ", year=" + year +
                ", transactionId='" + transactionId + '\'' +
                ", reference='" + reference + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorldpayNotification that = (WorldpayNotification) o;

        if (dayOfMonth != that.dayOfMonth) return false;
        if (month != that.month) return false;
        if (year != that.year) return false;
        if (merchantCode != null ? !merchantCode.equals(that.merchantCode) : that.merchantCode != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (transactionId != null ? !transactionId.equals(that.transactionId) : that.transactionId != null)
            return false;
        return reference != null ? reference.equals(that.reference) : that.reference == null;
    }

    @Override
    public int hashCode() {
        int result = merchantCode != null ? merchantCode.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + dayOfMonth;
        result = 31 * result + month;
        result = 31 * result + year;
        result = 31 * result + (transactionId != null ? transactionId.hashCode() : 0);
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        return result;
    }
}
