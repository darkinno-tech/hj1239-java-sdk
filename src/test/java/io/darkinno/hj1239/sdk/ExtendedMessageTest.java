package io.darkinno.hj1239.sdk;

import io.darkinno.hj1239.sdk.codec.PacketDecoder;
import io.darkinno.hj1239.sdk.codec.PacketEncoder;
import io.darkinno.hj1239.sdk.crypto.KeyExchangeHandler;
import io.darkinno.hj1239.sdk.model.*;
import io.darkinno.hj1239.sdk.model.enums.DataType;
import io.darkinno.hj1239.sdk.model.enums.EmissionStandard;
import io.darkinno.hj1239.sdk.model.enums.FuelType;
import io.darkinno.hj1239.sdk.validator.EmissionValidator;
import io.darkinno.hj1239.sdk.validator.ValidationResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.within;

class ExtendedMessageTest {

    private static final String VIN = "LSVAM41Z6F2000001";

    @Test
    void shouldEncodeAndDecodeVehicleLogout() {
        byte[] enc = PacketEncoder.encodeVehicleLogout(VIN, 3);
        DataPacket dec = PacketDecoder.decode(enc);

        assertThat(dec.getCommandId()).isEqualTo(DataType.VEHICLE_LOGOUT.getCode());
        assertThat(dec.getVehicleId()).isEqualTo(VIN);
        assertThat(PacketDecoder.isUpload(dec)).isTrue();
    }

    @Test
    void shouldEncodeComplementData() {
        EmissionData em = EmissionData.builder()
                .timestamp(LocalDateTime.of(2026, 5, 14, 12, 0, 0))
                .vehicleSpeed(70.0).engineSpeed(1800.0).fuelConsumptionRate(10.0)
                .engineCoolantTemp(88.0).scrUpstreamNox(55.0).scrDownstreamNox(7.0)
                .reagentRemaining(75.0).intakePressure(102.0).exhaustFlow(220.0)
                .dpfDifferentialPressure(1.8).reagentLevel(70.0)
                .positionStatus(0x01).longitude(116.5).latitude(39.9).odometer(30000.0)
                .build();

        byte[] enc = PacketEncoder.encodeComplementData(em, VIN, 5);
        DataPacket dec = PacketDecoder.decode(enc);

        assertThat(dec.getCommandId()).isEqualTo(DataType.REPLENISH_DATA.getCode());
        EmissionData out = PacketDecoder.decodeRealtimeEmission(dec);
        assertThat(out.getVehicleSpeed()).isCloseTo(70.0, within(0.004));
    }

    @Test
    void shouldEncodeTimeSync() {
        byte[] enc = PacketEncoder.encodeTimeSync(VIN, 1);
        DataPacket dec = PacketDecoder.decode(enc);

        assertThat(dec.getCommandId()).isEqualTo(DataType.TERMINAL_TIME_SYNC.getCode());
        assertThat(dec.getVehicleId()).isEqualTo(VIN);
    }

    @Test
    void shouldEncodePlatformLogin() {
        byte[] enc = PacketEncoder.encodePlatformLogin("PLT001", "admin", "pass123", 1);
        DataPacket dec = PacketDecoder.decode(enc);

        assertThat(dec.getCommandId()).isEqualTo(DataType.PLATFORM_LOGIN.getCode());
    }

    @Test
    void shouldEncodePlatformLogout() {
        byte[] enc = PacketEncoder.encodePlatformLogout("PLT001", 2);
        DataPacket dec = PacketDecoder.decode(enc);

        assertThat(dec.getCommandId()).isEqualTo(DataType.PLATFORM_LOGOUT.getCode());
    }

    @Test
    void shouldEncodeAndDecodeObdEngineData() {
        ObdEngineData obd = ObdEngineData.builder()
                .timestamp(LocalDateTime.of(2026, 5, 14, 12, 0, 0))
                .milOn(true).dtcCount(3).egrErrorRate(5.0)
                .scrSystemStatus(1).dpfSystemStatus(0)
                .dpfSootLoad(45.0).dpfAshLoad(2.5)
                .engineRuntime(360000).positionStatus(0x01)
                .longitude(116.397128).latitude(39.916527).odometer(50000.0)
                .build();

        byte[] enc = PacketEncoder.encodeObdEngineData(obd, VIN, 1);
        DataPacket dec = PacketDecoder.decode(enc);
        ObdEngineData out = PacketDecoder.decodeObdEngineData(dec);

        assertThat(out.isMilOn()).isTrue();
        assertThat(out.getDtcCount()).isEqualTo(3);
        assertThat(out.getEgrErrorRate()).isCloseTo(5.0, within(0.5));
        assertThat(out.getScrSystemStatus()).isEqualTo(1);
        assertThat(out.getDpfSootLoad()).isCloseTo(45.0, within(0.5));
        assertThat(out.getDpfAshLoad()).isCloseTo(2.5, within(0.1));
        assertThat(out.getEngineRuntime()).isEqualTo(360000);
        assertThat(out.getLongitude()).isCloseTo(116.397128, within(0.000001));
    }

