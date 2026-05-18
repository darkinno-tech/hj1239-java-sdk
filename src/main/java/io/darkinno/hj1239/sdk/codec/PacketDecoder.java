package io.darkinno.hj1239.sdk.codec;

import io.darkinno.hj1239.sdk.Gb1239Config;
import io.darkinno.hj1239.sdk.model.*;
import io.darkinno.hj1239.sdk.model.enums.DataType;
import io.darkinno.hj1239.sdk.model.enums.EmissionStandard;
import io.darkinno.hj1239.sdk.model.enums.FuelType;
import io.darkinno.hj1239.sdk.model.enums.MessageType;
import io.darkinno.hj1239.sdk.util.CrcUtil;
import io.darkinno.hj1239.sdk.util.TimeUtil;
import io.darkinno.hj1239.sdk.validator.EmissionValidator;
import io.darkinno.hj1239.sdk.validator.ValidationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static io.darkinno.hj1239.sdk.codec.PacketEncoder.*;

/**
 * HJ 1239.3-2021 packet decoder.
 */
public final class PacketDecoder {

    private static final int VIN_LEN = 17;
    private static final int HEADER_LEN = 2 + 1 + 1 + VIN_LEN + 1 + 2;
    private static final Logger LOG = LoggerFactory.getLogger(PacketDecoder.class);
    private static final Gb1239Config DEFAULT_STRICT_NO_VALIDATION;

    static {
        DEFAULT_STRICT_NO_VALIDATION = new Gb1239Config();
        DEFAULT_STRICT_NO_VALIDATION.setEnableValidation(false);
    }

    private PacketDecoder() {}

    public static DataPacket decode(byte[] raw) {
        return decode(raw, DEFAULT_STRICT_NO_VALIDATION);
    }

    public static DataPacket decode(byte[] raw, Gb1239Config config) {
        if (raw == null || raw.length < HEADER_LEN + 1) {
            throw new IllegalArgumentException("Packet too short, min " + (HEADER_LEN + 1) + " bytes");
        }
        ByteBuf b = new ByteBuf(raw);

        byte s0 = b.readByte(), s1 = b.readByte();
        if (s0 != DataPacket.START_MARKER[0] || s1 != DataPacket.START_MARKER[1]) {
            throw new IllegalArgumentException("Invalid start marker: 0x"
                    + Integer.toHexString(s0 & 0xFF) + " 0x" + Integer.toHexString(s1 & 0xFF));
        }
        int cmd = b.readByte() & 0xFF;
        int resp = b.readByte() & 0xFF;
        String vin = b.readString(VIN_LEN);
        int enc = b.readByte() & 0xFF;
        int duLen = b.readShort() & 0xFFFF;

        if (duLen > raw.length - HEADER_LEN - 1) {
            throw new IllegalArgumentException("Data unit length " + duLen + " exceeds remaining bytes");
        }
        byte[] du = duLen > 0 ? b.readBytes(duLen) : new byte[0];

        byte bcc = b.readByte();
        byte calc = CrcUtil.xorChecksum(raw, 2, raw.length - 3);
        if (bcc != calc) {
            if (config.isStrictMode()) {
                throw new IllegalArgumentException("BCC mismatch: got 0x"
                        + Integer.toHexString(bcc & 0xFF) + ", calc 0x" + Integer.toHexString(calc & 0xFF));
            }
            LOG.warn("BCC mismatch for vin={}: got 0x{}, calc 0x{}", vin,
                    Integer.toHexString(bcc & 0xFF), Integer.toHexString(calc & 0xFF));
        }
        return DataPacket.builder().commandId(cmd).responseFlag(resp)
                .vehicleId(vin).encryptionMode(enc).dataUnit(du).build();
    }

    public static EmissionData decodeRealtimeEmission(DataPacket pkt) {
        return decodeRealtimeEmission(pkt, DEFAULT_STRICT_NO_VALIDATION);
    }

