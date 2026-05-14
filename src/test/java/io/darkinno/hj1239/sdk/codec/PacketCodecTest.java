package io.darkinno.hj1239.sdk.codec;

import io.darkinno.hj1239.sdk.model.DataPacket;
import io.darkinno.hj1239.sdk.model.EmissionData;
import io.darkinno.hj1239.sdk.model.enums.DataType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class PacketCodecTest {

    private static final String VIN = "LSVAM41Z6F2000001";

    @Test
    void shouldEncodeAndDecodeHeartbeat() {
        byte[] encoded = PacketEncoder.encodeHeartbeat(VIN, 1);
        DataPacket decoded = PacketDecoder.decode(encoded);

        assertThat(decoded.getCommandId()).isEqualTo(DataType.REALTIME_DATA.getCode());
        assertThat(decoded.getVehicleId()).isEqualTo(VIN);
        assertThat(decoded.getResponseFlag()).isEqualTo(0xFE);
    }

    @Test
    void shouldEncodeAndDecodeRealtimeData() {
        EmissionData em = EmissionData.builder()
                .timestamp(LocalDateTime.of(2026, 5, 14, 12, 0, 0))
                .vehicleSpeed(60.0)
                .engineSpeed(1500.0)
                .fuelConsumptionRate(8.5)
                .engineCoolantTemp(85.0)
                .scrUpstreamNox(45.0)
                .scrDownstreamNox(5.0)
                .reagentRemaining(80.0)
                .intakePressure(100.0)
                .exhaustFlow(200.0)
                .dpfDifferentialPressure(1.5)
                .reagentLevel(75.0)
                .positionStatus(0x01)
                .longitude(116.397128)
                .latitude(39.916527)
                .odometer(12345.6)
                .build();

        byte[] encoded = PacketEncoder.encodeRealtimeData(em, VIN, 1);
        DataPacket decoded = PacketDecoder.decode(encoded);

        assertThat(decoded.getCommandId()).isEqualTo(DataType.REALTIME_DATA.getCode());
        assertThat(decoded.getVehicleId()).isEqualTo(VIN);

        EmissionData out = PacketDecoder.decodeRealtimeEmission(decoded);

        assertThat(out.getVehicleSpeed()).isCloseTo(60.0, within(0.004));
        assertThat(out.getEngineSpeed()).isCloseTo(1500.0, within(0.125));
        assertThat(out.getScrUpstreamNox()).isCloseTo(45.0, within(0.05));
        assertThat(out.getEngineCoolantTemp()).isCloseTo(85.0, within(1.0));
        assertThat(out.getLongitude()).isCloseTo(116.397128, within(0.000001));
        assertThat(out.getLatitude()).isCloseTo(39.916527, within(0.000001));
        assertThat(out.getOdometer()).isCloseTo(12345.6, within(0.1));
        assertThat(out.isGpsValid()).isTrue();
    }

    @Test
    void shouldRejectInvalidStartMarker() {
        byte[] data = new byte[25];
        data[0] = 0x00;
        data[1] = 0x00;

        assertThatThrownBy(() -> PacketDecoder.decode(data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start marker");
    }

    @Test
    void shouldRejectTooShortPacket() {
        assertThatThrownBy(() -> PacketDecoder.decode(new byte[10]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("too short");
    }

    @Test
    void shouldDetectBccMismatch() {
        byte[] valid = PacketEncoder.encodeHeartbeat(VIN, 1);
        valid[valid.length - 1] ^= 0x01;

        assertThatThrownBy(() -> PacketDecoder.decode(valid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BCC mismatch");
    }

    @Test
    void shouldHandleInvalidReadings() {
        EmissionData em = EmissionData.builder()
                .timestamp(LocalDateTime.now())
                .vehicleSpeed(-1)
                .engineSpeed(-1)
                .positionStatus(0)
                .build();

        byte[] encoded = PacketEncoder.encodeRealtimeData(em, VIN, 1);
        DataPacket decoded = PacketDecoder.decode(encoded);
        EmissionData out = PacketDecoder.decodeRealtimeEmission(decoded);

        assertThat(out.getVehicleSpeed()).isEqualTo(-1);
        assertThat(out.getEngineSpeed()).isEqualTo(-1);
        assertThat(out.isGpsValid()).isFalse();
    }
}
