package io.darkinno-tech-tech.hj1239.sdk.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class HybridData {

    private final LocalDateTime timestamp;
    private final double vehicleSpeed;
    private final double engineSpeed;
    private final double motorSpeed;
    private final double motorTorque;
    private final double batterySoc;
    private final double batteryVoltage;
    private final double batteryCurrent;
    private final double fuelConsumptionRate;
    private final double engineCoolantTemp;
    private final int hybridMode;
    private final int positionStatus;
    private final double longitude;
    private final double latitude;
    private final double odometer;

    private HybridData(Builder b) {
        this.timestamp = b.timestamp;
        this.vehicleSpeed = b.vehicleSpeed;
        this.engineSpeed = b.engineSpeed;
        this.motorSpeed = b.motorSpeed;
        this.motorTorque = b.motorTorque;
        this.batterySoc = b.batterySoc;
        this.batteryVoltage = b.batteryVoltage;
        this.batteryCurrent = b.batteryCurrent;
        this.fuelConsumptionRate = b.fuelConsumptionRate;
        this.engineCoolantTemp = b.engineCoolantTemp;
        this.hybridMode = b.hybridMode;
        this.positionStatus = b.positionStatus;
        this.longitude = b.longitude;
        this.latitude = b.latitude;
        this.odometer = b.odometer;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public double getVehicleSpeed() { return vehicleSpeed; }
    public double getEngineSpeed() { return engineSpeed; }
    public double getMotorSpeed() { return motorSpeed; }
    public double getMotorTorque() { return motorTorque; }
    public double getBatterySoc() { return batterySoc; }
    public double getBatteryVoltage() { return batteryVoltage; }
    public double getBatteryCurrent() { return batteryCurrent; }
    public double getFuelConsumptionRate() { return fuelConsumptionRate; }
    public double getEngineCoolantTemp() { return engineCoolantTemp; }
    public int getHybridMode() { return hybridMode; }
    public int getPositionStatus() { return positionStatus; }
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }
    public double getOdometer() { return odometer; }
    public boolean isGpsValid() { return (positionStatus & 0x01) != 0; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HybridData that)) return false;
        return Double.compare(that.vehicleSpeed, vehicleSpeed) == 0
                && Double.compare(that.engineSpeed, engineSpeed) == 0
                && Double.compare(that.motorSpeed, motorSpeed) == 0
                && Double.compare(that.motorTorque, motorTorque) == 0
                && Double.compare(that.batterySoc, batterySoc) == 0
                && Double.compare(that.batteryVoltage, batteryVoltage) == 0
                && Double.compare(that.batteryCurrent, batteryCurrent) == 0
                && Double.compare(that.fuelConsumptionRate, fuelConsumptionRate) == 0
                && Double.compare(that.engineCoolantTemp, engineCoolantTemp) == 0
                && hybridMode == that.hybridMode
                && positionStatus == that.positionStatus
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.odometer, odometer) == 0
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, vehicleSpeed, engineSpeed,
                motorSpeed, motorTorque, batterySoc, batteryVoltage,
                batteryCurrent, fuelConsumptionRate, engineCoolantTemp,
                hybridMode, positionStatus, longitude, latitude, odometer);
    }

    @Override
    public String toString() {
        return "HybridData{ts=" + timestamp + ", spd=" + vehicleSpeed
                + ", soc=" + batterySoc + "%, mode=" + hybridMode + "}";
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private LocalDateTime timestamp;
        private double vehicleSpeed = -1;
        private double engineSpeed = -1;
        private double motorSpeed = -1;
        private double motorTorque = Double.NaN;
        private double batterySoc = -1;
        private double batteryVoltage = -1;
        private double batteryCurrent = Double.NaN;
        private double fuelConsumptionRate = -1;
        private double engineCoolantTemp = Double.NaN;
        private int hybridMode;
        private int positionStatus;
        private double longitude = -1;
        private double latitude = -1;
        private double odometer = -1;

        public Builder timestamp(LocalDateTime v) { this.timestamp = v; return this; }
        public Builder vehicleSpeed(double v) { this.vehicleSpeed = v; return this; }
        public Builder engineSpeed(double v) { this.engineSpeed = v; return this; }
        public Builder motorSpeed(double v) { this.motorSpeed = v; return this; }
        public Builder motorTorque(double v) { this.motorTorque = v; return this; }
        public Builder batterySoc(double v) { this.batterySoc = v; return this; }
        public Builder batteryVoltage(double v) { this.batteryVoltage = v; return this; }
        public Builder batteryCurrent(double v) { this.batteryCurrent = v; return this; }
        public Builder fuelConsumptionRate(double v) { this.fuelConsumptionRate = v; return this; }
        public Builder engineCoolantTemp(double v) { this.engineCoolantTemp = v; return this; }
        public Builder hybridMode(int v) { this.hybridMode = v; return this; }
        public Builder positionStatus(int v) { this.positionStatus = v; return this; }
        public Builder longitude(double v) { this.longitude = v; return this; }
        public Builder latitude(double v) { this.latitude = v; return this; }
        public Builder odometer(double v) { this.odometer = v; return this; }

        public HybridData build() {
            Objects.requireNonNull(timestamp, "timestamp must not be null");
            return new HybridData(this);
        }
    }
}