    public static EmissionData decodeRealtimeEmission(DataPacket pkt, Gb1239Config config) {
        if (pkt == null) throw new IllegalArgumentException("packet must not be null");
        byte[] du = pkt.getDataUnit();
        if (du == null || du.length < 15) {
            throw new IllegalArgumentException("Realtime data too short, min 15 bytes");
        }
        ByteBuf b = new ByteBuf(du);

        LocalDateTime ts = TimeUtil.decode(du, 0);
        b.readBytes(6);
        int seq = b.readShort() & 0xFFFF;
        int msgType = b.readByte() & 0xFF;
        b.readBytes(6);

        EmissionData result;
        switch (msgType) {
            case 0x02:
                result = decodeTable5Body(ts, b);
                break;
            case 0x03:
            case 0x05:
                result = decodeTwcNoxBody(ts, b);
                break;
            default:
                if (config.isStrictMode()) {
                    throw new IllegalArgumentException("Unsupported message type: 0x"
                            + Integer.toHexString(msgType));
                }
                LOG.warn("Unsupported message type 0x{} for vin={}, returning empty data",
                        Integer.toHexString(msgType), pkt.getVehicleId());
                return EmissionData.builder().timestamp(ts).build();
        }

        if (config.isEnableValidation()) {
            ValidationResult vr = EmissionValidator.validate(result);
            if (!vr.isValid()) {
                if (config.isStrictMode()) {
                    throw new IllegalArgumentException("Emission validation failed: " + vr.getErrors());
                }
                LOG.warn("Emission validation warnings for vin={}: {}", pkt.getVehicleId(), vr.getErrors());
            }
        }
        return result;
    }

    public static ObdEngineData decodeObdEngineData(DataPacket pkt) {
        return decodeObdEngineData(pkt, DEFAULT_STRICT_NO_VALIDATION);
    }

    public static ObdEngineData decodeObdEngineData(DataPacket pkt, Gb1239Config config) {
        if (pkt == null) throw new IllegalArgumentException("packet must not be null");
        byte[] du = pkt.getDataUnit();
        if (du == null || du.length < 25) {
            throw new IllegalArgumentException("OBD data too short, min 25 bytes");
        }
        ByteBuf b = new ByteBuf(du);
        LocalDateTime ts = TimeUtil.decode(du, 0);
        b.readBytes(6);
        /* int seq = */ b.readShort();
        int msgType = b.readByte() & 0xFF;
        b.readBytes(6);
        if (msgType != MessageType.OBD_ENGINE_DATA.getCode()) {
            throw new IllegalArgumentException("Expected OBD message type 0x01, got 0x"
                    + Integer.toHexString(msgType));
        }
        return decodeObdBody(ts, b);
    }

    public static HybridData decodeHybridData(DataPacket pkt) {
        return decodeHybridData(pkt, DEFAULT_STRICT_NO_VALIDATION);
    }

    public static HybridData decodeHybridData(DataPacket pkt, Gb1239Config config) {
        if (pkt == null) throw new IllegalArgumentException("packet must not be null");
        byte[] du = pkt.getDataUnit();
        if (du == null || du.length < 28) {
            throw new IllegalArgumentException("Hybrid data too short, min 28 bytes");
        }
        ByteBuf b = new ByteBuf(du);
        LocalDateTime ts = TimeUtil.decode(du, 0);
        b.readBytes(6);
        /* int seq = */ b.readShort();
        int msgType = b.readByte() & 0xFF;
        b.readBytes(6);
        if (msgType != MessageType.ENGINE_HYBRID_DATA.getCode()) {
            throw new IllegalArgumentException("Expected hybrid message type 0x04, got 0x"
                    + Integer.toHexString(msgType));
        }
        return decodeHybridBody(ts, b);
    }

    public static EmissionCheckData decodeEmissionCheckData(DataPacket pkt) {
        return decodeEmissionCheckData(pkt, DEFAULT_STRICT_NO_VALIDATION);
    }

