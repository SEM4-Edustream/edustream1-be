package sem4.edustreambe.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import sem4.edustreambe.repository.InvalidatedTokenRepository;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Value("${supabase.jwt-secret:}")
    private String supabaseJwtSecret;

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            com.nimbusds.jwt.SignedJWT signedJWT = com.nimbusds.jwt.SignedJWT.parse(token);
            var claims = signedJWT.getJWTClaimsSet();
            String jwtId = claims.getJWTID();
            String issuer = claims.getIssuer();

            log.debug("JWT decode - issuer: {}, jti: {}", issuer, jwtId);

            // Detect if it's a Supabase token
            boolean isSupabase = issuer != null && issuer.contains("supabase");

            if (isSupabase) {
                if (supabaseJwtSecret == null || supabaseJwtSecret.isEmpty()) {
                    log.error("Supabase JWT secret is not configured!");
                    throw new JwtException("Supabase JWT secret not configured");
                }

                log.debug("Decoding Supabase token with HS256");
                // Supabase JWT secrets are Base64-encoded
                byte[] keyBytes = Base64.getDecoder().decode(supabaseJwtSecret);
                SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
                return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build()
                        .decode(token);
            }

            // Internal token: check invalidation
            if (jwtId != null && invalidatedTokenRepository.existsById(jwtId)) {
                throw new JwtException("Token has been invalidated (Logged Out)");
            }

            // Default to internal HS512 token
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
            return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build()
                    .decode(token);

        } catch (ParseException e) {
            throw new JwtException("Invalid token format");
        }
    }
}
