package sem4.edustreambe.config;

import lombok.RequiredArgsConstructor;
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
            
            // Check if token is invalidated (ONLY for INTERNAL tokens)
            boolean isInternal = issuer == null || !issuer.contains("supabase.co");
            
            if (isInternal && jwtId != null && invalidatedTokenRepository.existsById(jwtId)) {
                throw new JwtException("Token has been invalidated (Logged Out)");
            }

            // Detect if it's a Supabase token
            if (!isInternal) {
                if (supabaseJwtSecret == null || supabaseJwtSecret.isEmpty()) {
                    throw new JwtException("Supabase JWT secret not configured");
                }
                
                // Supabase uses HS256 by default. 
                // CRITICAL: Supabase JWT secrets are often just strings, but if they are 
                // provided as Base64 in some contexts, we should be careful.
                // However, the error 401 is usually signature mismatch.
                SecretKeySpec secretKeySpec = new SecretKeySpec(supabaseJwtSecret.getBytes(), "HS256");
                return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build()
                        .decode(token);
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
