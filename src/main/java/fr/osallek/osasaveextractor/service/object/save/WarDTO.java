package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.war.ActiveWar;
import fr.osallek.eu4parser.model.save.war.PreviousWar;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WarDTO {

    private final int id;

    private final String name;

    private final LocalDate startDate;

    private final LocalDate endDate;

    private final Integer duration;

    private final boolean finished;

    private final Map<String, WarParticipantDTO> attackers;

    private final Map<String, WarParticipantDTO> defenders;

    private final Double defenderScore;

    private final Integer outcome;

    private final List<WarHistoryEventDTO> history;

    public WarDTO(int id, ActiveWar war) {
        this.id = id;
        this.name = StringUtils.trimToNull(war.getName());
        this.finished = war.isFinished();
        this.attackers = war.getPersistentAttackers()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(entry -> entry.getKey().getTag(), entry -> new WarParticipantDTO(entry.getValue()), (a, b) -> a,
                                                      LinkedHashMap::new));
        this.defenders = war.getPersistentDefenders()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(entry -> entry.getKey().getTag(), entry -> new WarParticipantDTO(entry.getValue()), (a, b) -> a,
                                                      LinkedHashMap::new));
        this.defenderScore = war.getDefenderScore();
        this.history = war.getEvents().stream().map(WarHistoryEventDTO::new).collect(Collectors.toList());
        this.history.sort(Comparator.comparing(WarHistoryEventDTO::date));
        this.startDate = this.history.get(0).date();
        this.endDate = this.finished ? this.history.get(this.history.size() - 1).date() : null;
        this.duration = (this.endDate == null || this.startDate == null) ? null : (int) ChronoUnit.MONTHS.between(this.startDate, this.endDate);

        if (war instanceof PreviousWar previousWar) {
            this.outcome = previousWar.getOutcome();
        } else {
            this.outcome = null;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public boolean isFinished() {
        return finished;
    }

    public Map<String, WarParticipantDTO> getAttackers() {
        return attackers;
    }

    public Map<String, WarParticipantDTO> getDefenders() {
        return defenders;
    }

    public Double getDefenderScore() {
        return defenderScore;
    }

    public Integer getOutcome() {
        return outcome;
    }

    public List<WarHistoryEventDTO> getHistory() {
        return history;
    }
}
