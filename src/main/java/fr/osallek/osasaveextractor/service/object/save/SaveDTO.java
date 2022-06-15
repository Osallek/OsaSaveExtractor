package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.Save;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class SaveDTO {

    private String id;

    private String name;

    private LocalDate date;

    private int nbProvinces;

    private List<TeamDTO> teams;

    private SortedSet<ProvinceDTO> provinces;

    private SortedSet<ProvinceDTO> oceansProvinces;

    private SortedSet<ProvinceDTO> lakesProvinces;

    private SortedSet<ProvinceDTO> impassableProvinces;

    public SaveDTO(Save save) {
        this.id = UUID.randomUUID().toString();
        this.name = save.getName();
        this.date = save.getDate();
        this.nbProvinces = Collections.max(save.getGame().getProvinces().keySet()); //Get the greatest id

        this.teams = CollectionUtils.isNotEmpty(save.getTeams()) ? save.getTeams().stream().map(TeamDTO::new).toList() : null;

        this.provinces = new TreeSet<>();
        this.oceansProvinces = new TreeSet<>();
        this.lakesProvinces = new TreeSet<>();
        this.impassableProvinces = new TreeSet<>();
        save.getProvinces().values().forEach(province -> {
            if (province.isImpassable()) {
                this.impassableProvinces.add(new ProvinceDTO(province));
            } else if (province.isOcean()) {
                this.oceansProvinces.add(new ProvinceDTO(province));
            } else if (province.isLake()) {
                this.lakesProvinces.add(new ProvinceDTO(province));
            } else {
                this.provinces.add(new ProvinceDTO(province));
            }
        });
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getNbProvinces() {
        return nbProvinces;
    }

    public void setNbProvinces(int nbProvinces) {
        this.nbProvinces = nbProvinces;
    }

    public List<TeamDTO> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamDTO> teams) {
        this.teams = teams;
    }

    public SortedSet<ProvinceDTO> getProvinces() {
        return provinces;
    }

    public void setProvinces(SortedSet<ProvinceDTO> provinces) {
        this.provinces = provinces;
    }

    public SortedSet<ProvinceDTO> getOceansProvinces() {
        return oceansProvinces;
    }

    public void setOceansProvinces(SortedSet<ProvinceDTO> oceansProvinces) {
        this.oceansProvinces = oceansProvinces;
    }

    public SortedSet<ProvinceDTO> getLakesProvinces() {
        return lakesProvinces;
    }

    public void setLakesProvinces(SortedSet<ProvinceDTO> lakesProvinces) {
        this.lakesProvinces = lakesProvinces;
    }

    public SortedSet<ProvinceDTO> getImpassableProvinces() {
        return impassableProvinces;
    }

    public void setImpassableProvinces(SortedSet<ProvinceDTO> impassableProvinces) {
        this.impassableProvinces = impassableProvinces;
    }
}
