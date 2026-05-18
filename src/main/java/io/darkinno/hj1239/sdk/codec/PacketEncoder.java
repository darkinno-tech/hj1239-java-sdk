package io.darkinno.hj1239.sdk.codec;

import io.darkinno.hj1239.sdk.model.*;
import io.darkinno.hj1239.sdk.model.enums.DataType;
import io.darkinno.hj1239.sdk.model.enums.MessageType;
import io.darkinno.hj1239.sdk.util.CrcUtil;
import io.darkinno.hj1239.sdk.util.TimeUtil;

import java.time.LocalDateTime;

/**
 * HJ 1239.3-2021 packet encoder — Enterprise Platform protocol (Section 5).
 *
 * <p>Packet structure (Table 16):
 * <pre>
 *   start(2) | cmd(1) | resp(1) | vin(17) | encrypt(1) | len(2) | data(N) | bcc(1)
 * </pre>
 * BCC = XOR from cmd byte to last data byte (excludes start marker).
 * </p>
 */
public final class PacketEncoder {

    private static final int VIN_LEN = 17;
    private static final int HEADER_LEN = 2 + 1 + 1 + VIN_LEN + 1 + 2;
    private static final int BUF_SIZE = 2048;

    private PacketEncoder() {}

    // ── protocol constants (Table 5) ──

    static final double SPD_SCALE = 256.0;          // 1/256 km/h
    static final double INTAKE_P_SCALE = 0.5;       // 0.5 kPa
    static final double TORQUE_SCALE = 1.0;         // 1%/bit, offset -125%
    static final double TORQUE_OFFSET = -125;
    static final double ENG_SCALE = 0.125;          // 0.125 rpm
    static final double FUEL_SCALE = 0.05;          // 0.05 L/h
    static final double NOX_SCALE = 0.05;           // 0.05 ppm, offset -200
    static final double NOX_OFFSET = -200;
    static final double REAGENT_SCALE = 0.4;        // 0.4%
    static final double EXHAUST_SCALE = 0.05;       // 0.05 kg/h
    static final double SCR_TEMP_SCALE = 0.03125;   // 0.03125 degC, offset -273
    static final double SCR_TEMP_OFFSET = -273;
    static final double DPF_SCALE = 0.1;            // 0.1 kPa
    static final double COOLANT_SCALE = 1.0;        // 1 degC, offset -40
    static final double COOLANT_OFFSET = -40;
    static final double DMS_SCALE = 0.000001;       // 1e-6 deg
    static final double ODO_SCALE = 0.1;            // 0.1 km

    static final int INV_BYTE = 0xFF;
    static final int INV_WORD = 0xFFFF;
    static final long INV_DWORD = 0xFFFFFFFFL;

    static final double EGR_SCALE = 0.5;
    static final double SOOT_SCALE = 0.5;
    static final double ASH_SCALE = 0.1;
    static final double MOTOR_SPEED_SCALE = 0.25;
    static final double MOTOR_TORQUE_SCALE = 0.5;
    static final double MOTOR_TORQUE_OFFSET = -2000;
    static final double SOC_SCALE = 0.5;
    static final double VOLTAGE_SCALE = 0.1;
    static final double CURRENT_SCALE = 0.1;
    static final double CURRENT_OFFSET = -1000;
    static final double CO_SCALE = 0.01;
    static final double PM_SCALE = 0.01;

    // ── public API ──

    public static byte[] encode(DataPacket pkt) {
        if (pkt == null) throw new IllegalArgumentException("packet must not be null");
        return build(pkt.getCommandId(), pkt.getResponseFlag(),
                pkt.getEncryptionMode(), pkt.getVehicleId(), pkt.getDataUnit());
    }

