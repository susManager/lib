package fundur.systems.lib;

public record EncrState(
        String algo,
        byte[] salt,
        byte[] iv
)
{}