    @Test
    void shouldEncodeAndDecodeHybridData() {
        HybridData hd = HybridData.builder()
                .timestamp(LocalDateTime.of(2026, 5, 14, 12, 0, 0))
                .vehicleSpeed(60.0).engineSpeed(1500.0).motorSpeed(3000.0)
                .motorTorque(150.0).batterySoc(75.0).batteryVoltage(350.0)
                .batteryCurrent(20.0).fuelConsumptionRate(5.0)
                .engineCoolantTemp(85.0).hybridMode(1).positionStatus(0x01)
                .longitude(121.473701).latitude(31.230416).odometer(30000.0)
                .build();

        byte[] enc = PacketEncoder.encodeHybridData(hd, VIN, 1);
        DataPacket dec = PacketDecoder.decode(enc);
        HybridData out = PacketDecoder.decodeHybridData(dec);

        assertThat(out.getVehicleSpeed()).isCloseTo(60.0, within(0.004));
        assertThat(out.getEngineSpeed()).isCloseTo(1500.0, within(0.125));
        assertThat(out.getMotorSpeed()).isCloseTo(3000.0, within(0.25));
        assertThat(out.getMotorTorque()).isCloseTo(150.0, within(0.5));
        assertThat(out.getBatterySoc()).isCloseTo(75.0, within(0.5));
        assertThat(out.getBatteryVoltage()).isCloseTo(350.0, within(0.1));
        assertThat(out.getBatteryCurrent()).isCloseTo(20.0, within(0.1));
        assertThat(out.getHybridMode()).isEqualTo(1);
    }

    @Test
    void shouldEncodeAndDecodeEmissionCheck() {
        EmissionCheckData ec = EmissionCheckData.builder()
                .timestamp(LocalDateTime.of(2026, 5, 14, 12, 0, 0))
                .checkType(1).noxPemsValue(120.0).coPemsValue(15.0).pmPemsValue(2.5)
                .exhaustFlow(300.0).engineSpeed(2000.0).engineTorquePercent(60.0)
                .engineCoolantTemp(88.0).positionStatus(0x01)
                .longitude(116.5).latitude(39.9).odometer(40000.0)
                .build();

        byte[] enc = PacketEncoder.encodeEmissionCheck(ec, VIN, 1);
        DataPacket dec = PacketDecoder.decode(enc);
        EmissionCheckData out = PacketDecoder.decodeEmissionCheckData(dec);

        assertThat(out.getCheckType()).isEqualTo(1);
        assertThat(out.getNoxPemsValue()).isCloseTo(120.0, within(0.05));
        assertThat(out.getCoPemsValue()).isCloseTo(15.0, within(0.01));
        assertThat(out.getPmPemsValue()).isCloseTo(2.5, within(0.01));
        assertThat(out.getExhaustFlow()).isCloseTo(300.0, within(0.05));
    }

    @Test
    void shouldValidateObdEngineData() {
        ObdEngineData valid = ObdEngineData.builder()
                .timestamp(LocalDateTime.now())
                .milOn(true).dtcCount(5).egrErrorRate(10.0)
                .dpfSootLoad(50.0).dpfAshLoad(5.0)
                .build();
        assertThat(EmissionValidator.validate(valid).isValid()).isTrue();

        ObdEngineData invalid = ObdEngineData.builder()
                .timestamp(LocalDateTime.now())
                .milOn(false).dtcCount(5).egrErrorRate(200.0)
                .build();
        assertThat(EmissionValidator.validate(invalid).isValid()).isFalse();
    }

    @Test
    void shouldValidateHybridData() {
        HybridData valid = HybridData.builder()
                .timestamp(LocalDateTime.now())
                .vehicleSpeed(60.0).engineSpeed(1500.0).motorSpeed(3000.0)
                .motorTorque(150.0).batterySoc(75.0).batteryVoltage(350.0)
                .batteryCurrent(20.0).hybridMode(1)
                .build();
        assertThat(EmissionValidator.validate(valid).isValid()).isTrue();

        HybridData invalid = HybridData.builder()
                .timestamp(LocalDateTime.now())
                .vehicleSpeed(60.0).batterySoc(150.0)
                .build();
        assertThat(EmissionValidator.validate(invalid).isValid()).isFalse();
    }