    public static byte[] encodeVehicleLogin(VehicleInfo vi, int seq) {
        if (vi == null) throw new IllegalArgumentException("vehicleInfo must not be null");
        validateSeq(seq);
        ByteBuf du = new ByteBuf(256);
        du.writeBytes(TimeUtil.encode(LocalDateTime.now()));
        du.writeShort((short) seq);
        du.writeString(vi.getVin(), 17);
        du.writeByte((byte) vi.getFuelType().getCode());
        du.writeByte((byte) vi.getEmissionStandard().getCode());
        du.writeString(vi.getPlateNumber() != null ? vi.getPlateNumber() : "", 12);
        du.writeString(vi.getPlateColor() != null ? vi.getPlateColor() : "", 8);
        du.writeString(vi.getManufacturer() != null ? vi.getManufacturer() : "", 32);
        du.writeString(vi.getModel() != null ? vi.getModel() : "", 32);
        du.writeShort((short) vi.getModelYear());

        return build(DataType.VEHICLE_LOGIN.getCode(), 0xFE, 0x01, vi.getVin(), du.toByteArray());
    }

    public static byte[] encodeRealtimeData(EmissionData em, String vin, int seq) {
        if (em == null) throw new IllegalArgumentException("emission must not be null");
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        validateSeq(seq);
        ByteBuf du = new ByteBuf(512);

        byte[] ts = TimeUtil.encode(em.getTimestamp());
        du.writeBytes(ts);
        du.writeShort((short) seq);
        du.writeByte((byte) MessageType.ENGINE_DPF_SCR_DATA.getCode());
        du.writeBytes(ts);

        encodeTable5Body(du, em);

        return build(DataType.REALTIME_DATA.getCode(), 0xFE, 0x01, vin, du.toByteArray());
    }

    public static byte[] encodeHeartbeat(String vin, int seq) {
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        validateSeq(seq);
        ByteBuf du = new ByteBuf(16);
        du.writeBytes(TimeUtil.encode(LocalDateTime.now()));
        du.writeShort((short) seq);
        return build(DataType.REALTIME_DATA.getCode(), 0xFE, 0x01, vin, du.toByteArray());
    }

    // ── Table 5 body: DPF+SCR diesel engine (37 bytes) ──

    public static byte[] encodeVehicleLogout(String vin, int seq) {
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        validateSeq(seq);
        ByteBuf du = new ByteBuf(16);
        du.writeBytes(TimeUtil.encode(LocalDateTime.now()));
        du.writeShort((short) seq);
        return build(DataType.VEHICLE_LOGOUT.getCode(), 0xFE, 0x01, vin, du.toByteArray());
    }

    public static byte[] encodeComplementData(EmissionData em, String vin, int seq) {
        if (em == null) throw new IllegalArgumentException("emission must not be null");
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        validateSeq(seq);
        ByteBuf du = new ByteBuf(512);
        byte[] ts = TimeUtil.encode(em.getTimestamp());
        du.writeBytes(ts);
        du.writeShort((short) seq);
        du.writeByte((byte) MessageType.ENGINE_DPF_SCR_DATA.getCode());
        du.writeBytes(ts);
        encodeTable5Body(du, em);
        return build(DataType.REPLENISH_DATA.getCode(), 0xFE, 0x01, vin, du.toByteArray());
    }

    public static byte[] encodeTimeSync(String vin, int seq) {
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        validateSeq(seq);
        ByteBuf du = new ByteBuf(16);
        du.writeBytes(TimeUtil.encode(LocalDateTime.now()));
        du.writeShort((short) seq);
        return build(DataType.TERMINAL_TIME_SYNC.getCode(), 0xFE, 0x01, vin, du.toByteArray());
    }

    public static byte[] encodePlatformLogin(String platformId, String username, String password, int seq) {
        if (platformId == null) throw new IllegalArgumentException("platformId must not be null");
        validateSeq(seq);
        ByteBuf du = new ByteBuf(128);
        du.writeBytes(TimeUtil.encode(LocalDateTime.now()));
        du.writeShort((short) seq);
        du.writeString(platformId, 12);
        du.writeString(username != null ? username : "", 16);
        du.writeString(password != null ? password : "", 16);
        return build(DataType.PLATFORM_LOGIN.getCode(), 0xFE, 0x01, platformId, du.toByteArray());
    }

