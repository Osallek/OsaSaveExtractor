package fr.osallek.osasaveextractor.service.object.server;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class AssetsToSendDTO {

    private Path provinces;

    private Path colors;

    private Set<Path> countries = new HashSet<>();

    private Set<Path> advisors = new HashSet<>();

    private Set<Path> institutions = new HashSet<>();

    private Set<Path> buildings = new HashSet<>();

    private Set<Path> religions = new HashSet<>();

    private Set<Path> goods = new HashSet<>();

    public Path getProvinces() {
        return provinces;
    }

    public void setProvinces(Path provinces) {
        this.provinces = provinces;
    }

    public Path getColors() {
        return colors;
    }

    public void setColors(Path colors) {
        this.colors = colors;
    }

    public Set<Path> getCountries() {
        return countries;
    }

    public void setCountries(Set<Path> countries) {
        this.countries = countries;
    }

    public Set<Path> getAdvisors() {
        return advisors;
    }

    public void setAdvisors(Set<Path> advisors) {
        this.advisors = advisors;
    }

    public Set<Path> getInstitutions() {
        return institutions;
    }

    public void setInstitutions(Set<Path> institutions) {
        this.institutions = institutions;
    }

    public Set<Path> getBuildings() {
        return buildings;
    }

    public void setBuildings(Set<Path> buildings) {
        this.buildings = buildings;
    }

    public Set<Path> getReligions() {
        return religions;
    }

    public void setReligions(Set<Path> religions) {
        this.religions = religions;
    }

    public Set<Path> getGoods() {
        return goods;
    }

    public void setGoods(Set<Path> goods) {
        this.goods = goods;
    }
}
