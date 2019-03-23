public enum DBConfig {
    DBHOST("localhost"),
    DBPORT("3306"),
    DBUSER("dbUser"),
    DBPASS("1234"),
    DBNAME("chat");
    private String value;

    DBConfig(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
