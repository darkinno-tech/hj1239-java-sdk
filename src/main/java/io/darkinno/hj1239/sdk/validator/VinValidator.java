package io.darkinno.hj1239.sdk.validator;

public final class VinValidator {

    private VinValidator() {
    }

    public static boolean isValid(String vin) {
        if (vin == null || vin.length() != 17) {
            return false;
        }
        for (char c : vin.toUpperCase().toCharArray()) {
            if (c == 'I' || c == 'O' || c == 'Q') {
                return false;
            }
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidPlateNumber(String plateNumber) {
        if (plateNumber == null) {
            return false;
        }
        String trimmed = plateNumber.trim();
        return trimmed.length() >= 7 && trimmed.length() <= 8;
    }
}
