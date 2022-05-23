package fundur.systems.lib;

public class Entry {
    private String name;
    private String usr;
    private String pwd;

    private String notes;

    private long timestamp;

    public Entry(String name, String usr, String pwd, String notes, long timestamp) {
        this.name = name;
        this.usr = usr;
        this.pwd = pwd;
        this.notes = notes;
        this.timestamp = timestamp;
    }

    public String name() {
        return name;
    }

    public Entry setName(String name) {
        this.name = name;
        return this;
    }

    public String usr() {
        return usr;
    }

    public Entry setUsr(String usr) {
        this.usr = usr;
        return this;
    }

    public String pwd() {
        return pwd;
    }

    public Entry setPwd(String pwd) {
        this.pwd = pwd;
        return this;
    }

    public String notes() {
        return notes;
    }

    public Entry setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public long timestamp() {
        return timestamp;
    }

    public Entry setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "name='" + name + '\'' +
                ", usr='" + usr + '\'' +
                ", pwd='" + pwd + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
