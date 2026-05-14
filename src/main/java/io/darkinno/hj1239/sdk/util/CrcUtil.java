package io.darkinno.hj1239.sdk.util;

public final class CrcUtil {

    private CrcUtil() {
    }

    public static byte xorChecksum(byte[] data, int offset, int length) {
        if (data == null || length <= 0) {
            return 0;
        }
        byte checksum = 0;
        int end = Math.min(offset + length, data.length);
        for (int i = offset; i < end; i++) {
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
        if (data == null || length <= 0) {
            return 0;
        }
        int crc = CRC16_INIT;
        int end = Math.min(offset + length, data.length);
        for (int i = offset; i < end; i++) {
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
