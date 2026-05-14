package io.darkinno.hj1239.sdk.model;

import io.darkinno.hj1239.sdk.model.enums.EmissionStandard;
import io.darkinno.hj1239.sdk.model.enums.FuelType;

import java.util.Objects;

public class VehicleInfo {

    private final String vin;
    private final String plateNumber;
    private final String plateColor;
    private final FuelType fuelType;
    private final EmissionStandard emissionStandard;
    private final String manufacturer;
    private final String model;
    private final int modelYear;

    private VehicleInfo(Builder builder) {
        this.vin = builder.vin;
        this.plateNumber = builder.plateNumber;
        this.plateColor = builder.plateColor;
        this.fuelType = builder.fuelType;
        this.emissionStandard = builder.emissionStandard;
        this.manufacturer = builder.manufacturer;
        this.model = builder.model;
        this.modelYear = builder.modelYear;
    }

    public String getVin() {
        return vin;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getPlateColor() {
        return plateColor;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public EmissionStandard getEmissionStandard() {
        return emissionStandard;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public int getModelYear() {
        return modelYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleInfo that)) return false;
        return modelYear == that.modelYear
                && Objects.equals(vin, that.vin)
                && Objects.equals(plateNumber, that.plateNumber)
                && Objects.equals(plateColor, that.plateColor)
                && fuelType == that.fuelType
                && emissionStandard == that.emissionStandard
                && Objects.equals(manufacturer, that.manufacturer)
                && Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, plateNumber, plateColor, fuelType,
                emissionStandard, manufacturer, model, modelYear);
    }

    @Override
    public String toString() {
        return "VehicleInfo{vin='" + vin + "', plateNumber='" + plateNumber
                + "', fuelType=" + fuelType + "}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String vin;
        private String plateNumber;
        private String plateColor;
        private FuelType fuelType = FuelType.OTHER;
        private EmissionStandard emissionStandard = EmissionStandard.UNKNOWN;
        private String manufacturer;
        private String model;
        private int modelYear;

        public Builder vin(String vin) {
            this.vin = vin;
            return this;
        }

        public Builder plateNumber(String plateNumber) {
            this.plateNumber = plateNumber;
            return this;
        }

        public Builder plateColor(String plateColor) {
            this.plateColor = plateColor;
            return this;
        }

        public Builder fuelType(FuelType fuelType) {
            this.fuelType = fuelType;
            return this;
        }

        public Builder emissionStandard(EmissionStandard emissionStandard) {
            this.emissionStandard = emissionStandard;
            return this;
        }

        public Builder manufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder modelYear(int modelYear) {
            this.modelYear = modelYear;
            return this;
        }

        public VehicleInfo build() {
            Objects.requireNonNull(vin, "VIN must not be null");
            return new VehicleInfo(this);
        }
    }
}