    public static EmissionCheckData decodeEmissionCheckData(DataPacket pkt, Gb1239Config config) {
        if (pkt == null) throw new IllegalArgumentException("packet must not be null");
        byte[] du = pkt.getDataUnit();
        if (du == null || du.length < 26) {
            throw new IllegalArgumentException("Emission check data too short, min 26 bytes");
        }
        ByteBuf b = new ByteBuf(du);
        LocalDateTime ts = TimeUtil.decode(du, 0);
        b.readBytes(6);
        /* int seq = */ b.readShort();
        int msgType = b.readByte() & 0xFF;
        b.readBytes(6);
        if (msgType != MessageType.EMISSION_CHECK_DATA.getCode()) {
            throw new IllegalArgumentException("Expected emission check message type 0x80, got 0x"
                    + Integer.toHexString(msgType));
        }
        return decodeEmissionCheckBody(ts, b);
    }

    public static VehicleInfo decodeVehicleLogin(DataPacket pkt) {
        return decodeVehicleLogin(pkt, DEFAULT_STRICT_NO_VALIDATION);
    }

    public static VehicleInfo decodeVehicleLogin(DataPacket pkt, Gb1239Config config) {
        if (pkt == null) throw new IllegalArgumentException("packet must not be null");
        byte[] du = pkt.getDataUnit();
        if (du == null || du.length < 26) {
            throw new IllegalArgumentException("Vehicle login data too short, min 26 bytes, got "
                    + (du != null ? du.length : 0));
        }
        ByteBuf b = new ByteBuf(du);

        /* LocalDateTime ts = */ TimeUtil.decode(du, 0);
        b.readBytes(6);
        b.readShort();
        String vin = b.readString(17);
        int fuelCode = b.readByte() & 0xFF;
        FuelType fuelType = FuelType.fromCode(fuelCode);

        EmissionStandard emissionStandard = EmissionStandard.UNKNOWN;
        String plateNumber = "";
        String plateColor = "";
        String manufacturer = "";
        String model = "";
        int modelYear = 0;

        if (du.length >= 27) {
            emissionStandard = EmissionStandard.fromCode(b.readByte() & 0xFF);
        }
        if (du.length >= 39) {
            plateNumber = b.readString(12).trim();
        }
        if (du.length >= 47) {
            plateColor = b.readString(8).trim();
        }
        if (du.length >= 79) {
            manufacturer = b.readString(32).trim();
        }
        if (du.length >= 111) {
            model = b.readString(32).trim();
        }
        if (du.length >= 113) {
            modelYear = b.readShort() & 0xFFFF;
        }

        return VehicleInfo.builder()
                .vin(vin)
                .fuelType(fuelType)
                .emissionStandard(emissionStandard)
                .plateNumber(!plateNumber.isEmpty() ? plateNumber : null)
                .plateColor(!plateColor.isEmpty() ? plateColor : null)
                .manufacturer(!manufacturer.isEmpty() ? manufacturer : null)
                .model(!model.isEmpty() ? model : null)
                .modelYear(modelYear)
                .build();
    }

