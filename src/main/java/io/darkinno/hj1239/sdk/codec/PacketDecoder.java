package io.darkinno.hj1239.sdk.codec;

import io.darkinno.hj1239.sdk.model.DataPacket;
import io.darkinno.hj1239.sdk.model.EmissionData;
import io.darkinno.hj1239.sdk.model.enums.MessageType;
import io.darkinno.hj1239.sdk.util.CrcUtil;
import io.darkinno.hj1239.sdk.util.TimeUtil;

import java.time.LocalDateTime;

import static io.darkinno.hj1239.sdk.codec.PacketEncoder.*;

/**
 * HJ 1239.3-2021 packet decoder.
 */
public final class PacketDecoder {

    private static final int VIN_LEN = 17;
    private static final int HEADER_LEN = 2 + 1 + 1 + VIN_LEN + 1 + 2;

    private PacketDecoder() {}

    public static DataPacket decode(byte[] raw) {
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
            throw new IllegalArgumentException("BCC mismatch: got 0x"
                    + Integer.toHexString(bcc & 0xFF) + ", calc 0x" + Integer.toHexString(calc & 0xFF));
        }
        return DataPacket.builder().commandId(cmd).responseFlag(resp)
                .vehicleId(vin).encryptionMode(enc).dataUnit(du).build();
    }

    public static EmissionData decodeRealtimeEmission(DataPacket pkt) {
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

        if (msgType != MessageType.ENGINE_DPF_SCR_DATA.getCode()) {
            throw new IllegalArgumentException("Unsupported message type: 0x"
                    + Integer.toHexString(msgType));
        }
        return decodeTable5Body(ts, b);
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
}
