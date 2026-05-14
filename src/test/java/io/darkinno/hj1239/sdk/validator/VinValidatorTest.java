package io.darkinno.hj1239.sdk.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class VinValidatorTest {

    @Test
    void shouldValidateCorrectVin() {
        assertThat(VinValidator.isValid("LSVAM41Z6F2000001")).isTrue();
        assertThat(VinValidator.isValid("WBA3A5C52DF955059")).isTrue();
    }

    @Test
    void shouldRejectNullVin() {
        assertThat(VinValidator.isValid(null)).isFalse();
    }

    @Test
    void shouldRejectShortVin() {
        assertThat(VinValidator.isValid("LSVAM")).isFalse();
    }

    @Test
    void shouldRejectLongVin() {
        assertThat(VinValidator.isValid("LSVAM41Z6F2000001X")).isFalse();
    }

    @Test
    void shouldRejectVinWithInvalidChars() {
        assertThat(VinValidator.isValid("LSVAM41Z6FI000001")).isFalse();
        assertThat(VinValidator.isValid("LSVAM41Z6FO000001")).isFalse();
        assertThat(VinValidator.isValid("LSVAM41Z6FQ000001")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"京A12345", "京A-12345", "沪B88888"})
    void shouldValidatePlateNumber(String plate) {
        assertThat(VinValidator.isValidPlateNumber(plate)).isTrue();
    }
}
