// SUMMARY: This service handles hybrid encryption (mixing RSA and AES) for secure payment messages.
// Hybrid encryption is used by real-world systems like TLS (HTTPS), PGP (email encryption), and Signal (messaging).
// Why hybrid? RSA alone is slow and can only encrypt small amounts of data (245 bytes max for 2048-bit RSA).
// Our payment data (like sender, receiver, amount) plus signatures could be larger than that.
// Solution: Use AES (fast, can handle any size) to encrypt the actual payment, then use RSA to encrypt just the AES key.

package com.example.OFFUPI.crypto;

import com.example.OFFUPI.entity.PaymentInstruction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

/**
 * Hybrid encryption — the same pattern used by TLS, PGP, Signal, etc.
 *
 * Why hybrid? RSA can only encrypt small data (~245 bytes for a 2048-bit key).
 * Our payment instruction (JSON) might be ~300 bytes, and in real use we might
 * include device certificates and signatures pushing it well over.
 *
 * Solution: generate a fresh AES key per packet, encrypt the JSON with AES-GCM
 * (fast + authenticated), then encrypt JUST the AES key with RSA-OAEP.
 *
 * Wire format (after base64 encoding):
 *   [ 256 bytes RSA-encrypted AES key ][ 12 bytes GCM IV ][ ciphertext + 16-byte tag ]
 *
 * AES-GCM is authenticated encryption: any single-bit tampering with the ciphertext
 * causes decryption to fail with an exception. This is what makes it safe for
 * untrusted intermediates to hold.
 */

// @Service tells Spring: "This class contains business logic for encryption/decryption"
// Spring will create one instance of this service that other classes can use
@Service
public class HybridCryptoService {

    // RSA transformation string - tells Java how to perform RSA encryption
    // "RSA/ECB/OAEPWithSHA-256AndMGF1Padding" means:
    // - RSA: the algorithm
    // - ECB: encryption mode (not really used with RSA, but required in the string)
    // - OAEPWithSHA-256AndMGF1Padding: padding scheme that adds randomness and security
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    // AES transformation with GCM mode - GCM provides both encryption AND integrity checking
    // "AES/GCM/NoPadding" means:
    // - AES: Advanced Encryption Standard algorithm
    // - GCM: Galois/Counter Mode (authenticated encryption mode)
    // - NoPadding: GCM doesn't need padding, it can handle any size data
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";

    // AES key size in bits - 256 bits is the strongest available for AES
    private static final int AES_KEY_BITS = 256;

    // GCM IV (Initialization Vector) length in bytes - 12 bytes is standard for GCM
    // IV is like a salt - it ensures same data encrypted twice produces different output
    private static final int GCM_IV_BYTES = 12;

    // GCM authentication tag size - 128 bits (16 bytes) of tamper-proof verification
    // This tag proves the data hasn't been modified
    private static final int GCM_TAG_BITS = 128;

    // RSA encrypted key size - for 2048-bit RSA, encrypted output is always 256 bytes
    private static final int RSA_ENCRYPTED_KEY_BYTES = 256; // for 2048-bit RSA

    // SecureRandom generates cryptographically strong random numbers (used for IVs)
    // This is much more secure than regular Random class
    private final SecureRandom rng = new SecureRandom();

    // ObjectMapper converts Java objects to JSON and vice versa (Jackson library)
    private final ObjectMapper json = new ObjectMapper();

    // Inject the server's key holder to get RSA keys
    // This gives us access to server's private key for decryption
    @Autowired
    private ServerKeyHolder serverKey;

