package uk.gov.pay.connector.charge.util;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.util.Map;

public class JwtGenerator {

    public String createJwt(Map<String, Object> claims, String secret) {
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .addClaims(claims)
                .signWith(secret_key, SignatureAlgorithm.HS256)
                .compact();
    }
}