    public static byte[] encodePlatformLogout(String platformId, int seq) {
        if (platformId == null) throw new IllegalArgumentException("platformId must not be null");
        validateSeq(seq);
        ByteBuf du = new ByteBuf(32);
        du.writeBytes(TimeUtil.encode(LocalDateTime.now()));
        du.writeShort((short) seq);
        return build(DataType.PLATFORM_LOGOUT.getCode(), 0xFE, 0x01, platformId, du.toByteArray());
    }

    public static byte[] encodeObdEngineData(ObdEngineData obd, String vin, int seq) {
        if (obd == null) throw new IllegalArgumentException("obd must not be null");
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        validateSeq(seq);
        ByteBuf du = new ByteBuf(256);
        byte[] ts = TimeUtil.encode(obd.getTimestamp());
        du.writeBytes(ts);
        du.writeShort((short) seq);
        du.writeByte((byte) MessageType.OBD_ENGINE_DATA.getCode());
        du.writeBytes(ts);
        encodeObdBody(du, obd);
        return build(DataType.REALTIME_DATA.getCode(), 0xFE, 0x01, vin, du.toByteArray());
    }

    public static byte[] encodeHybridData(HybridData hd, String vin, int seq) {
        if (hd == null) throw new IllegalArgumentException("hybrid data must not be null");
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        validateSeq(seq);
        ByteBuf du = new ByteBuf(256);
        byte[] ts = TimeUtil.encode(hd.getTimestamp());
        du.writeBytes(ts);
        du.writeShort((short) seq);
        du.writeByte((byte) MessageType.ENGINE_HYBRID_DATA.getCode());
        du.writeBytes(ts);
        encodeHybridBody(du, hd);
        return build(DataType.REALTIME_DATA.getCode(), 0xFE, 0x01, vin, du.toByteArray());
    }

    public static byte[] encodeEmissionCheck(EmissionCheckData ec, String vin, int seq) {
        if (ec == null) throw new IllegalArgumentException("emission check data must not be null");
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        validateSeq(seq);
        ByteBuf du = new ByteBuf(256);
        byte[] ts = TimeUtil.encode(ec.getTimestamp());
        du.writeBytes(ts);
        du.writeShort((short) seq);
        du.writeByte((byte) MessageType.EMISSION_CHECK_DATA.getCode());
        du.writeBytes(ts);
        encodeEmissionCheckBody(du, ec);
        return build(DataType.EMISSION_CHECK.getCode(), 0xFE, 0x01, vin, du.toByteArray());
    }

    private static void encodeTable5Body(ByteBuf b, EmissionData e) {
        b.writeShort(e.getVehicleSpeed() >= 0
                ? (short) (e.getVehicleSpeed() * SPD_SCALE) : (short) INV_WORD);
        b.writeByte(e.getIntakePressure() >= 0
                ? (byte) (e.getIntakePressure() / INTAKE_P_SCALE) : (byte) INV_BYTE);
        b.writeByte(!Double.isNaN(e.getEngineTorquePercent())
                ? (byte) ((e.getEngineTorquePercent() - TORQUE_OFFSET) / TORQUE_SCALE) : (byte) INV_BYTE);
        b.writeByte(!Double.isNaN(e.getFrictionTorquePercent())
                ? (byte) ((e.getFrictionTorquePercent() - TORQUE_OFFSET) / TORQUE_SCALE) : (byte) INV_BYTE);
        b.writeShort(e.getEngineSpeed() >= 0
                ? (short) (e.getEngineSpeed() / ENG_SCALE) : (short) INV_WORD);
        b.writeShort(e.getFuelConsumptionRate() >= 0
                ? (short) (e.getFuelConsumptionRate() / FUEL_SCALE) : (short) INV_WORD);
        b.writeShort(e.getScrUpstreamNox() >= 0
                ? (short) ((e.getScrUpstreamNox() - NOX_OFFSET) / NOX_SCALE) : (short) INV_WORD);
        b.writeShort(e.getScrDownstreamNox() >= 0
                ? (short) ((e.getScrDownstreamNox() - NOX_OFFSET) / NOX_SCALE) : (short) INV_WORD);
        b.writeByte(e.getReagentRemaining() >= 0
                ? (byte) (e.getReagentRemaining() / REAGENT_SCALE) : (byte) INV_BYTE);
        b.writeShort(e.getExhaustFlow() >= 0
                ? (short) (e.getExhaustFlow() / EXHAUST_SCALE) : (short) INV_WORD);
        b.writeShort(!Double.isNaN(e.getScrInletTemp())
                ? (short) ((e.getScrInletTemp() - SCR_TEMP_OFFSET) / SCR_TEMP_SCALE) : (short) INV_WORD);
        b.writeShort(!Double.isNaN(e.getScrOutletTemp())
                ? (short) ((e.getScrOutletTemp() - SCR_TEMP_OFFSET) / SCR_TEMP_SCALE) : (short) INV_WORD);
        b.writeShort(e.getDpfDifferentialPressure() >= 0
                ? (short) (e.getDpfDifferentialPressure() / DPF_SCALE) : (short) INV_WORD);
        b.writeByte(!Double.isNaN(e.getEngineCoolantTemp())
                ? (byte) ((e.getEngineCoolantTemp() - COOLANT_OFFSET) / COOLANT_SCALE) : (byte) INV_BYTE);
        b.writeByte(e.getReagentLevel() >= 0
                ? (byte) (e.getReagentLevel() / REAGENT_SCALE) : (byte) INV_BYTE);
        b.writeByte((byte) e.getPositionStatus());
        b.writeInt(e.getLongitude() >= 0
                ? (int) (e.getLongitude() / DMS_SCALE) : (int) INV_DWORD);
        b.writeInt(e.getLatitude() >= 0
                ? (int) (e.getLatitude() / DMS_SCALE) : (int) INV_DWORD);
        b.writeInt(e.getOdometer() >= 0
                ? (int) (e.getOdometer() / ODO_SCALE) : (int) INV_DWORD);
    }

