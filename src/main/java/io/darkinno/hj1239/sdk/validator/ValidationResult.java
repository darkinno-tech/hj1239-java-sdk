package io.darkinno.hj1239.sdk.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidationResult {

    private final boolean valid;
    private final List<FieldError> errors;

    private ValidationResult(boolean valid, List<FieldError> errors) {
        this.valid = valid;
        this.errors = Collections.unmodifiableList(errors);
    }

    public boolean isValid() {
        return valid;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class FieldError {
        private final String field;
        private final String message;

        FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return "{" + field + ": " + message + "}";
        }
    }

    public static class Builder {
        private final List<FieldError> errors = new ArrayList<>();

        public Builder addError(String field, String message) {
            errors.add(new FieldError(field, message));
            return this;
        }

        public ValidationResult build() {
            return new ValidationResult(errors.isEmpty(), new ArrayList<>(errors));
        }
    }

    @Override
    public String toString() {
        return "ValidationResult{valid=" + valid + ", errors=" + errors + "}";
    }
}
