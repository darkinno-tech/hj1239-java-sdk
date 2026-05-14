package io.darkinno.hj1239.sdk.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrcUtilTest {

    @Test
    void shouldComputeXorChecksum() {
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        byte result = CrcUtil.xorChecksum(data);

        assertThat(result).isEqualTo((byte) (0x01 ^ 0x02 ^ 0x03 ^ 0x04));
    }

    @Test
    void shouldReturnZeroForNullData() {
        assertThat(CrcUtil.xorChecksum(null)).isEqualTo((byte) 0);
        assertThat(CrcUtil.crc16(null)).isEqualTo(0);
    }

    @Test
    void shouldReturnZeroForEmptyData() {
        assertThat(CrcUtil.xorChecksum(new byte[0])).isEqualTo((byte) 0);
        assertThat(CrcUtil.crc16(new byte[0])).isEqualTo(0);
    }

    @Test
    void shouldComputeCrc16() {
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        int crc = CrcUtil.crc16(data);

        assertThat(crc).isNotZero();
        assertThat(crc).isLessThanOrEqualTo(0xFFFF);
    }

    @Test
    void shouldVerifyXorChecksum() {
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        byte checksum = CrcUtil.xorChecksum(data);

        assertThat(CrcUtil.verifyXorChecksum(data, 0, data.length, checksum)).isTrue();
        assertThat(CrcUtil.verifyXorChecksum(data, 0, data.length, (byte) 0x00)).isFalse();
    }
}
