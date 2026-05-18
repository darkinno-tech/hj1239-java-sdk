package io.darkinno.hj1239.sdk.crypto;

import io.darkinno.hj1239.sdk.codec.ByteBuf;
import io.darkinno.hj1239.sdk.model.DataPacket;
import io.darkinno.hj1239.sdk.model.enums.DataType;
import io.darkinno.hj1239.sdk.util.CrcUtil;
import io.darkinno.hj1239.sdk.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.time.LocalDateTime;

public final class KeyExchangeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(KeyExchangeHandler.class);

    private KeyExchangeHandler() {}

    public static byte[] encodeKeyExchangeRequest(String vin, int seq, int algorithm) {
        ByteBuf du = new ByteBuf(128);
        du.writeBytes(TimeUtil.encode(LocalDateTime.now()));
        du.writeShort((short) seq);
        du.writeByte((byte) algorithm);
        return build(DataType.KEY_EXCHANGE.getCode(), 0xFE, 0x01, vin, du.toByteArray());
    }

    public static byte[] encodeKeyExchangeRequestWithRsaKey(String vin, int seq) {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048, new SecureRandom());
            KeyPair kp = gen.generateKeyPair();
            byte[] pubKey = kp.getPublic().getEncoded();

            ByteBuf du = new ByteBuf(512);
            du.writeBytes(TimeUtil.encode(LocalDateTime.now()));
            du.writeShort((short) seq);
            du.writeByte((byte) 0x01);
            du.writeShort((short) pubKey.length);
            du.writeBytes(pubKey);

            return build(DataType.KEY_EXCHANGE.getCode(), 0xFE, 0x01, vin, du.toByteArray());
        } catch (Exception e) {
            LOG.error("Failed to generate RSA key pair", e);
            throw new RuntimeException("Key exchange failed", e);
        }
    }

    public static byte[] encodeKeyExchangeResponse(String vin, int seq, int algorithm, byte[] publicKey) {
        ByteBuf du = new ByteBuf(512);
        du.writeBytes(TimeUtil.encode(LocalDateTime.now()));
        du.writeShort((short) seq);
        du.writeByte((byte) algorithm);
        du.writeShort((short) publicKey.length);
        du.writeBytes(publicKey);
        return build(DataType.KEY_EXCHANGE.getCode(), (short) seq, 0x01, vin, du.toByteArray());
    }

    public static class KeyExchangeData {
        public final LocalDateTime timestamp;
        public final int seq;
        public final int algorithm;
        public final byte[] publicKey;

        KeyExchangeData(LocalDateTime timestamp, int seq, int algorithm, byte[] publicKey) {
            this.timestamp = timestamp;
            this.seq = seq;
            this.algorithm = algorithm;
            this.publicKey = publicKey;
        }
    }

    public static KeyExchangeData decodeKeyExchange(DataPacket pkt) {
        byte[] du = pkt.getDataUnit();
        if (du == null || du.length < 9) {
            throw new IllegalArgumentException("Key exchange data too short");
        }
        ByteBuf b = new ByteBuf(du);
        LocalDateTime ts = TimeUtil.decode(du, 0);
        b.readBytes(6);
        int seq = b.readShort() & 0xFFFF;
        int algorithm = b.readByte() & 0xFF;
        byte[] pubKey = new byte[0];
        if (du.length > 9) {
            int keyLen = b.readShort() & 0xFFFF;
            pubKey = b.readBytes(keyLen);
        }
        return new KeyExchangeData(ts, seq, algorithm, pubKey);
    }

    private static byte[] build(int cmd, int resp, int encrypt, String vehicleId, byte[] du) {
        ByteBuf buf = new ByteBuf(2048);
        buf.writeBytes(DataPacket.START_MARKER);
        buf.writeByte((byte) cmd);
        buf.writeByte((byte) resp);
        buf.writeString(vehicleId, 17);
        buf.writeByte((byte) encrypt);
        buf.writeShort((short) du.length);
        buf.writeBytes(du);
        byte[] arr = buf.toByteArray();
        byte bcc = CrcUtil.xorChecksum(arr, 2, arr.length - 2);
        buf.writeByte(bcc);
        return buf.toByteArray();
    }
}
