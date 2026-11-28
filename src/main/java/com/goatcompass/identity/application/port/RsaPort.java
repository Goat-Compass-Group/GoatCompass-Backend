package com.goatcompass.identity.application.port;

public interface RsaPort {
    String getPublicKey();

    String decrypt(String encryptedText);
}
