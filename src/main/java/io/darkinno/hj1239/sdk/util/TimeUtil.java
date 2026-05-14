package io.darkinno.hj1239.sdk.util;

import java.time.LocalDateTime;

/**
 * HJ 1239.3-2021 Table 14 — Time encoding (GMT+8, BYTE[6]).
 */
public final class TimeUtil {

    private TimeUtil() {
    }

    /**
     * Encode LocalDateTime to 6-byte array: year, month, day, hour, minute, second.
     */
    public static byte[] encode(LocalDateTime dt) {
        if (dt == null) {
            throw new IllegalArgumentException("datetime must not be null");
        }
        byte[] buf = new byte[6];
        buf[0] = (byte) (dt.getYear() % 100);
        buf[1] = (byte) dt.getMonthValue();
        buf[2] = (byte) dt.getDayOfMonth();
        buf[3] = (byte) dt.getHour();
        buf[4] = (byte) dt.getMinute();
        buf[5] = (byte) dt.getSecond();
        return buf;
    }

    /**
     * Decode 6-byte array to LocalDateTime. Assumes year values 0-99 map to 2000-2099.
     */
    public static LocalDateTime decode(byte[] bytes, int offset) {
        if (bytes == null) throw new IllegalArgumentException("bytes must not be null");
        if (offset < 0 || offset + 5 >= bytes.length)
            throw new IllegalArgumentException("offset out of bounds");
        int year = (bytes[offset] & 0xFF) + 2000;
        int month = bytes[offset + 1] & 0xFF;
        int day = bytes[offset + 2] & 0xFF;
        int hour = bytes[offset + 3] & 0xFF;
        int minute = bytes[offset + 4] & 0xFF;
        int second = bytes[offset + 5] & 0xFF;
        return LocalDateTime.of(year, month, day, hour, minute, second);
    }
}