    private static EmissionData decodeTable5Body(LocalDateTime ts, ByteBuf b) {
        int spdRaw = b.readShort() & 0xFFFF;
        int intakeRaw = b.readByte() & 0xFF;
        int tqRaw = b.readByte() & 0xFF;
        int ftqRaw = b.readByte() & 0xFF;
        int rpmRaw = b.readShort() & 0xFFFF;
        int fuelRaw = b.readShort() & 0xFFFF;
        int noxUpRaw = b.readShort() & 0xFFFF;
        int noxDnRaw = b.readShort() & 0xFFFF;
        int reaRaw = b.readByte() & 0xFF;
        int exfRaw = b.readShort() & 0xFFFF;
        int scrInRaw = b.readShort() & 0xFFFF;
        int scrOutRaw = b.readShort() & 0xFFFF;
        int dpfRaw = b.readShort() & 0xFFFF;
        int coolRaw = b.readByte() & 0xFF;
        int reaLvlRaw = b.readByte() & 0xFF;
        int posStat = b.readByte() & 0xFF;
        long lonRaw = b.readInt() & 0xFFFFFFFFL;
        long latRaw = b.readInt() & 0xFFFFFFFFL;
        long odoRaw = b.readInt() & 0xFFFFFFFFL;

        return EmissionData.builder()
                .timestamp(ts)
                .vehicleSpeed(spdRaw != INV_WORD ? spdRaw / SPD_SCALE : -1)
                .intakePressure(intakeRaw != INV_BYTE ? intakeRaw * INTAKE_P_SCALE : -1)
                .engineTorquePercent(tqRaw != INV_BYTE ? tqRaw * TORQUE_SCALE + TORQUE_OFFSET : Double.NaN)
                .frictionTorquePercent(ftqRaw != INV_BYTE ? ftqRaw * TORQUE_SCALE + TORQUE_OFFSET : Double.NaN)
                .engineSpeed(rpmRaw != INV_WORD ? rpmRaw * ENG_SCALE : -1)
                .fuelConsumptionRate(fuelRaw != INV_WORD ? fuelRaw * FUEL_SCALE : -1)
                .scrUpstreamNox(noxUpRaw != INV_WORD ? noxUpRaw * NOX_SCALE + NOX_OFFSET : -1)
                .scrDownstreamNox(noxDnRaw != INV_WORD ? noxDnRaw * NOX_SCALE + NOX_OFFSET : -1)
                .reagentRemaining(reaRaw != INV_BYTE ? reaRaw * REAGENT_SCALE : -1)
                .exhaustFlow(exfRaw != INV_WORD ? exfRaw * EXHAUST_SCALE : -1)
                .scrInletTemp(scrInRaw != INV_WORD ? scrInRaw * SCR_TEMP_SCALE + SCR_TEMP_OFFSET : Double.NaN)
                .scrOutletTemp(scrOutRaw != INV_WORD ? scrOutRaw * SCR_TEMP_SCALE + SCR_TEMP_OFFSET : Double.NaN)
                .dpfDifferentialPressure(dpfRaw != INV_WORD ? dpfRaw * DPF_SCALE : -1)
                .engineCoolantTemp(coolRaw != INV_BYTE ? coolRaw * COOLANT_SCALE + COOLANT_OFFSET : Double.NaN)
                .reagentLevel(reaLvlRaw != INV_BYTE ? reaLvlRaw * REAGENT_SCALE : -1)
                .positionStatus(posStat)
                .longitude(lonRaw != INV_DWORD ? lonRaw * DMS_SCALE : -1)
                .latitude(latRaw != INV_DWORD ? latRaw * DMS_SCALE : -1)
                .odometer(odoRaw != INV_DWORD ? odoRaw * ODO_SCALE : -1)
                .build();
    }

    private static EmissionData decodeTwcNoxBody(LocalDateTime ts, ByteBuf b) {
        int spdRaw = b.readShort() & 0xFFFF;
        int rpmRaw = b.readShort() & 0xFFFF;
        int fuelRaw = b.readShort() & 0xFFFF;
        int noxUpRaw = b.readShort() & 0xFFFF;
        int noxDnRaw = b.readShort() & 0xFFFF;
        int o2s1Raw = b.readShort() & 0xFFFF;
        int o2s2Raw = b.readShort() & 0xFFFF;
        int tqRaw = b.readByte() & 0xFF;
        int twcRaw = b.readShort() & 0xFFFF;
        int exfRaw = b.readShort() & 0xFFFF;
        int posStat = b.readByte() & 0xFF;
        long lonRaw = b.readInt() & 0xFFFFFFFFL;
        long latRaw = b.readInt() & 0xFFFFFFFFL;
        long odoRaw = b.readInt() & 0xFFFFFFFFL;

        return EmissionData.builder()
                .timestamp(ts)
                .vehicleSpeed(spdRaw != INV_WORD ? spdRaw / SPD_SCALE : -1)
                .engineSpeed(rpmRaw != INV_WORD ? rpmRaw * ENG_SCALE : -1)
                .fuelConsumptionRate(fuelRaw != INV_WORD ? fuelRaw * FUEL_SCALE : -1)
                .scrUpstreamNox(noxUpRaw != INV_WORD ? noxUpRaw * NOX_SCALE + NOX_OFFSET : -1)
                .scrDownstreamNox(noxDnRaw != INV_WORD ? noxDnRaw * NOX_SCALE + NOX_OFFSET : -1)
                .engineTorquePercent(tqRaw != INV_BYTE ? tqRaw * TORQUE_SCALE + TORQUE_OFFSET : Double.NaN)
                .exhaustFlow(exfRaw != INV_WORD ? exfRaw * EXHAUST_SCALE : -1)
                .positionStatus(posStat)
                .longitude(lonRaw != INV_DWORD ? lonRaw * DMS_SCALE : -1)
                .latitude(latRaw != INV_DWORD ? latRaw * DMS_SCALE : -1)
                .odometer(odoRaw != INV_DWORD ? odoRaw * ODO_SCALE : -1)
                .build();
    }

