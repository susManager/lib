package fundur.systems.lib;

public record DataState (
        String algo,
        String salt,
        byte[] iv
)
{}