    /**
     * Encrypt a payment instruction with the server's public key.
     * Called by the simulated sender device.
     *
     * Flow: Generate random AES key → Encrypt payment with AES → Encrypt AES key with RSA
     */
    public String encrypt(PaymentInstruction instruction, PublicKey serverPublicKey) throws Exception {

        // Step 1: Convert the payment instruction object into JSON bytes
        // JSON format example: {"senderId":"alice","amount":100,"receiverId":"bob"}
        byte[] plaintext = json.writeValueAsBytes(instruction);

        // Step 2: Generate a brand new AES key for THIS payment only (one-time use)
        // This is called an "ephemeral key" - used once then discarded
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(AES_KEY_BITS);  // Tell generator to create a 256-bit key
        SecretKey aesKey = kg.generateKey();  // Generate the actual key

        // Step 3: Create a random IV (Initialization Vector) for AES-GCM
        // IV ensures that even if you encrypt the same data twice, results are different
        byte[] iv = new byte[GCM_IV_BYTES];
        rng.nextBytes(iv);  // Fill with random bytes

        // Initialize AES cipher in ENCRYPT mode with our key and IV
        Cipher aes = Cipher.getInstance(AES_TRANSFORMATION);
        aes.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));

        // Perform the actual AES encryption
        // This returns ciphertext + authentication tag combined
        byte[] aesCiphertext = aes.doFinal(plaintext);

        // Step 4: Encrypt the AES key using RSA and the server's public key
        // RSA encrypts a small amount of data (the AES key) which is only 32 bytes
        Cipher rsa = Cipher.getInstance(RSA_TRANSFORMATION);

        // OAEP parameter spec - adds extra security to RSA encryption
        // Prevents certain attacks by adding random padding
        OAEPParameterSpec oaep = new OAEPParameterSpec(
                "SHA-256",  // Hash algorithm
                "MGF1",     // Mask generation function
                MGF1ParameterSpec.SHA256,  // Parameters for MGF1
                PSource.PSpecified.DEFAULT  // Default source for encoding
        );

        rsa.init(Cipher.ENCRYPT_MODE, serverPublicKey, oaep);
        byte[] encryptedAesKey = rsa.doFinal(aesKey.getEncoded());  // Encrypt the AES key

        // Step 5: Package everything into a single byte array
        // Format: [RSA-encrypted AES key (256 bytes)] + [IV (12 bytes)] + [AES ciphertext + tag]
        ByteBuffer buf = ByteBuffer.allocate(encryptedAesKey.length + iv.length + aesCiphertext.length);
        buf.put(encryptedAesKey);  // Put encrypted key first
        buf.put(iv);               // Put IV second
        buf.put(aesCiphertext);    // Put encrypted data last

        // Step 6: Convert to Base64 string for easy transmission (email, JSON, HTTP)
        // Base64 turns binary data into text that can be safely sent in URLs and JSON
        return Base64.getEncoder().encodeToString(buf.array());
    }

    /**
     * Decrypt with the server's private key.
     * If anything has been tampered with — wrong key, modified ciphertext,
     * truncated input — this throws an exception.
     *
     * This is the reverse process of encrypt() above
     */
    public PaymentInstruction decrypt(String base64Ciphertext) throws Exception {

        // Step 1: Convert from Base64 string back to raw bytes
        byte[] all = Base64.getDecoder().decode(base64Ciphertext);

        // Step 2: Basic validation - make sure the message is long enough
        // Minimum length = RSA key (256) + IV (12) + GCM tag (16 bytes = 128 bits)
        if (all.length < RSA_ENCRYPTED_KEY_BYTES + GCM_IV_BYTES + GCM_TAG_BITS / 8) {
            throw new IllegalArgumentException("Ciphertext too short");
        }

        // Step 3: Unpack the three parts of the message
        // Part 1: The RSA-encrypted AES key (first 256 bytes)
        byte[] encryptedAesKey = new byte[RSA_ENCRYPTED_KEY_BYTES];

        // Part 2: The GCM IV (next 12 bytes)
        byte[] iv = new byte[GCM_IV_BYTES];

        // Part 3: The actual AES ciphertext + authentication tag (remaining bytes)
        byte[] aesCiphertext = new byte[all.length - RSA_ENCRYPTED_KEY_BYTES - GCM_IV_BYTES];

        // Use ByteBuffer to easily extract the three parts
        ByteBuffer buf = ByteBuffer.wrap(all);
        buf.get(encryptedAesKey);  // Extract encrypted AES key
        buf.get(iv);               // Extract IV
        buf.get(aesCiphertext);    // Extract ciphertext

        // Step 4: Decrypt the AES key using server's RSA private key
        Cipher rsa = Cipher.getInstance(RSA_TRANSFORMATION);
        OAEPParameterSpec oaep = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        rsa.init(Cipher.DECRYPT_MODE, serverKey.getPrivateKey(), oaep);

        // Decrypt to get the original AES key bytes
        byte[] aesKeyBytes = rsa.doFinal(encryptedAesKey);

        // Convert bytes back into a SecretKey object
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        // Step 5: Decrypt the payment data with AES-GCM
        Cipher aes = Cipher.getInstance(AES_TRANSFORMATION);
        aes.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));

        // This step also verifies the authentication tag
        // If someone tampered with the data, this line throws an exception
        byte[] plaintext = aes.doFinal(aesCiphertext);

        // Step 6: Convert JSON bytes back into a PaymentInstruction object
        return json.readValue(plaintext, PaymentInstruction.class);
    }

    /**
     * SHA-256 of the ciphertext. THIS is the idempotency key.
     *
     * Why ciphertext and not packetId? Because intermediates can rewrite packetId
     * but cannot forge a valid ciphertext for a different payload. Two delivered
     * copies of the same packet have identical ciphertexts, hence identical hashes.
     *
     * Idempotency means: processing the same payment twice has the same effect as processing it once.
     * This hash helps detect duplicate payments.
     */
    public String hashCiphertext(String base64Ciphertext) throws Exception {

        // MessageDigest creates a hash (digital fingerprint) of the data
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        // Convert string to bytes and compute hash
        byte[] hash = sha256.digest(base64Ciphertext.getBytes());

        // Convert hash bytes to hexadecimal string (human-readable format)
        // Example output: "a3f5c9e2d1b4..." - 64 characters long
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            // %02x converts each byte to a 2-character hex value
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}