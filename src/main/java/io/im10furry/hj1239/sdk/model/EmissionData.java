package io.im10furry.hj1239.sdk.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * HJ 1239.3-2021 Table 5 — DPF/SCR diesel engine real-time emission data.
 *
 * <p>All fields use sentinel values (0xFF / 0xFFFF / 0xFFFFFFFF) to indicate
 * invalid/unavailable readings per the standard. Fields with sentinel value -1
 * or NaN indicate unavailable sensor data.</p>
 */
public class EmissionData {

    private final LocalDateTime timestamp;
    private final double vehicleSpeed;
    private final double intakePressure;
    private final double engineTorquePercent;
    private final double frictionTorquePercent;
    private final double engineSpeed;
    private final double fuelConsumptionRate;
    private final double scrUpstreamNox;
    private final double scrDownstreamNox;
    private final double reagentRemaining;
    private final double exhaustFlow;
    private final double scrInletTemp;
    private final double scrOutletTemp;
    private final double dpfDifferentialPressure;
    private final double engineCoolantTemp;
    private final double reagentLevel;
    private final int positionStatus;
    private final double longitude;
    private final double latitude;
    private final double odometer;

    private EmissionData(Builder b) {
        this.timestamp = b.timestamp;
        this.vehicleSpeed = b.vehicleSpeed;
        this.intakePressure = b.intakePressure;
        this.engineTorquePercent = b.engineTorquePercent;
        this.frictionTorquePercent = b.frictionTorquePercent;
        this.engineSpeed = b.engineSpeed;
        this.fuelConsumptionRate = b.fuelConsumptionRate;
        this.scrUpstreamNox = b.scrUpstreamNox;
        this.scrDownstreamNox = b.scrDownstreamNox;
        this.reagentRemaining = b.reagentRemaining;
        this.exhaustFlow = b.exhaustFlow;
        this.scrInletTemp = b.scrInletTemp;
        this.scrOutletTemp = b.scrOutletTemp;
        this.dpfDifferentialPressure = b.dpfDifferentialPressure;
        this.engineCoolantTemp = b.engineCoolantTemp;
        this.reagentLevel = b.reagentLevel;
        this.positionStatus = b.positionStatus;
        this.longitude = b.longitude;
        this.latitude = b.latitude;
        this.odometer = b.odometer;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public double getVehicleSpeed() { return vehicleSpeed; }
    public double getIntakePressure() { return intakePressure; }
    public double getEngineTorquePercent() { return engineTorquePercent; }
    public double getFrictionTorquePercent() { return frictionTorquePercent; }
    public double getEngineSpeed() { return engineSpeed; }
    public double getFuelConsumptionRate() { return fuelConsumptionRate; }
    public double getScrUpstreamNox() { return scrUpstreamNox; }
    public double getScrDownstreamNox() { return scrDownstreamNox; }
    public double getReagentRemaining() { return reagentRemaining; }
    public double getExhaustFlow() { return exhaustFlow; }
    public double getScrInletTemp() { return scrInletTemp; }
    public double getScrOutletTemp() { return scrOutletTemp; }
    public double getDpfDifferentialPressure() { return dpfDifferentialPressure; }
    public double getEngineCoolantTemp() { return engineCoolantTemp; }
    public double getReagentLevel() { return reagentLevel; }
    public int getPositionStatus() { return positionStatus; }
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }
    public double getOdometer() { return odometer; }
    public boolean isGpsValid() { return (positionStatus & 0x01) != 0; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmissionData that)) return false;
        return positionStatus == that.positionStatus
                && Double.compare(that.vehicleSpeed, vehicleSpeed) == 0
                && Double.compare(that.intakePressure, intakePressure) == 0
                && Double.compare(that.engineTorquePercent, engineTorquePercent) == 0
                && Double.compare(that.frictionTorquePercent, frictionTorquePercent) == 0
                && Double.compare(that.engineSpeed, engineSpeed) == 0
                && Double.compare(that.fuelConsumptionRate, fuelConsumptionRate) == 0
                && Double.compare(that.scrUpstreamNox, scrUpstreamNox) == 0
                && Double.compare(that.scrDownstreamNox, scrDownstreamNox) == 0
                && Double.compare(that.reagentRemaining, reagentRemaining) == 0
                && Double.compare(that.exhaustFlow, exhaustFlow) == 0
                && Double.compare(that.scrInletTemp, scrInletTemp) == 0
                && Double.compare(that.scrOutletTemp, scrOutletTemp) == 0
                && Double.compare(that.dpfDifferentialPressure, dpfDifferentialPressure) == 0
                && Double.compare(that.engineCoolantTemp, engineCoolantTemp) == 0
                && Double.compare(that.reagentLevel, reagentLevel) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.odometer, odometer) == 0
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, vehicleSpeed, intakePressure,
                engineTorquePercent, frictionTorquePercent, engineSpeed,
                fuelConsumptionRate, scrUpstreamNox, scrDownstreamNox,
                reagentRemaining, exhaustFlow, scrInletTemp, scrOutletTemp,
                dpfDifferentialPressure, engineCoolantTemp, reagentLevel,
                positionStatus, longitude, latitude, odometer);
    }

    @Override
    public String toString() {
        return "EmissionData{ts=" + timestamp + ", spd=" + vehicleSpeed
                + ", rpm=" + engineSpeed + ", nox=" + scrUpstreamNox + "}";
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private LocalDateTime timestamp;
        private double vehicleSpeed = -1;
        private double intakePressure = -1;
        private double engineTorquePercent = Double.NaN;
        private double frictionTorquePercent = Double.NaN;
        private double engineSpeed = -1;
        private double fuelConsumptionRate = -1;
        private double scrUpstreamNox = -1;
        private double scrDownstreamNox = -1;
        private double reagentRemaining = -1;
        private double exhaustFlow = -1;
        private double scrInletTemp = Double.NaN;
        private double scrOutletTemp = Double.NaN;
        private double dpfDifferentialPressure = -1;
        private double engineCoolantTemp = Double.NaN;
        private double reagentLevel = -1;
        private int positionStatus;
        private double longitude = -1;
        private double latitude = -1;
        private double odometer = -1;

        public Builder timestamp(LocalDateTime v) { this.timestamp = v; return this; }
        public Builder vehicleSpeed(double v) { this.vehicleSpeed = v; return this; }
        public Builder intakePressure(double v) { this.intakePressure = v; return this; }
        public Builder engineTorquePercent(double v) { this.engineTorquePercent = v; return this; }
        public Builder frictionTorquePercent(double v) { this.frictionTorquePercent = v; return this; }
        public Builder engineSpeed(double v) { this.engineSpeed = v; return this; }
        public Builder fuelConsumptionRate(double v) { this.fuelConsumptionRate = v; return this; }
        public Builder scrUpstreamNox(double v) { this.scrUpstreamNox = v; return this; }
        public Builder scrDownstreamNox(double v) { this.scrDownstreamNox = v; return this; }
        public Builder reagentRemaining(double v) { this.reagentRemaining = v; return this; }
        public Builder exhaustFlow(double v) { this.exhaustFlow = v; return this; }
        public Builder scrInletTemp(double v) { this.scrInletTemp = v; return this; }
        public Builder scrOutletTemp(double v) { this.scrOutletTemp = v; return this; }
        public Builder dpfDifferentialPressure(double v) { this.dpfDifferentialPressure = v; return this; }
        public Builder engineCoolantTemp(double v) { this.engineCoolantTemp = v; return this; }
        public Builder reagentLevel(double v) { this.reagentLevel = v; return this; }
        public Builder positionStatus(int v) { this.positionStatus = v; return this; }
        public Builder longitude(double v) { this.longitude = v; return this; }
        public Builder latitude(double v) { this.latitude = v; return this; }
        public Builder odometer(double v) { this.odometer = v; return this; }

        public EmissionData build() {
            Objects.requireNonNull(timestamp, "timestamp must not be null");
            return new EmissionData(this);
        }
    }
}
