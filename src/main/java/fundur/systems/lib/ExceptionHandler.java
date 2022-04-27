package fundur.systems.lib;

public interface ExceptionHandler {
    void handleException(Exception e);
    void handleUrgent(Exception e);
}
