package uk.gov.pay.connector.gatewayaccount.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Worldpay3dsFlexCredentials {

    private String issuer;
    private String organisationalUnitId;

    @JsonIgnore
    private String jwtMacKey;

    public Worldpay3dsFlexCredentials(String issuer, String organisationalUnitId, String jwtMacKey) {
        this.issuer = issuer;
        this.organisationalUnitId = organisationalUnitId;
        this.jwtMacKey = jwtMacKey;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getOrganisationalUnitId() {
        return organisationalUnitId;
    }

    public String getJwtMacKey() {
        return jwtMacKey;
    }

    public static Worldpay3dsFlexCredentials fromEntity(Worldpay3dsFlexCredentialsEntity entity) {
        return new Worldpay3dsFlexCredentials(entity.getIssuer(), entity.getOrganisationalUnitId(), entity.getJwtMacKey());
    }
}
