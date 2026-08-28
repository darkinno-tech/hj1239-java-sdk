package io.darkinno-tech-tech.hj1239.sdk;

import io.darkinno-tech-tech.hj1239.sdk.model.DataPacket;
import io.darkinno-tech-tech.hj1239.sdk.model.EmissionData;
import io.darkinno-tech-tech.hj1239.sdk.model.VehicleInfo;
import io.darkinno-tech-tech.hj1239.sdk.model.enums.DataType;
import io.darkinno-tech-tech.hj1239.sdk.model.enums.EmissionStandard;
import io.darkinno-tech-tech.hj1239.sdk.model.enums.FuelType;
import io.darkinno-tech-tech.hj1239.sdk.validator.ValidationResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.within;

class Gb1239SdkTest {

    private final Gb1239Sdk sdk = new Gb1239Sdk();
    private static final String VIN = "LSVAM41Z6F2000001";

    @Test
    void shouldReturnVersion() { assertThat(sdk.getVersion()).isEqualTo("1.0.0"); }

    @Test
    void shouldValidateVin() {
        assertThat(sdk.validateVin(VIN)).isTrue();
        assertThat(sdk.validateVin("SHORT")).isFalse();
    }

    @Test
    void shouldValidatePlateNumber() {
        assertThat(sdk.validatePlateNumber("京A12345")).isTrue();
        assertThat(sdk.validatePlateNumber(null)).isFalse();
    }

    @Test
    void shouldEncodeDecodeHeartbeat() {
        byte[] enc = sdk.encodeHeartbeat(VIN, 1);
        DataPacket dec = sdk.decode(enc);
        assertThat(dec.getCommandId()).isEqualTo(DataType.REALTIME_DATA.getCode());
        assertThat(dec.getVehicleId()).isEqualTo(VIN);
    }

    @Test
    void shouldEncodeVehicleLogin() {
        VehicleInfo vi = VehicleInfo.builder().vin(VIN)
                .fuelType(FuelType.DIESEL)
                .emissionStandard(EmissionStandard.CHINA_VI_B).modelYear(2024).build();
        byte[] enc = sdk.encodeVehicleLogin(vi, 0);
        DataPacket dec = sdk.decode(enc);
        assertThat(dec.getCommandId()).isEqualTo(DataType.VEHICLE_LOGIN.getCode());
    }

    @Test
    void shouldEncodeDecodeRealtimeData() {
        EmissionData em = EmissionData.builder()
                .timestamp(LocalDateTime.of(2026, 5, 14, 12, 0, 0))
                .vehicleSpeed(80.0).engineSpeed(2000.0).fuelConsumptionRate(12.5)
                .engineCoolantTemp(90.0).scrUpstreamNox(60.0).scrDownstreamNox(8.0)
                .reagentRemaining(70.0).intakePressure(105.0).exhaustFlow(250.0)
                .dpfDifferentialPressure(2.0).reagentLevel(65.0)
                .positionStatus(0x01).longitude(121.473701).latitude(31.230416).odometer(50000.0)
                .build();

        byte[] enc = sdk.encodeRealtimeData(em, VIN, 5);
        DataPacket dec = sdk.decode(enc);
        EmissionData out = sdk.decodeRealtimeEmission(dec);

        assertThat(out.getVehicleSpeed()).isCloseTo(80.0, within(0.004));
        assertThat(out.getEngineSpeed()).isCloseTo(2000.0, within(0.125));
        assertThat(out.getScrUpstreamNox()).isCloseTo(60.0, within(0.05));
        assertThat(out.getLongitude()).isCloseTo(121.473701, within(0.000001));
        assertThat(out.getLatitude()).isCloseTo(31.230416, within(0.000001));
        assertThat(out.isGpsValid()).isTrue();
    }

    @Test
    void shouldValidateEmission() {
        EmissionData em = EmissionData.builder()
                .timestamp(LocalDateTime.now())
                .vehicleSpeed(60.0).engineSpeed(1500.0).fuelConsumptionRate(8.5)
                .engineCoolantTemp(85.0).scrUpstreamNox(50.0).scrDownstreamNox(10.0)
                .reagentRemaining(80.0).intakePressure(100.0).exhaustFlow(200.0)
                .dpfDifferentialPressure(1.5).reagentLevel(75.0)
                .build();
        assertThat(sdk.validateEmission(em).isValid()).isTrue();
    }

    @Test
    void shouldRejectInvalidEmission() {
        EmissionData em = EmissionData.builder()
                .timestamp(LocalDateTime.now())
                .vehicleSpeed(300.0).engineSpeed(1500.0).fuelConsumptionRate(8.5)
                .engineCoolantTemp(85.0).scrUpstreamNox(50.0).scrDownstreamNox(10.0)
                .build();
        ValidationResult r = sdk.validateEmission(em);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> e.getField().equals("vehicleSpeed"));
    }

    @Test
    void shouldExportConfig() {
        Gb1239Config cfg = new Gb1239Config();
        cfg.setStrictMode(false);
        Gb1239Sdk s = new Gb1239Sdk(cfg);
        assertThat(s.getConfig().isStrictMode()).isFalse();
    }
}
