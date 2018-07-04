package uk.gov.pay.connector.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.connector.cqrs.EventCommandHandler;
import uk.gov.pay.connector.exception.InvalidStateTransitionException;
import uk.gov.pay.connector.model.api.ExternalChargeState;
import uk.gov.pay.connector.service.PaymentGatewayName;
import uk.gov.pay.connector.util.RandomIdGenerator;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static uk.gov.pay.connector.model.domain.ChargeStatus.CAPTURED;
import static uk.gov.pay.connector.model.domain.ChargeStatus.CAPTURE_SUBMITTED;
import static uk.gov.pay.connector.model.domain.ChargeStatus.CREATED;
import static uk.gov.pay.connector.model.domain.ChargeStatus.fromString;
import static uk.gov.pay.connector.model.domain.PaymentGatewayStateTransitions.isValidTransition;

@Entity
@Table(name = "charges")
@Access(AccessType.FIELD)
@SequenceGenerator(name = "charges_charge_id_seq",
        sequenceName = "charges_charge_id_seq", allocationSize = 1)
public class ChargeEntity extends AbstractVersionedEntity {
    private final static Logger logger = LoggerFactory.getLogger(ChargeEntity.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "charges_charge_id_seq")
    @JsonIgnore
    private Long id;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "status")
    private String status;

    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;

    @Column(name = "return_url")
    private String returnUrl;

    @Column(name = "email")
    private String email;

    @Embedded
    private CardDetailsEntity cardDetails;

    @Embedded
    private Auth3dsDetailsEntity auth3dsDetails;

    @ManyToOne
    @JoinColumn(name = "gateway_account_id", updatable = false)
    private GatewayAccountEntity gatewayAccount;

    @OneToMany(mappedBy = "chargeEntity", fetch = FetchType.EAGER)
    @OrderBy("createdDate")
    private List<RefundEntity> refunds = new ArrayList<>();

    @OneToMany(mappedBy = "chargeEntity")
    @OrderBy("updated DESC")
    private List<ChargeEventEntity> events = new ArrayList<>();

    @Column(name = "description")
    private String description;

    @Column(name = "reference")
    private String reference;

    @Column(name = "provider_session_id")
    private String providerSessionId;

    @Column(name = "created_date")
    @Convert(converter = UTCDateTimeConverter.class)
    private ZonedDateTime createdDate;

    public ChargeEntity() {
        //for jpa
    }

    public ChargeEntity(Long amount, String returnUrl, String description, String reference, GatewayAccountEntity gatewayAccount, String email) {
        this(amount, CREATED, returnUrl, description, reference, gatewayAccount, email, ZonedDateTime.now(ZoneId.of("UTC")));
    }

    //for fixture
    ChargeEntity(Long amount, ChargeStatus status, String returnUrl, String description, String reference,
                 GatewayAccountEntity gatewayAccount, String email, ZonedDateTime createdDate) {
        this.amount = amount;
        this.status = status.getValue();
        this.returnUrl = returnUrl;
        this.description = description;
        this.reference = reference;
        this.gatewayAccount = gatewayAccount;
        this.createdDate = createdDate;
        this.externalId = RandomIdGenerator.newId();
        this.email = email;
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

    public String getStatus() {
        return status;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public GatewayAccountEntity getGatewayAccount() {
        return gatewayAccount;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public List<RefundEntity> getRefunds() {
        return refunds;
    }

    public List<ChargeEventEntity> getEvents() {
        return events;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getEmail() {
        return email;
    }

    public String getProviderSessionId() {
        return providerSessionId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setStatus(ChargeStatus targetStatus, EventCommandHandler eventCommandHandler) throws InvalidStateTransitionException {
        if (isValidTransition(fromString(this.status), targetStatus)) {
            logger.info(String.format("Changing charge status for externalId [%s] [%s]->[%s]",
                    externalId,
                    this.status,
                    targetStatus.getValue())
            );
            this.status = targetStatus.getValue();
            eventCommandHandler.handleSuccessfulChargeEvent(this);
        } else {
            throw new InvalidStateTransitionException(this.status, targetStatus.getValue());
        }
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public void setGatewayAccount(GatewayAccountEntity gatewayAccount) {
        this.gatewayAccount = gatewayAccount;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProviderSessionId(String providerSessionId) {
        this.providerSessionId = providerSessionId;
    }

    public boolean hasStatus(ChargeStatus... status) {
        return Arrays.stream(status).anyMatch(s -> equalsIgnoreCase(s.getValue(), getStatus()));
    }

    public boolean hasStatus(List<ChargeStatus> status) {
        return hasStatus(status.toArray(new ChargeStatus[0]));
    }

    public boolean hasExternalStatus(ExternalChargeState... state) {
        return Arrays.stream(state).anyMatch(s -> fromString(getStatus()).toExternal().equals(s));
    }

    public long getTotalAmountToBeRefunded() {
        return this.amount - getRefundedAmount();
    }

    public long getRefundedAmount() {
        return this.refunds.stream()
                .filter(p -> p.hasStatus(RefundStatus.CREATED, RefundStatus.REFUND_SUBMITTED, RefundStatus.REFUNDED))
                .mapToLong(RefundEntity::getAmount)
                .sum();
    }

    public ZonedDateTime getCaptureSubmitTime() {
        return this.events.stream()
                .filter(e -> e.getStatus().equals(CAPTURE_SUBMITTED))
                .findFirst()
                .map(ChargeEventEntity::getUpdated)
                .orElse(null);
    }

    public ZonedDateTime getCapturedTime() {
        return this.events.stream()
                .filter(e -> e.getStatus().equals(CAPTURED))
                .findFirst()
                // use updated for old CAPTURED cqrs that do not have a generated time recorded
                .map(e -> e.getGatewayEventDate().orElse(e.getUpdated()))
                .orElse(null);
    }

    public CardDetailsEntity getCardDetails() {
        return cardDetails;
    }

    public void setCardDetails(CardDetailsEntity cardDetailsEntity) {
        this.cardDetails = cardDetailsEntity;
    }

    public PaymentGatewayName getPaymentGatewayName() {
        return PaymentGatewayName.valueFrom(gatewayAccount.getGatewayName());
    }

    public Auth3dsDetailsEntity get3dsDetails() {
        return auth3dsDetails;
    }

    public void set3dsDetails(Auth3dsDetailsEntity auth3dsDetails) {
        this.auth3dsDetails = auth3dsDetails;
    }
}
