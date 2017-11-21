package uk.gov.pay.connector.model.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Entity
@Table(name = "gateway_accounts")
@SequenceGenerator(name = "gateway_accounts_gateway_account_id_seq", sequenceName = "gateway_accounts_gateway_account_id_seq", allocationSize = 1)
public class GatewayAccountEntity extends AbstractEntity {

    public class Views {
        public class ApiView { }
        public class FrontendView {}
    }

    public enum Type {
        TEST("test"), LIVE("live");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }

        public static Type fromString(String type) {
            for (Type typeEnum : Type.values()) {
                if (typeEnum.toString().equalsIgnoreCase(type)) {
                    return typeEnum;
                }
            }
            throw new IllegalArgumentException("gateway account type has to be one of (test, live)");
        }
    }

    public GatewayAccountEntity() {}

    //TODO: Should we rename the columns to be more consistent?
    @Column(name = "payment_provider")
    private String gatewayName;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    //TODO: Revisit this to map to a java.util.Map
    @Column(name = "credentials", columnDefinition = "json")
    @Convert(converter = CredentialsConverter.class)
    private Map<String, String> credentials;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "description")
    private String description;

    @Column(name = "analytics_id")
    private String analyticsId;

    @Column(name = "requires_3ds")
    private boolean requires3ds;

    @Column(name = "notify_settings", columnDefinition = "json")
    @Convert(converter = JsonToMapConverter.class)
    private Map<String, String> notifySettings;

    @JsonBackReference
    @OneToOne(mappedBy = "accountEntity", cascade = CascadeType.PERSIST)
    private EmailNotificationEntity emailNotification;

    @OneToOne(mappedBy = "accountEntity", cascade = CascadeType.PERSIST)
    private NotificationCredentials notificationCredentials;

    @ManyToMany
    @JoinTable(
            name = "accepted_card_types",
            joinColumns = @JoinColumn(name = "gateway_account_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "card_type_id", referencedColumnName = "id")
    )
    private List<CardTypeEntity> cardTypes = newArrayList();

    public GatewayAccountEntity(String gatewayName, Map<String, String> credentials, Type type) {
        this.gatewayName = gatewayName;
        this.credentials = credentials;
        this.type = type;
    }

    @Override
    @JsonProperty("gateway_account_id")
    @JsonView({Views.ApiView.class, Views.FrontendView.class})
    public Long getId() {
        return super.getId();
    }

    @JsonProperty("payment_provider")
    @JsonView(value = {Views.ApiView.class, Views.FrontendView.class})
    public String getGatewayName() {
        return gatewayName;
    }

    @JsonView(Views.ApiView.class)
    public Map<String, String> getCredentials() {
        return credentials;
    }

    @JsonView(Views.ApiView.class)
    public String getDescription() {
        return description;
    }

    @JsonView(value = {Views.ApiView.class, Views.FrontendView.class})
    @JsonProperty("analytics_id")
    public String getAnalyticsId() {
        return analyticsId;
    }

    @JsonProperty("type")
    @JsonView(value = {Views.ApiView.class, Views.FrontendView.class})
    public String getType() {
        return type.value;
    }

    @JsonProperty("service_name")
    @JsonView(value = {Views.ApiView.class, Views.FrontendView.class})
    public String getServiceName() {
        return serviceName;
    }

    @JsonView(Views.FrontendView.class)
    @JsonProperty("card_types")
    public List<CardTypeEntity> getCardTypes() {
        return cardTypes;
    }

    @JsonView(Views.ApiView.class)
    public EmailNotificationEntity getEmailNotification() {
        return emailNotification;
    }

    @JsonView(Views.ApiView.class)
    public NotificationCredentials getNotificationCredentials() {
        return notificationCredentials;
    }

    public boolean isRequires3ds() {
        return requires3ds;
    }

    public void setNotificationCredentials(NotificationCredentials notificationCredentials) {
        this.notificationCredentials = notificationCredentials;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAnalyticsId(String analyticsId) {
        this.analyticsId = analyticsId;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setCardTypes(List<CardTypeEntity> cardTypes) {
        this.cardTypes = cardTypes;
    }

    public void setEmailNotification(EmailNotificationEntity emailNotification) {
        this.emailNotification = emailNotification;
    }

    public void setRequires3ds(boolean requires3ds) {
        this.requires3ds = requires3ds;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setNotifySettings(Map<String, String> notifySettings){
        this.notifySettings = notifySettings;
    }

    public Map<String, String> getNotifySettings() {
        return notifySettings;
    }

    public Map<String, String> withoutCredentials() {
        Map<String, String> account = newHashMap();
        account.put("gateway_account_id", String.valueOf(super.getId()));
        account.put("payment_provider", getGatewayName());
        account.put("type", getType());
        if (isNotBlank(getDescription())) {
            account.put("description", getDescription());
        }
        if (isNotBlank(getAnalyticsId())) {
            account.put("analytics_id", getAnalyticsId());
        }
        if (isNotBlank(getServiceName())) {
            account.put("service_name", getServiceName());
        }
        account.put("toggle_3ds", String.valueOf(isRequires3ds()));
        return account;
    }

    public boolean hasEmailNotificationsEnabled() {
        return emailNotification != null && emailNotification.isEnabled();
    }

    public boolean hasAnyAcceptedCardType3dsRequired() {
        return cardTypes.stream()
                .anyMatch(CardTypeEntity::isRequires3ds);
    }
}
