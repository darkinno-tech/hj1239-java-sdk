package io.darkinno.hj1239.sdk.validator;

public final class VinValidator {

    private static final int[] WEIGHTS = {8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2};

    private VinValidator() {
    }

    public static boolean isValid(String vin) {
        if (vin == null || vin.length() != 17) {
            return false;
        }
        String upper = vin.toUpperCase();
        for (int i = 0; i < 17; i++) {
            char c = upper.charAt(i);
            if (c == 'I' || c == 'O' || c == 'Q') {
                return false;
            }
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidWithCheckDigit(String vin) {
        if (!isValid(vin)) return false;
        String upper = vin.toUpperCase();
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            char c = upper.charAt(i);
            int value;
            if (c >= 'A' && c <= 'Z') {
                value = charValue(c);
            } else if (c >= '0' && c <= '9') {
                value = c - '0';
            } else {
                return false;
            }
            sum += value * WEIGHTS[i];
        }
        int remainder = sum % 11;
        char expected = remainder == 10 ? 'X' : (char) ('0' + remainder);
        return upper.charAt(8) == expected;
    }

    private static int charValue(char c) {
        int v = c - 'A' + 1;
        if (c > 'I') v--;
        if (c > 'O') v--;
        if (c > 'Q') v--;
        return v;
    }

    public static boolean isValidPlateNumber(String plateNumber) {
        if (plateNumber == null) {
            return false;
        }
        String trimmed = plateNumber.trim();
        return trimmed.length() >= 7 && trimmed.length() <= 8;
    }
}
