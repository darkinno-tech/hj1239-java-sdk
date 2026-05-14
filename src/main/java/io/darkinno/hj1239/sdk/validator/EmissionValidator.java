package io.darkinno.hj1239.sdk.validator;

import io.darkinno.hj1239.sdk.model.EmissionData;

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

        if (d.getTimestamp() != null
                && d.getTimestamp().isAfter(LocalDateTime.now().plus(Duration.ofHours(1)))) {
            r.addError("timestamp", "Timestamp is in the future");
        }
        return r.build();
    }

    private static void chk(ValidationResult.Builder r, String f, double v, double lo, double hi) {
        if (!Double.isNaN(v) && v != -1 && (v < lo || v > hi)) {
            r.addError(f, f + " out of range [" + lo + ", " + hi + "]: " + v);
        }
    }
}