    private static void encodeObdBody(ByteBuf b, ObdEngineData d) {
        b.writeByte((byte) (d.isMilOn() ? 0x01 : 0x00));
        b.writeByte((byte) d.getDtcCount());
        b.writeByte(d.getEgrErrorRate() >= 0 ? (byte) (d.getEgrErrorRate() / EGR_SCALE) : (byte) INV_BYTE);
        b.writeByte((byte) d.getScrSystemStatus());
        b.writeByte((byte) d.getDpfSystemStatus());
        b.writeByte(d.getDpfSootLoad() >= 0 ? (byte) (d.getDpfSootLoad() / SOOT_SCALE) : (byte) INV_BYTE);
        b.writeByte(d.getDpfAshLoad() >= 0 ? (byte) (d.getDpfAshLoad() / ASH_SCALE) : (byte) INV_BYTE);
        b.writeInt((int) d.getEngineRuntime());
        b.writeByte((byte) d.getPositionStatus());
        b.writeInt(d.getLongitude() >= 0 ? (int) (d.getLongitude() / DMS_SCALE) : (int) INV_DWORD);
        b.writeInt(d.getLatitude() >= 0 ? (int) (d.getLatitude() / DMS_SCALE) : (int) INV_DWORD);
        b.writeInt(d.getOdometer() >= 0 ? (int) (d.getOdometer() / ODO_SCALE) : (int) INV_DWORD);
    }

