package io.darkinno-tech-tech.hj1239.sdk.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * HJ 1239.3-2021 Table 16 — Data packet structure.
 *
 * <pre>
 *   start(2) | cmd(1) | resp(1) | vin(17) | encrypt(1) | len(2) | data(N) | bcc(1)
 * </pre>
 */
public class DataPacket {

    public static final byte[] START_MARKER = {(byte) 0x7E, (byte) 0x7E};

    private final int commandId;
    private final int responseFlag;
    private final String vehicleId;
    private final int encryptionMode;
    private final byte[] dataUnit;

    private DataPacket(Builder builder) {
        this.commandId = builder.commandId;
        this.responseFlag = builder.responseFlag;
        this.vehicleId = builder.vehicleId;
        this.encryptionMode = builder.encryptionMode;
        this.dataUnit = builder.dataUnit;
    }

    public int getCommandId() { return commandId; }
    public int getResponseFlag() { return responseFlag; }
    public String getVehicleId() { return vehicleId; }
    public int getEncryptionMode() { return encryptionMode; }
    public byte[] getDataUnit() { return dataUnit != null ? dataUnit.clone() : new byte[0]; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataPacket that)) return false;
        return commandId == that.commandId
                && responseFlag == that.responseFlag
                && encryptionMode == that.encryptionMode
                && Objects.equals(vehicleId, that.vehicleId)
                && Arrays.equals(dataUnit, that.dataUnit);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(commandId, responseFlag, vehicleId, encryptionMode);
        result = 31 * result + Arrays.hashCode(dataUnit);
        return result;
    }

    @Override
    public String toString() {
        return "DataPacket{cmd=0x" + Integer.toHexString(commandId)
                + ", vin='" + vehicleId + "', dataLen="
                + (dataUnit != null ? dataUnit.length : 0) + "}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int commandId;
        private int responseFlag = 0xFE;
        private String vehicleId;
        private int encryptionMode = 0x01;
        private byte[] dataUnit;

        public Builder commandId(int v) { this.commandId = v; return this; }
        public Builder responseFlag(int v) { this.responseFlag = v; return this; }
        public Builder vehicleId(String v) { this.vehicleId = v; return this; }
        public Builder encryptionMode(int v) { this.encryptionMode = v; return this; }
        public Builder dataUnit(byte[] v) { this.dataUnit = v != null ? v.clone() : null; return this; }

        public DataPacket build() {
            Objects.requireNonNull(vehicleId, "vehicleId must not be null");
            return new DataPacket(this);
        }
    }
}