    private static ObdEngineData decodeObdBody(LocalDateTime ts, ByteBuf b) {
        boolean milOn = (b.readByte() & 0xFF) == 0x01;
        int dtcCount = b.readByte() & 0xFF;
        int egrRaw = b.readByte() & 0xFF;
        int scrStat = b.readByte() & 0xFF;
        int dpfStat = b.readByte() & 0xFF;
        int sootRaw = b.readByte() & 0xFF;
        int ashRaw = b.readByte() & 0xFF;
        long runtime = b.readInt() & 0xFFFFFFFFL;
        int posStat = b.readByte() & 0xFF;
        long lonRaw = b.readInt() & 0xFFFFFFFFL;
        long latRaw = b.readInt() & 0xFFFFFFFFL;
        long odoRaw = b.readInt() & 0xFFFFFFFFL;

        return ObdEngineData.builder()
                .timestamp(ts)
                .milOn(milOn)
                .dtcCount(dtcCount)
                .egrErrorRate(egrRaw != INV_BYTE ? egrRaw * EGR_SCALE : -1)
                .scrSystemStatus(scrStat)
                .dpfSystemStatus(dpfStat)
                .dpfSootLoad(sootRaw != INV_BYTE ? sootRaw * SOOT_SCALE : -1)
                .dpfAshLoad(ashRaw != INV_BYTE ? ashRaw * ASH_SCALE : -1)
                .engineRuntime(runtime)
                .positionStatus(posStat)
                .longitude(lonRaw != INV_DWORD ? lonRaw * DMS_SCALE : -1)
                .latitude(latRaw != INV_DWORD ? latRaw * DMS_SCALE : -1)
                .odometer(odoRaw != INV_DWORD ? odoRaw * ODO_SCALE : -1)
                .build();
    }

