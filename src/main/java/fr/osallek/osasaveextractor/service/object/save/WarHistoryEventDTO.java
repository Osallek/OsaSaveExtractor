package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.WarHistoryEvent;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class WarHistoryEventDTO {

    private final LocalDate date;

    private List<String> addAttacker;

    private List<String> addDefender;

    private List<String> remAttacker;
    private List<String> remDefender;

    private List<BattleDTO> battles;

    public WarHistoryEventDTO(WarHistoryEvent event) {
        this.date = event.getDate();
        this.addAttacker = event.getAddAttacker();
        this.addDefender = event.getAddDefender();
        this.remAttacker = event.getRemAttacker();
        this.remDefender = event.getRemDefender();
        this.battles = event.getBattles().stream().map(BattleDTO::new).toList();
    }

    public WarHistoryEventDTO merge(WarHistoryEventDTO other) {
        this.addAttacker = ListUtils.union(ObjectUtils.firstNonNull(this.addAttacker, new ArrayList<>()),
                                           ObjectUtils.firstNonNull(other.addAttacker, new ArrayList<>()));
        this.addDefender = ListUtils.union(ObjectUtils.firstNonNull(this.addDefender, new ArrayList<>()),
                                           ObjectUtils.firstNonNull(other.addDefender, new ArrayList<>()));
        this.remAttacker = ListUtils.union(ObjectUtils.firstNonNull(this.remAttacker, new ArrayList<>()),
                                           ObjectUtils.firstNonNull(other.remAttacker, new ArrayList<>()));
        this.remDefender = ListUtils.union(ObjectUtils.firstNonNull(this.remDefender, new ArrayList<>()),
                                           ObjectUtils.firstNonNull(other.remDefender, new ArrayList<>()));
        this.battles = ListUtils.union(ObjectUtils.firstNonNull(this.battles, new ArrayList<>()), ObjectUtils.firstNonNull(other.battles, new ArrayList<>()));

        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<String> getAddAttacker() {
        return addAttacker;
    }

    public void setAddAttacker(List<String> addAttacker) {
        this.addAttacker = addAttacker;
    }

    public List<String> getAddDefender() {
        return addDefender;
    }

    public void setAddDefender(List<String> addDefender) {
        this.addDefender = addDefender;
    }

    public List<String> getRemAttacker() {
        return remAttacker;
    }

    public void setRemAttacker(List<String> remAttacker) {
        this.remAttacker = remAttacker;
    }

    public List<String> getRemDefender() {
        return remDefender;
    }

    public void setRemDefender(List<String> remDefender) {
        this.remDefender = remDefender;
    }

    public List<BattleDTO> getBattles() {
        return battles;
    }

    public void setBattles(List<BattleDTO> battles) {
        this.battles = battles;
    }
}
