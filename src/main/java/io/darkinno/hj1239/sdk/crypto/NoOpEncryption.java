package io.darkinno.hj1239.sdk.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpEncryption implements PacketEncryption {

    private static final Logger LOG = LoggerFactory.getLogger(NoOpEncryption.class);

    @Override
    public byte[] encrypt(byte[] dataUnit, int encryptionMode) {
        if (encryptionMode == MODE_PLAIN) {
            return dataUnit;
        }
        LOG.warn("Encryption mode 0x{} not supported by NoOpEncryption, passing through",
                Integer.toHexString(encryptionMode));
        return dataUnit;
    }

    @Override
    public byte[] decrypt(byte[] dataUnit, int encryptionMode) {
        if (encryptionMode == MODE_PLAIN) {
            return dataUnit;
        }
        LOG.warn("Encryption mode 0x{} not supported by NoOpEncryption, passing through",
                Integer.toHexString(encryptionMode));
        return dataUnit;
    }
}
