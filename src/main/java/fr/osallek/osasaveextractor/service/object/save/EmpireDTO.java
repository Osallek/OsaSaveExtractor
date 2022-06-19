package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.common.NumbersUtils;
import fr.osallek.eu4parser.model.save.empire.Empire;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class EmpireDTO {

    private final SortedSet<OldEmperorDTO> oldEmperors;

    private final boolean dismantled;

    private final Map<String, Boolean> mainLineReforms = new LinkedHashMap<>();

    private final Map<String, Boolean> leftBranchReforms = new LinkedHashMap<>();

    private final Map<String, Boolean> rightBranchReforms = new LinkedHashMap<>();

    private final double influence;

    protected LocalDate dismantleDate;

    public EmpireDTO(Empire empire) {
        this.oldEmperors = empire.getOldEmperors().stream().map(OldEmperorDTO::new).collect(Collectors.toCollection(TreeSet::new));
        this.dismantled = empire.dismantled();
        empire.getMainLinePassedReforms().forEach(imperialReform -> this.mainLineReforms.put(imperialReform.getName(), true));
        empire.getMainLineNotPassedReforms().forEach(imperialReform -> this.mainLineReforms.put(imperialReform.getName(), false));
        empire.getLeftBranchPassedReforms().forEach(imperialReform -> this.leftBranchReforms.put(imperialReform.getName(), true));
        empire.getLeftBranchNotPassedReforms().forEach(imperialReform -> this.leftBranchReforms.put(imperialReform.getName(), false));
        empire.getRightBranchPassedReforms().forEach(imperialReform -> this.rightBranchReforms.put(imperialReform.getName(), true));
        empire.getRightBranchNotPassedReforms().forEach(imperialReform -> this.rightBranchReforms.put(imperialReform.getName(), false));
        this.influence = NumbersUtils.doubleOrDefault(empire.getImperialInfluence());
        this.dismantleDate = null;
    }

    public SortedSet<OldEmperorDTO> getOldEmperors() {
        return oldEmperors;
    }

    public boolean isDismantled() {
        return dismantled;
    }

    public Map<String, Boolean> getMainLineReforms() {
        return mainLineReforms;
    }

    public Map<String, Boolean> getLeftBranchReforms() {
        return leftBranchReforms;
    }

    public Map<String, Boolean> getRightBranchReforms() {
        return rightBranchReforms;
    }

    public double getInfluence() {
        return influence;
    }

    public LocalDate getDismantleDate() {
        return dismantleDate;
    }
}
