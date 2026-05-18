package io.darkinno.hj1239.sdk.crypto;

public interface PacketEncryption {

    byte MODE_PLAIN = 0x01;
    byte MODE_RSA = 0x02;
    byte MODE_AES = 0x03;

    byte[] encrypt(byte[] dataUnit, int encryptionMode);

    byte[] decrypt(byte[] dataUnit, int encryptionMode);

    static boolean isPlain(int mode) {
        return mode == MODE_PLAIN;
    }

    static boolean isSupported(int mode) {
        return mode == MODE_PLAIN || mode == MODE_RSA || mode == MODE_AES;
    }
}
