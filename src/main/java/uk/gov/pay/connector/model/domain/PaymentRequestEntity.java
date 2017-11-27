package uk.gov.pay.connector.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.pay.connector.model.domain.transaction.ChargeTransactionEntity;
import uk.gov.pay.connector.model.domain.transaction.TransactionEntity;
import uk.gov.pay.connector.model.domain.transaction.TransactionOperation;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Entity
@Table(name = "payment_requests")
@Access(AccessType.FIELD)
@SequenceGenerator(name = "payment_requests_id_seq",
        sequenceName = "payment_requests_id_seq", allocationSize = 1)
public class PaymentRequestEntity extends AbstractVersionedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_requests_id_seq")
    @JsonIgnore
    private Long id;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "return_url")
    private String returnUrl;

    @ManyToOne
    @JoinColumn(name = "gateway_account_id", updatable = false)
    private GatewayAccountEntity gatewayAccount;

    @Column(name = "description")
    private String description;

    @Column(name = "reference")
    private String reference;

    @Column(name = "created_date")
    @Convert(converter = UTCDateTimeConverter.class)
    private ZonedDateTime createdDate;

    @Column(name = "external_id")
    private String externalId;

    @OneToMany(mappedBy = "paymentRequest", cascade = CascadeType.ALL)
    private List<TransactionEntity> transactions = new ArrayList<>();

    public PaymentRequestEntity() {
        // enjoy it JPA
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public GatewayAccountEntity getGatewayAccount() {
        return gatewayAccount;
    }

    public void setGatewayAccount(GatewayAccountEntity gatewayAccount) {
        this.gatewayAccount = gatewayAccount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public List<TransactionEntity> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void addTransaction(TransactionEntity transactionEntity) {
        this.transactions.add(transactionEntity);
        transactionEntity.setPaymentRequest(this);
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
        transactions.forEach(transactionEntity -> transactionEntity.setPaymentRequest(this));
    }

    public ChargeTransactionEntity getChargeTransaction() {
        return (ChargeTransactionEntity)transactions.stream().filter(byChargeTransactions()).findFirst().orElseThrow(
                () -> new IllegalStateException("Payment Request has been initialised without a charge transaction")
        );
    }

    //Remove this once transactions table has been backfilled and we will no longer need to set this.
    public boolean hasChargeTransaction() {
        return transactions.stream().anyMatch(byChargeTransactions());
    }

    private Predicate<TransactionEntity> byChargeTransactions() {
        return transactionEntity -> transactionEntity.getOperation().equals(TransactionOperation.CHARGE);
    }

    public static PaymentRequestEntity from(ChargeEntity chargeEntity, ChargeTransactionEntity transactionEntity) {
        PaymentRequestEntity paymentEntity = new PaymentRequestEntity();
        paymentEntity.setAmount(chargeEntity.getAmount());
        paymentEntity.setCreatedDate(chargeEntity.getCreatedDate());
        paymentEntity.setDescription(chargeEntity.getDescription());
        paymentEntity.setExternalId(chargeEntity.getExternalId());
        paymentEntity.setGatewayAccount(chargeEntity.getGatewayAccount());
        paymentEntity.setReference(chargeEntity.getReference());
        paymentEntity.setReturnUrl(chargeEntity.getReturnUrl());
        paymentEntity.addTransaction(transactionEntity);

        return paymentEntity;
    }
}
