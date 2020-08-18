/*
 * SMRT Solutions
 * Data Collection Platform
 */
package com.smrtsolutions.util;

import com.smrtsolutions.exception.InvalidTokenException;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import java.util.ArrayList;
import java.util.List;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

/**
 *
 * @author lenny
 */
public class TokenUtil {
    
    // Generate an RSA key pair, which will be used for signing and verification of the JWT, wrapped in a JWK
    public static RsaJsonWebKey rsaJsonWebKey ;
    
    static {
        try {
            rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        } catch (Exception e) {
            e.printStackTrace();
        }
    
    }
    
    public static SMRTUser validateToken (String tok) throws Exception{
        SMRTUser user = new SMRTUser();
        
        // Use JwtConsumerBuilder to construct an appropriate JwtConsumer, which will
        // be used to validate and process the JWT.
        // The specific validation requirements for a JWT are context dependent, however,
        // it typically advisable to require a expiration time, a trusted issuer, and
        // and audience that identifies your system as the intended recipient.
        // If the JWT is encrypted too, you need only provide a decryption key or
        // decryption key resolver to the builder.
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime() // the JWT must have an expiration time
                .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
                .setRequireSubject() // the JWT must have a subject claim
                .setExpectedIssuer("SMRT") // whom the JWT needs to have been issued by
                .setExpectedAudience("SMRTAPI") // to whom the JWT is intended for
                .setVerificationKey(rsaJsonWebKey.getKey()) // verify the signature with the public key
                .build(); // create the JwtConsumer instance

        try
        {
            //  Validate the JWT and process it to the Claims
            JwtClaims jwtClaims = jwtConsumer.processToClaims(tok);
            System.out.println("JWT validation succeeded! " + jwtClaims);
            //user.setId(Long.parseLong(jwtClaims.getSubject()));
            user.setId(jwtClaims.getSubject());
            user.setName(jwtClaims.getStringClaimValue("name"));
            user.setFirstname(jwtClaims.getStringClaimValue("firstname"));
            user.setLastname(jwtClaims.getStringClaimValue("lastname"));
            user.setEmail(jwtClaims.getStringClaimValue("email"));
            user.setUsertype(jwtClaims.getStringClaimValue("usertype"));
            Customer c = new Customer();
            c.setId(jwtClaims.getStringClaimValue("customerid"));
            user.setCustomerId(c.getId()+"");
            //user.setRoles(new ArrayList<SMRTRole>());
            user.setRoles(new ArrayList<String>());
            List<String> r = jwtClaims.getStringListClaimValue("roles");
            /*for ( String rs : r) {
                SMRTRole sr = new SMRTRole();
                sr.setIdentifier(Long.parseLong(rs));
                //user.getRoles().add(sr);
                user.getRoles().add(sr)
            }*/
            user.setRoles(r);

        }
        catch (InvalidJwtException e)
        {
            // InvalidJwtException will be thrown, if the JWT failed processing or validation in anyway.
            // Hopefully with meaningful explanations(s) about what went wrong.
            System.out.println("Invalid JWT! " + e);
            throw new InvalidTokenException();
        }

        return user;
    }
    
    public static String[] getRolesIds(List<SMRTRole> roles){
       
        String[] r =  new String[roles.size()];
        int i=0;
        for ( SMRTRole role : roles){
            r[i] = role.getIdentifier()+"";
            i++;
        }
        
        return r;
    }
    
    /*
    see SMRTUserREST
    public static String[] getPermissions(List<SMRTRole> roles){
       
        String[] r =  new String[roles.size()];
        int i=0;
        for ( SMRTRole role : roles){
            
            i++;
        }
        
        return r;
    }*/
    
    public static String generateToken(SMRTUser user) throws Exception{
        // Create the Claims, which will be the content of the JWT
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("SMRT");  // who creates the token and signs it
        claims.setAudience("SMRTAPI"); // to whom the token is intended to be sent
        claims.setExpirationTimeMinutesInTheFuture(60); // time when the token will expire (60 minutes from now)
        claims.setGeneratedJwtId(); // a unique identifier for the token
        claims.setIssuedAtToNow();  // when the token was issued/created (now)
        claims.setNotBeforeMinutesInThePast(2); // time before which the token is not yet valid (2 minutes ago)
        claims.setSubject(user.getId()+""); // the subject/principal is whom the token is about
        claims.setClaim("email",user.getEmail()); // additional claims/attributes about the subject can be added
        claims.setClaim("name", user.getName());
        claims.setClaim("firstname", user.getFirstname());
        claims.setClaim("lastname", user.getLastname());
        claims.setClaim("customerid", user.getCustomerId());
        claims.setClaim("usertype", user.getUsertype());
        //claims.setStringListClaim("roles", TokenUtil.getRolesIds(user.getRoles()));
        claims.setStringListClaim("roles", user.getRoles());
        //List<String> groups = Arrays.asList("group-one", "other-group", "group-three");
        //claims.setStringListClaim("groups", groups); // multi-valued claims work too and will end up as a JSON array

        // A JWT is a JWS and/or a JWE with JSON claims as the payload.
        // In this example it is a JWS so we create a JsonWebSignature object.
        JsonWebSignature jws = new JsonWebSignature();

        // The payload of the JWS is JSON content of the JWT Claims
        jws.setPayload(claims.toJson());

        // The JWT is signed using the private key
        jws.setKey(rsaJsonWebKey.getPrivateKey());

        // Set the Key ID (kid) header because it's just the polite thing to do.
        // We only have one key in this example but a using a Key ID helps
        // facilitate a smooth key rollover process
        jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());

        // Set the signature algorithm on the JWT/JWS that will integrity protect the claims
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

        // Sign the JWS and produce the compact serialization or the complete JWT/JWS
        // representation, which is a string consisting of three dot ('.') separated
        // base64url-encoded parts in the form Header.Payload.Signature
        // If you wanted to encrypt it, you can simply set this jwt as the payload
        // of a JsonWebEncryption object and set the cty (Content Type) header to "jwt".
        String jwt = jws.getCompactSerialization();

        return jwt;
    }
}