    @Test
    void shouldValidateEmissionCheckData() {
        EmissionCheckData valid = EmissionCheckData.builder()
                .timestamp(LocalDateTime.now())
                .checkType(1).noxPemsValue(100.0).coPemsValue(10.0).pmPemsValue(2.0)
                .exhaustFlow(250.0).engineSpeed(1800.0)
                .build();
        assertThat(EmissionValidator.validate(valid).isValid()).isTrue();

        EmissionCheckData invalid = EmissionCheckData.builder()
                .timestamp(LocalDateTime.now())
                .checkType(1).noxPemsValue(5000.0)
                .build();
        assertThat(EmissionValidator.validate(invalid).isValid()).isFalse();
    }

    @Test
    void shouldEncodeAndDecodeKeyExchange() {
        byte[] enc = KeyExchangeHandler.encodeKeyExchangeRequest(VIN, 1, 0x01);
        DataPacket dec = PacketDecoder.decode(enc);

        assertThat(dec.getCommandId()).isEqualTo(DataType.KEY_EXCHANGE.getCode());
        assertThat(dec.getVehicleId()).isEqualTo(VIN);

        KeyExchangeHandler.KeyExchangeData kd = KeyExchangeHandler.decodeKeyExchange(dec);
        assertThat(kd.algorithm).isEqualTo(0x01);
    }

    @Test
    void shouldEncodeKeyExchangeWithRsaKey() {
        byte[] enc = KeyExchangeHandler.encodeKeyExchangeRequestWithRsaKey(VIN, 1);
        DataPacket dec = PacketDecoder.decode(enc);
        KeyExchangeHandler.KeyExchangeData kd = KeyExchangeHandler.decodeKeyExchange(dec);

        assertThat(kd.algorithm).isEqualTo(0x01);
        assertThat(kd.publicKey).isNotEmpty();
        assertThat(kd.publicKey.length).isGreaterThan(256);
    }

    @Test
    void shouldDecodeVehicleLoginWithAllFields() {
        VehicleInfo vi = VehicleInfo.builder()
                .vin(VIN).fuelType(FuelType.DIESEL)
                .emissionStandard(EmissionStandard.CHINA_VI_B)
                .plateNumber("BJ12345").plateColor("BLUE")
                .manufacturer("DFM").model("TianLong")
                .modelYear(2024)
                .build();

        byte[] enc = PacketEncoder.encodeVehicleLogin(vi, 0);
        DataPacket dec = PacketDecoder.decode(enc);
        VehicleInfo out = PacketDecoder.decodeVehicleLogin(dec);

        assertThat(out.getVin()).isEqualTo(VIN);
        assertThat(out.getFuelType()).isEqualTo(FuelType.DIESEL);
        assertThat(out.getEmissionStandard()).isEqualTo(EmissionStandard.CHINA_VI_B);
        assertThat(out.getPlateNumber()).isEqualTo("BJ12345");
        assertThat(out.getPlateColor()).isEqualTo("BLUE");
        assertThat(out.getManufacturer()).isEqualTo("DFM");
        assertThat(out.getModel()).isEqualTo("TianLong");
        assertThat(out.getModelYear()).isEqualTo(2024);
    }

    @Test
    void shouldHandleCorruptedPacketsGracefully() {
        Gb1239Config relaxed = new Gb1239Config();
        relaxed.setStrictMode(false);
        relaxed.setEnableValidation(false);

        byte[] enc = PacketEncoder.encodeHeartbeat(VIN, 1);
        enc[enc.length - 1] ^= 0x01;

        DataPacket dec = PacketDecoder.decode(enc, relaxed);
        assertThat(dec.getVehicleId()).isEqualTo(VIN);
    }

    @Test
    void shouldVerifyPacketCommandTypes() {
        byte[] enc = PacketEncoder.encodeHeartbeat(VIN, 1);
        DataPacket dec = PacketDecoder.decode(enc);

        assertThat(PacketDecoder.isUpload(dec)).isTrue();
        assertThat(PacketDecoder.isResponse(dec)).isFalse();
        assertThat(PacketDecoder.getCommandType(dec)).isEqualTo(DataType.REALTIME_DATA);
    }
}
