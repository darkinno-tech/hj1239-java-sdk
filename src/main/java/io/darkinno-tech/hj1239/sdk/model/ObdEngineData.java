package io.darkinno-tech-tech.hj1239.sdk.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class ObdEngineData {

    private final LocalDateTime timestamp;
    private final boolean milOn;
    private final int dtcCount;
    private final double egrErrorRate;
    private final int scrSystemStatus;
    private final int dpfSystemStatus;
    private final double dpfSootLoad;
    private final double dpfAshLoad;
    private final long engineRuntime;
    private final int positionStatus;
    private final double longitude;
    private final double latitude;
    private final double odometer;

    private ObdEngineData(Builder b) {
        this.timestamp = b.timestamp;
        this.milOn = b.milOn;
        this.dtcCount = b.dtcCount;
        this.egrErrorRate = b.egrErrorRate;
        this.scrSystemStatus = b.scrSystemStatus;
        this.dpfSystemStatus = b.dpfSystemStatus;
        this.dpfSootLoad = b.dpfSootLoad;
        this.dpfAshLoad = b.dpfAshLoad;
        this.engineRuntime = b.engineRuntime;
        this.positionStatus = b.positionStatus;
        this.longitude = b.longitude;
        this.latitude = b.latitude;
        this.odometer = b.odometer;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isMilOn() { return milOn; }
    public int getDtcCount() { return dtcCount; }
    public double getEgrErrorRate() { return egrErrorRate; }
    public int getScrSystemStatus() { return scrSystemStatus; }
    public int getDpfSystemStatus() { return dpfSystemStatus; }
    public double getDpfSootLoad() { return dpfSootLoad; }
    public double getDpfAshLoad() { return dpfAshLoad; }
    public long getEngineRuntime() { return engineRuntime; }
    public int getPositionStatus() { return positionStatus; }
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }
    public double getOdometer() { return odometer; }
    public boolean isGpsValid() { return (positionStatus & 0x01) != 0; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObdEngineData that)) return false;
        return milOn == that.milOn
                && dtcCount == that.dtcCount
                && Double.compare(that.egrErrorRate, egrErrorRate) == 0
                && scrSystemStatus == that.scrSystemStatus
                && dpfSystemStatus == that.dpfSystemStatus
                && Double.compare(that.dpfSootLoad, dpfSootLoad) == 0
                && Double.compare(that.dpfAshLoad, dpfAshLoad) == 0
                && engineRuntime == that.engineRuntime
                && positionStatus == that.positionStatus
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.odometer, odometer) == 0
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, milOn, dtcCount, egrErrorRate,
                scrSystemStatus, dpfSystemStatus, dpfSootLoad, dpfAshLoad,
                engineRuntime, positionStatus, longitude, latitude, odometer);
    }

    @Override
    public String toString() {
        return "ObdEngineData{ts=" + timestamp + ", mil=" + milOn
                + ", dtc=" + dtcCount + ", egr=" + egrErrorRate + "}";
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private LocalDateTime timestamp;
        private boolean milOn;
        private int dtcCount;
        private double egrErrorRate = -1;
        private int scrSystemStatus;
        private int dpfSystemStatus;
        private double dpfSootLoad = -1;
        private double dpfAshLoad = -1;
        private long engineRuntime;
        private int positionStatus;
        private double longitude = -1;
        private double latitude = -1;
        private double odometer = -1;

        public Builder timestamp(LocalDateTime v) { this.timestamp = v; return this; }
        public Builder milOn(boolean v) { this.milOn = v; return this; }
        public Builder dtcCount(int v) { this.dtcCount = v; return this; }
        public Builder egrErrorRate(double v) { this.egrErrorRate = v; return this; }
        public Builder scrSystemStatus(int v) { this.scrSystemStatus = v; return this; }
        public Builder dpfSystemStatus(int v) { this.dpfSystemStatus = v; return this; }
        public Builder dpfSootLoad(double v) { this.dpfSootLoad = v; return this; }
        public Builder dpfAshLoad(double v) { this.dpfAshLoad = v; return this; }
        public Builder engineRuntime(long v) { this.engineRuntime = v; return this; }
        public Builder positionStatus(int v) { this.positionStatus = v; return this; }
        public Builder longitude(double v) { this.longitude = v; return this; }
        public Builder latitude(double v) { this.latitude = v; return this; }
        public Builder odometer(double v) { this.odometer = v; return this; }

        public ObdEngineData build() {
            Objects.requireNonNull(timestamp, "timestamp must not be null");
            return new ObdEngineData(this);
        }
    }
}
