// SUMMARY: This component holds the server's RSA key pair (public and private keys).
// In production, the private key would live in an HSM (Hardware Security Module)
// or at least a KMS like AWS KMS / HashiCorp Vault. NEVER hardcode keys in source code!
// For this demo we generate a fresh keypair on every startup. The public key is
// exposed via /api/server-key so sender devices can encrypt payloads.

package com.example.OFFUPI.crypto;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Holds the server's RSA keypair.
 *
 * In production, the private key would live in an HSM (Hardware Security Module)
 * or at least a KMS like AWS KMS / HashiCorp Vault. NEVER in the JAR or source.
 *
 * For this demo we generate a fresh keypair on every startup. The public key is
 * exposed via /api/server-key so the (simulated) sender devices can use it to
 * encrypt payloads.
 */

// @Component tells Spring: "Create and manage a single instance of this class"
// This is similar to @Service but more generic - it's a Spring-managed bean
@Component
public class ServerKeyHolder {

    // Logger for recording messages to the console/log file
    // Helps with debugging and monitoring
    private static final Logger log = LoggerFactory.getLogger(ServerKeyHolder.class);

    // The key pair contains both public key (share with everyone) and private key (keep secret)
    private KeyPair keyPair;

    // @PostConstruct means: "Run this method AFTER Spring creates the object but BEFORE it's used"
    // This is the perfect place for initialization/startup code
    @PostConstruct
    public void init() throws Exception {

        // KeyPairGenerator creates RSA key pairs (public + private together)
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");

        // Initialize with 2048-bit key size (standard security level)
        // Higher bits = more secure but slower (4096 is also common)
        gen.initialize(2048);

        // Generate the actual key pair
        this.keyPair = gen.generateKeyPair();

        // Log a success message with part of the public key for identification
        // First 32 characters of base64-encoded public key as a fingerprint
        log.info("Server RSA keypair generated (2048-bit). Public key fingerprint: {}",
                getPublicKeyBase64().substring(0, 32) + "...");
    }

    // Getter method to retrieve the public key (can be shared with anyone)
    // Public key is used to ENCRYPT data that only we can decrypt
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    // Getter method to retrieve the private key (MUST be kept secret)
    // Private key is used to DECRYPT data that was encrypted with our public key
    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    // Returns the public key as a Base64 string for easy transmission via HTTP/JSON
    // Frontend apps can use this string to encrypt messages to our server
    public String getPublicKeyBase64() {
        // getEncoded() gives the raw bytes of the key
        // Base64 encodes those bytes into a safe text string
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
}