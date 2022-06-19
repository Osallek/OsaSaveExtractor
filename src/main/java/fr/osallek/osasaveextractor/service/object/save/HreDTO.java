package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.empire.Hre;
import fr.osallek.eu4parser.model.save.province.SaveProvinceHistoryEvent;
import java.time.LocalDate;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.collections4.CollectionUtils;

public class HreDTO extends EmpireDTO {

    public HreDTO(Hre hre) {
        super(hre);

        if (hre.dismantled()) {
            if (CollectionUtils.isEmpty(hre.getOldEmperors())) {
                this.dismantleDate = hre.getSave().getStartDate();
            } else {
                AtomicReference<LocalDate> lastHreNo = new AtomicReference<>();

                hre.getSave().getProvinces().values().stream().filter(province -> province.getHistory() != null).forEach(province -> {
                    ListIterator<SaveProvinceHistoryEvent> iterator = province.getHistory().getEvents().listIterator(province.getHistory().getEvents().size());
                    while (iterator.hasPrevious()) {
                        SaveProvinceHistoryEvent history = iterator.previous();

                        if (history.getHre() != null && !history.getHre() && (lastHreNo.get() == null || history.getDate().isAfter(lastHreNo.get()))) {
                            lastHreNo.set(history.getDate());
                        }
                    }
                });

                this.dismantleDate = lastHreNo.get();
            }
        }
    }
}
