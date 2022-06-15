package fr.osallek.osasaveextractor.service.object.save;

public class NameDTO {

    private String name;

    private String oldName;

    public NameDTO(String name, String oldName) {
        this.name = name;
        this.oldName = oldName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }
}
