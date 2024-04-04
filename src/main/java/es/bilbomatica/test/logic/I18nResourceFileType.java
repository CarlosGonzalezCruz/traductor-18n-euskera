package es.bilbomatica.test.logic;

public enum I18nResourceFileType {
    AUTO("Auto"),
    PROPERTIES("Properties"),
    JSON("JSON"),
    XML("XML");


    private String name;

    private I18nResourceFileType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean matches(String name) {
        return this.getName().toUpperCase().equals(name.toUpperCase());
    }
}
