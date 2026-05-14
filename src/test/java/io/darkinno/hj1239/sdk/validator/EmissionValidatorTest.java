package io.darkinno.hj1239.sdk.validator;

import io.darkinno.hj1239.sdk.model.EmissionData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EmissionValidatorTest {

    @Test
    void shouldPassValidData() {
        EmissionData d = EmissionData.builder()
                .timestamp(LocalDateTime.now())
                .vehicleSpeed(60.0).engineSpeed(1500.0).fuelConsumptionRate(8.5)
                .engineCoolantTemp(85.0).scrUpstreamNox(50.0).scrDownstreamNox(10.0)
                .reagentRemaining(80.0).intakePressure(100.0).exhaustFlow(200.0)
                .dpfDifferentialPressure(1.5).reagentLevel(75.0)
                .build();
        assertThat(EmissionValidator.validate(d).isValid()).isTrue();
    }

    @Test
    void shouldRejectNoxOverRange() {
        EmissionData d = EmissionData.builder()
                .timestamp(LocalDateTime.now()).vehicleSpeed(60.0).engineSpeed(1500.0)
                .fuelConsumptionRate(8.0).engineCoolantTemp(85.0)
                .scrUpstreamNox(3100.0).scrDownstreamNox(10.0)
                .build();
        ValidationResult r = EmissionValidator.validate(d);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> e.getField().equals("scrUpstreamNox"));
    }

    @Test
    void shouldRejectNegativeSpeed() {
        EmissionData d = EmissionData.builder()
                .timestamp(LocalDateTime.now()).vehicleSpeed(-10.0).engineSpeed(1500.0)
                .fuelConsumptionRate(8.0).engineCoolantTemp(85.0)
                .scrUpstreamNox(50.0).scrDownstreamNox(10.0)
                .build();
        ValidationResult r = EmissionValidator.validate(d);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> e.getField().equals("vehicleSpeed"));
    }

    @Test
    void shouldAllowNaNUnavailable() {
        EmissionData d = EmissionData.builder()
                .timestamp(LocalDateTime.now()).vehicleSpeed(60.0).engineSpeed(1500.0)
                .fuelConsumptionRate(8.0).engineCoolantTemp(Double.NaN)
                .scrUpstreamNox(50.0).scrDownstreamNox(10.0)
                .engineTorquePercent(Double.NaN).frictionTorquePercent(Double.NaN)
                .scrInletTemp(Double.NaN).scrOutletTemp(Double.NaN)
                .intakePressure(100).exhaustFlow(200).dpfDifferentialPressure(1.5)
                .reagentRemaining(80).reagentLevel(75)
                .build();
        assertThat(EmissionValidator.validate(d).isValid()).isTrue();
    }
}
