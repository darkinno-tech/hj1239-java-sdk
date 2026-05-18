package io.darkinno.hj1239.sdk.util;

public final class CrcUtil {

    private CrcUtil() {
    }

    public static byte xorChecksum(byte[] data, int offset, int length) {
        if (data == null) throw new IllegalArgumentException("data must not be null");
        if (offset < 0 || offset >= data.length)
            throw new IllegalArgumentException("offset " + offset + " out of bounds [0, " + (data.length - 1) + "]");
        if (length <= 0) throw new IllegalArgumentException("length must be positive, got " + length);
        if (offset + length > data.length)
            throw new IllegalArgumentException("offset+length " + (offset + length) + " exceeds data length " + data.length);
        byte checksum = 0;
        for (int i = offset; i < offset + length; i++) {
            checksum ^= data[i];
        }
        return checksum;
    }

    public static byte xorChecksum(byte[] data) {
        if (data == null || data.length == 0) {
            return 0;
        }
        return xorChecksum(data, 0, data.length);
    }

    private static final int CRC16_INIT = 0xFFFF;
    private static final int CRC16_POLY = 0xA001;

    public static int crc16(byte[] data, int offset, int length) {
        if (data == null) throw new IllegalArgumentException("data must not be null");
        if (offset < 0 || offset >= data.length)
            throw new IllegalArgumentException("offset " + offset + " out of bounds [0, " + (data.length - 1) + "]");
        if (length <= 0) throw new IllegalArgumentException("length must be positive, got " + length);
        if (offset + length > data.length)
            throw new IllegalArgumentException("offset+length " + (offset + length) + " exceeds data length " + data.length);
        int crc = CRC16_INIT;
        for (int i = offset; i < offset + length; i++) {
            crc ^= (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >>> 1) ^ CRC16_POLY;
                } else {
                    crc = crc >>> 1;
                }
            }
        }
        return crc & 0xFFFF;
    }

    public static int crc16(byte[] data) {
        if (data == null || data.length == 0) {
            return 0;
        }
        return crc16(data, 0, data.length);
    }

    public static boolean verifyXorChecksum(byte[] data, int offset, int length, byte expected) {
        return xorChecksum(data, offset, length) == expected;
    }

    public static boolean verifyCrc16(byte[] data, int offset, int length, int expected) {
        return crc16(data, offset, length) == expected;
    }
}