    private static HybridData decodeHybridBody(LocalDateTime ts, ByteBuf b) {
        int spdRaw = b.readShort() & 0xFFFF;
        int engRaw = b.readShort() & 0xFFFF;
        int motRaw = b.readShort() & 0xFFFF;
        int tqRaw = b.readShort() & 0xFFFF;
        int socRaw = b.readByte() & 0xFF;
        int voltRaw = b.readShort() & 0xFFFF;
        int curRaw = b.readShort() & 0xFFFF;
        int fuelRaw = b.readShort() & 0xFFFF;
        int coolRaw = b.readByte() & 0xFF;
        int mode = b.readByte() & 0xFF;
        int posStat = b.readByte() & 0xFF;
        long lonRaw = b.readInt() & 0xFFFFFFFFL;
        long latRaw = b.readInt() & 0xFFFFFFFFL;
        long odoRaw = b.readInt() & 0xFFFFFFFFL;

        return HybridData.builder()
                .timestamp(ts)
                .vehicleSpeed(spdRaw != INV_WORD ? spdRaw / SPD_SCALE : -1)
                .engineSpeed(engRaw != INV_WORD ? engRaw * ENG_SCALE : -1)
                .motorSpeed(motRaw != INV_WORD ? motRaw * MOTOR_SPEED_SCALE : -1)
                .motorTorque(tqRaw != INV_WORD ? tqRaw * MOTOR_TORQUE_SCALE + MOTOR_TORQUE_OFFSET : Double.NaN)
                .batterySoc(socRaw != INV_BYTE ? socRaw * SOC_SCALE : -1)
                .batteryVoltage(voltRaw != INV_WORD ? voltRaw * VOLTAGE_SCALE : -1)
                .batteryCurrent(curRaw != INV_WORD ? curRaw * CURRENT_SCALE + CURRENT_OFFSET : Double.NaN)
                .fuelConsumptionRate(fuelRaw != INV_WORD ? fuelRaw * FUEL_SCALE : -1)
                .engineCoolantTemp(coolRaw != INV_BYTE ? coolRaw * COOLANT_SCALE + COOLANT_OFFSET : Double.NaN)
                .hybridMode(mode)
                .positionStatus(posStat)
                .longitude(lonRaw != INV_DWORD ? lonRaw * DMS_SCALE : -1)
                .latitude(latRaw != INV_DWORD ? latRaw * DMS_SCALE : -1)
                .odometer(odoRaw != INV_DWORD ? odoRaw * ODO_SCALE : -1)
                .build();
    }

    private static EmissionCheckData decodeEmissionCheckBody(LocalDateTime ts, ByteBuf b) {
        int checkType = b.readByte() & 0xFF;
        int noxRaw = b.readShort() & 0xFFFF;
        int coRaw = b.readShort() & 0xFFFF;
        int pmRaw = b.readShort() & 0xFFFF;
        int exfRaw = b.readShort() & 0xFFFF;
        int rpmRaw = b.readShort() & 0xFFFF;
        int tqRaw = b.readByte() & 0xFF;
        int coolRaw = b.readByte() & 0xFF;
        int posStat = b.readByte() & 0xFF;
        long lonRaw = b.readInt() & 0xFFFFFFFFL;
        long latRaw = b.readInt() & 0xFFFFFFFFL;
        long odoRaw = b.readInt() & 0xFFFFFFFFL;

        return EmissionCheckData.builder()
                .timestamp(ts)
                .checkType(checkType)
                .noxPemsValue(noxRaw != INV_WORD ? noxRaw * NOX_SCALE + NOX_OFFSET : -1)
                .coPemsValue(coRaw != INV_WORD ? coRaw * CO_SCALE : -1)
                .pmPemsValue(pmRaw != INV_WORD ? pmRaw * PM_SCALE : -1)
                .exhaustFlow(exfRaw != INV_WORD ? exfRaw * EXHAUST_SCALE : -1)
                .engineSpeed(rpmRaw != INV_WORD ? rpmRaw * ENG_SCALE : -1)
                .engineTorquePercent(tqRaw != INV_BYTE ? tqRaw * TORQUE_SCALE + TORQUE_OFFSET : Double.NaN)
                .engineCoolantTemp(coolRaw != INV_BYTE ? coolRaw * COOLANT_SCALE + COOLANT_OFFSET : Double.NaN)
                .positionStatus(posStat)
                .longitude(lonRaw != INV_DWORD ? lonRaw * DMS_SCALE : -1)
                .latitude(latRaw != INV_DWORD ? latRaw * DMS_SCALE : -1)
                .odometer(odoRaw != INV_DWORD ? odoRaw * ODO_SCALE : -1)
                .build();
    }

    public static boolean isUpload(DataPacket pkt) {
        return pkt.getResponseFlag() == 0xFE;
    }

    public static boolean isResponse(DataPacket pkt) {
        return pkt.getResponseFlag() != 0xFE;
    }

    public static DataType getCommandType(DataPacket pkt) {
        return DataType.fromCode(pkt.getCommandId());
    }
}