    private static void encodeHybridBody(ByteBuf b, HybridData d) {
        b.writeShort(d.getVehicleSpeed() >= 0 ? (short) (d.getVehicleSpeed() * SPD_SCALE) : (short) INV_WORD);
        b.writeShort(d.getEngineSpeed() >= 0 ? (short) (d.getEngineSpeed() / ENG_SCALE) : (short) INV_WORD);
        b.writeShort(d.getMotorSpeed() >= 0 ? (short) (d.getMotorSpeed() / MOTOR_SPEED_SCALE) : (short) INV_WORD);
        b.writeShort(!Double.isNaN(d.getMotorTorque()) ? (short) ((d.getMotorTorque() - MOTOR_TORQUE_OFFSET) / MOTOR_TORQUE_SCALE) : (short) INV_WORD);
        b.writeByte(d.getBatterySoc() >= 0 ? (byte) (d.getBatterySoc() / SOC_SCALE) : (byte) INV_BYTE);
        b.writeShort(d.getBatteryVoltage() >= 0 ? (short) (d.getBatteryVoltage() / VOLTAGE_SCALE) : (short) INV_WORD);
        b.writeShort(!Double.isNaN(d.getBatteryCurrent()) ? (short) ((d.getBatteryCurrent() - CURRENT_OFFSET) / CURRENT_SCALE) : (short) INV_WORD);
        b.writeShort(d.getFuelConsumptionRate() >= 0 ? (short) (d.getFuelConsumptionRate() / FUEL_SCALE) : (short) INV_WORD);
        b.writeByte(!Double.isNaN(d.getEngineCoolantTemp()) ? (byte) ((d.getEngineCoolantTemp() - COOLANT_OFFSET) / COOLANT_SCALE) : (byte) INV_BYTE);
        b.writeByte((byte) d.getHybridMode());
        b.writeByte((byte) d.getPositionStatus());
        b.writeInt(d.getLongitude() >= 0 ? (int) (d.getLongitude() / DMS_SCALE) : (int) INV_DWORD);
        b.writeInt(d.getLatitude() >= 0 ? (int) (d.getLatitude() / DMS_SCALE) : (int) INV_DWORD);
        b.writeInt(d.getOdometer() >= 0 ? (int) (d.getOdometer() / ODO_SCALE) : (int) INV_DWORD);
    }

    private static void encodeEmissionCheckBody(ByteBuf b, EmissionCheckData d) {
        b.writeByte((byte) d.getCheckType());
        b.writeShort(d.getNoxPemsValue() >= 0 ? (short) ((d.getNoxPemsValue() - NOX_OFFSET) / NOX_SCALE) : (short) INV_WORD);
        b.writeShort(d.getCoPemsValue() >= 0 ? (short) (d.getCoPemsValue() / CO_SCALE) : (short) INV_WORD);
        b.writeShort(d.getPmPemsValue() >= 0 ? (short) (d.getPmPemsValue() / PM_SCALE) : (short) INV_WORD);
        b.writeShort(d.getExhaustFlow() >= 0 ? (short) (d.getExhaustFlow() / EXHAUST_SCALE) : (short) INV_WORD);
        b.writeShort(d.getEngineSpeed() >= 0 ? (short) (d.getEngineSpeed() / ENG_SCALE) : (short) INV_WORD);
        b.writeByte(!Double.isNaN(d.getEngineTorquePercent()) ? (byte) ((d.getEngineTorquePercent() - TORQUE_OFFSET) / TORQUE_SCALE) : (byte) INV_BYTE);
        b.writeByte(!Double.isNaN(d.getEngineCoolantTemp()) ? (byte) ((d.getEngineCoolantTemp() - COOLANT_OFFSET) / COOLANT_SCALE) : (byte) INV_BYTE);
        b.writeByte((byte) d.getPositionStatus());
        b.writeInt(d.getLongitude() >= 0 ? (int) (d.getLongitude() / DMS_SCALE) : (int) INV_DWORD);
        b.writeInt(d.getLatitude() >= 0 ? (int) (d.getLatitude() / DMS_SCALE) : (int) INV_DWORD);
        b.writeInt(d.getOdometer() >= 0 ? (int) (d.getOdometer() / ODO_SCALE) : (int) INV_DWORD);
    }

    private static byte[] build(int cmd, int resp, int encrypt, String vin, byte[] du) {
        ByteBuf buf = new ByteBuf(BUF_SIZE);
        buf.writeBytes(DataPacket.START_MARKER);
        buf.writeByte((byte) cmd);
        buf.writeByte((byte) resp);
        buf.writeString(vin, VIN_LEN);
        buf.writeByte((byte) encrypt);
        buf.writeShort((short) du.length);
        buf.writeBytes(du);
        byte[] arr = buf.toByteArray();
        byte bcc = CrcUtil.xorChecksum(arr, 2, arr.length - 2);
        buf.writeByte(bcc);
        return buf.toByteArray();
    }

    private static void validateSeq(int seq) {
        if (seq < 0 || seq > 65535) {
            throw new IllegalArgumentException("seq must be 0-65535, got: " + seq);
        }
    }
}
