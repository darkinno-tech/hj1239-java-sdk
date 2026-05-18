package io.darkinno.hj1239.sdk.validator;

import io.darkinno.hj1239.sdk.model.EmissionCheckData;
import io.darkinno.hj1239.sdk.model.EmissionData;
import io.darkinno.hj1239.sdk.model.HybridData;
import io.darkinno.hj1239.sdk.model.ObdEngineData;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * HJ 1239.3-2021 Table 5 field range validator.
 */
public final class EmissionValidator {

    private EmissionValidator() {}

    public static ValidationResult validate(EmissionData d) {
        if (d == null) throw new IllegalArgumentException("data must not be null");
        ValidationResult.Builder r = ValidationResult.builder();

        chk(r, "vehicleSpeed", d.getVehicleSpeed(), 0, 250.996);
        chk(r, "intakePressure", d.getIntakePressure(), 0, 125);
        chk(r, "engineTorquePercent", d.getEngineTorquePercent(), -125, 125);
        chk(r, "frictionTorquePercent", d.getFrictionTorquePercent(), -125, 125);
        chk(r, "engineSpeed", d.getEngineSpeed(), 0, 8031.875);
        chk(r, "fuelConsumptionRate", d.getFuelConsumptionRate(), 0, 3212.75);
        chk(r, "scrUpstreamNox", d.getScrUpstreamNox(), -200, 3012.75);
        chk(r, "scrDownstreamNox", d.getScrDownstreamNox(), -200, 3012.75);
        chk(r, "reagentRemaining", d.getReagentRemaining(), 0, 100);
        chk(r, "exhaustFlow", d.getExhaustFlow(), 0, 3212.75);
        chk(r, "scrInletTemp", d.getScrInletTemp(), -273, 1734.97);
        chk(r, "scrOutletTemp", d.getScrOutletTemp(), -273, 1734.97);
        chk(r, "dpfDifferentialPressure", d.getDpfDifferentialPressure(), 0, 6425.5);
        chk(r, "engineCoolantTemp", d.getEngineCoolantTemp(), -40, 210);
        chk(r, "reagentLevel", d.getReagentLevel(), 0, 100);
        chkFuture(r, "timestamp", d.getTimestamp());

        return r.build();
    }

    private static void chk(ValidationResult.Builder r, String f, double v, double lo, double hi) {
        if (!Double.isNaN(v) && v != -1 && (v < lo || v > hi)) {
            r.addError(f, f + " out of range [" + lo + ", " + hi + "]: " + v);
        }
    }

    private static void chkInt(ValidationResult.Builder r, String f, int v, int lo, int hi) {
        if (v < lo || v > hi) {
            r.addError(f, f + " out of range [" + lo + ", " + hi + "]: " + v);
        }
    }

    private static void chkFuture(ValidationResult.Builder r, String f, LocalDateTime ts) {
        if (ts != null && ts.isAfter(LocalDateTime.now().plus(Duration.ofHours(1)))) {
            r.addError(f, "Timestamp is in the future");
        }
    }

    public static ValidationResult validate(ObdEngineData d) {
        if (d == null) throw new IllegalArgumentException("data must not be null");
        ValidationResult.Builder r = ValidationResult.builder();
        chkInt(r, "dtcCount", d.getDtcCount(), 0, 255);
        chk(r, "egrErrorRate", d.getEgrErrorRate(), 0, 125);
        chkInt(r, "scrSystemStatus", d.getScrSystemStatus(), 0, 255);
        chkInt(r, "dpfSystemStatus", d.getDpfSystemStatus(), 0, 255);
        chk(r, "dpfSootLoad", d.getDpfSootLoad(), 0, 125);
        chk(r, "dpfAshLoad", d.getDpfAshLoad(), 0, 25.5);
        chkFuture(r, "timestamp", d.getTimestamp());
        return r.build();
    }

    public static ValidationResult validate(HybridData d) {
        if (d == null) throw new IllegalArgumentException("data must not be null");
        ValidationResult.Builder r = ValidationResult.builder();
        chk(r, "vehicleSpeed", d.getVehicleSpeed(), 0, 250.996);
        chk(r, "engineSpeed", d.getEngineSpeed(), 0, 8031.875);
        chk(r, "motorSpeed", d.getMotorSpeed(), 0, 16383.75);
        chk(r, "motorTorque", d.getMotorTorque(), -2000, 30767.5);
        chk(r, "batterySoc", d.getBatterySoc(), 0, 100);
        chk(r, "batteryVoltage", d.getBatteryVoltage(), 0, 6553.5);
        chk(r, "batteryCurrent", d.getBatteryCurrent(), -1000, 5553.5);
        chk(r, "fuelConsumptionRate", d.getFuelConsumptionRate(), 0, 3212.75);
        chk(r, "engineCoolantTemp", d.getEngineCoolantTemp(), -40, 210);
        chkInt(r, "hybridMode", d.getHybridMode(), 0, 7);
        chkFuture(r, "timestamp", d.getTimestamp());
        return r.build();
    }

    public static ValidationResult validate(EmissionCheckData d) {
        if (d == null) throw new IllegalArgumentException("data must not be null");
        ValidationResult.Builder r = ValidationResult.builder();
        chkInt(r, "checkType", d.getCheckType(), 0, 255);
        chk(r, "noxPemsValue", d.getNoxPemsValue(), -200, 3012.75);
        chk(r, "coPemsValue", d.getCoPemsValue(), 0, 655.35);
        chk(r, "pmPemsValue", d.getPmPemsValue(), 0, 655.35);
        chk(r, "exhaustFlow", d.getExhaustFlow(), 0, 3212.75);
        chk(r, "engineSpeed", d.getEngineSpeed(), 0, 8031.875);
        chk(r, "engineTorquePercent", d.getEngineTorquePercent(), -125, 125);
        chk(r, "engineCoolantTemp", d.getEngineCoolantTemp(), -40, 210);
        chkFuture(r, "timestamp", d.getTimestamp());
        return r.build();
    }
}
