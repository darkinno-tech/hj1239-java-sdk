package io.darkinno.hj1239.sdk.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class EmissionCheckData {

    private final LocalDateTime timestamp;
    private final int checkType;
    private final double noxPemsValue;
    private final double coPemsValue;
    private final double pmPemsValue;
    private final double exhaustFlow;
    private final double engineSpeed;
    private final double engineTorquePercent;
    private final double engineCoolantTemp;
    private final int positionStatus;
    private final double longitude;
    private final double latitude;
    private final double odometer;

    private EmissionCheckData(Builder b) {
        this.timestamp = b.timestamp;
        this.checkType = b.checkType;
        this.noxPemsValue = b.noxPemsValue;
        this.coPemsValue = b.coPemsValue;
        this.pmPemsValue = b.pmPemsValue;
        this.exhaustFlow = b.exhaustFlow;
        this.engineSpeed = b.engineSpeed;
        this.engineTorquePercent = b.engineTorquePercent;
        this.engineCoolantTemp = b.engineCoolantTemp;
        this.positionStatus = b.positionStatus;
        this.longitude = b.longitude;
        this.latitude = b.latitude;
        this.odometer = b.odometer;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getCheckType() { return checkType; }
    public double getNoxPemsValue() { return noxPemsValue; }
    public double getCoPemsValue() { return coPemsValue; }
    public double getPmPemsValue() { return pmPemsValue; }
    public double getExhaustFlow() { return exhaustFlow; }
    public double getEngineSpeed() { return engineSpeed; }
    public double getEngineTorquePercent() { return engineTorquePercent; }
    public double getEngineCoolantTemp() { return engineCoolantTemp; }
    public int getPositionStatus() { return positionStatus; }
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }
    public double getOdometer() { return odometer; }
    public boolean isGpsValid() { return (positionStatus & 0x01) != 0; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmissionCheckData that)) return false;
        return checkType == that.checkType
                && Double.compare(that.noxPemsValue, noxPemsValue) == 0
                && Double.compare(that.coPemsValue, coPemsValue) == 0
                && Double.compare(that.pmPemsValue, pmPemsValue) == 0
                && Double.compare(that.exhaustFlow, exhaustFlow) == 0
                && Double.compare(that.engineSpeed, engineSpeed) == 0
                && Double.compare(that.engineTorquePercent, engineTorquePercent) == 0
                && Double.compare(that.engineCoolantTemp, engineCoolantTemp) == 0
                && positionStatus == that.positionStatus
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.odometer, odometer) == 0
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, checkType, noxPemsValue,
                coPemsValue, pmPemsValue, exhaustFlow, engineSpeed,
                engineTorquePercent, engineCoolantTemp, positionStatus,
                longitude, latitude, odometer);
    }

    @Override
    public String toString() {
        return "EmissionCheckData{ts=" + timestamp + ", type=" + checkType
                + ", nox=" + noxPemsValue + ", co=" + coPemsValue + "}";
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private LocalDateTime timestamp;
        private int checkType;
        private double noxPemsValue = -1;
        private double coPemsValue = -1;
        private double pmPemsValue = -1;
        private double exhaustFlow = -1;
        private double engineSpeed = -1;
        private double engineTorquePercent = Double.NaN;
        private double engineCoolantTemp = Double.NaN;
        private int positionStatus;
        private double longitude = -1;
        private double latitude = -1;
        private double odometer = -1;

        public Builder timestamp(LocalDateTime v) { this.timestamp = v; return this; }
        public Builder checkType(int v) { this.checkType = v; return this; }
        public Builder noxPemsValue(double v) { this.noxPemsValue = v; return this; }
        public Builder coPemsValue(double v) { this.coPemsValue = v; return this; }
        public Builder pmPemsValue(double v) { this.pmPemsValue = v; return this; }
        public Builder exhaustFlow(double v) { this.exhaustFlow = v; return this; }
        public Builder engineSpeed(double v) { this.engineSpeed = v; return this; }
        public Builder engineTorquePercent(double v) { this.engineTorquePercent = v; return this; }
        public Builder engineCoolantTemp(double v) { this.engineCoolantTemp = v; return this; }
        public Builder positionStatus(int v) { this.positionStatus = v; return this; }
        public Builder longitude(double v) { this.longitude = v; return this; }
        public Builder latitude(double v) { this.latitude = v; return this; }
        public Builder odometer(double v) { this.odometer = v; return this; }

        public EmissionCheckData build() {
            Objects.requireNonNull(timestamp, "timestamp must not be null");
            return new EmissionCheckData(this);
        }
    }
}
