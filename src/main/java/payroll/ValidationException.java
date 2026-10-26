package payroll;

/**
 * Custom checked exception used for business rule and input validation errors.
 * Demonstrates Module 3 syllabus concept: Custom Exceptions.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
