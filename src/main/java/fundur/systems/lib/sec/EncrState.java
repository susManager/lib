package fundur.systems.lib.sec;

public record EncrState(
        String algo,
        byte[] salt,
        byte[] iv
)
{}
