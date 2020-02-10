package uk.gov.pay.connector.paritycheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LedgerTransaction {

    private String transactionId;
    private Long amount;
    private String description;
    private String reference;
    private String email;
    private boolean delayedCapture;
    private Long corporateCardSurcharge;
    private Long totalAmount;
    private Long fee;
    private Long netAmount;
    private String createdDate;
    private TransactionState state;

    public Long getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public String getEmail() {
        return email;
    }

    public boolean getDelayedCapture() {
        return delayedCapture;
    }

    public Long getCorporateCardSurcharge() {
        return corporateCardSurcharge;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public Long getFee() {
        return fee;
    }

    public Long getNetAmount() {
        return netAmount;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionState getState() {
        return state;
    }

    public void setState(TransactionState transactionState) {
        this.state = transactionState;
    }

    public static final class LedgerTransactionBuilder {
        private String transactionId;
        private Long amount;
        private String description;
        private String reference;
        private String email;
        private boolean delayedCapture;
        private Long corporateCardSurcharge;
        private Long totalAmount;
        private Long fee;
        private Long netAmount;
        private String createdDate;
        private TransactionState state;

        private LedgerTransactionBuilder() {
        }

        public static LedgerTransactionBuilder aLedgerTransaction() {
            return new LedgerTransactionBuilder();
        }

        public LedgerTransactionBuilder withTransactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public LedgerTransactionBuilder withAmount(Long amount) {
            this.amount = amount;
            return this;
        }

        public LedgerTransactionBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public LedgerTransactionBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public LedgerTransactionBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public LedgerTransactionBuilder withDelayedCapture(boolean delayedCapture) {
            this.delayedCapture = delayedCapture;
            return this;
        }

        public LedgerTransactionBuilder withCorporateCardSurcharge(Long corporateCardSurcharge) {
            this.corporateCardSurcharge = corporateCardSurcharge;
            return this;
        }

        public LedgerTransactionBuilder withTotalAmount(Long totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public LedgerTransactionBuilder withFee(Long fee) {
            this.fee = fee;
            return this;
        }

        public LedgerTransactionBuilder withNetAmount(Long netAmount) {
            this.netAmount = netAmount;
            return this;
        }

        public LedgerTransactionBuilder withCreatedDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public LedgerTransactionBuilder withState(TransactionState state) {
            this.state = state;
            return this;
        }

        public LedgerTransaction build() {
            LedgerTransaction ledgerTransaction = new LedgerTransaction();
            ledgerTransaction.setState(state);
            ledgerTransaction.email = this.email;
            ledgerTransaction.corporateCardSurcharge = this.corporateCardSurcharge;
            ledgerTransaction.transactionId = this.transactionId;
            ledgerTransaction.amount = this.amount;
            ledgerTransaction.fee = this.fee;
            ledgerTransaction.totalAmount = this.totalAmount;
            ledgerTransaction.reference = this.reference;
            ledgerTransaction.description = this.description;
            ledgerTransaction.createdDate = this.createdDate;
            ledgerTransaction.netAmount = this.netAmount;
            ledgerTransaction.delayedCapture = this.delayedCapture;
            return ledgerTransaction;
        }
    }
}
