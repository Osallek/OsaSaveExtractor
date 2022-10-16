package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.eu4parser.model.save.country.SaveCountry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class DiplomacyDTO {

    private final List<DependencyDTO> dependencies;

    private final List<RelationDTO> alliances;

    public DiplomacyDTO(Save save, List<SaveCountry> list) {
        this.dependencies = save.getDiplomacy().getDependencies().stream().map(DependencyDTO::new).collect(Collectors.toList());
        this.alliances = save.getDiplomacy().getAlliances().stream().map(RelationDTO::new).toList();

        list.stream().filter(c -> c.getOverlord() == null || !c.isAlive()) //Already found in this.dependencies
            .forEach(c -> c.getHistory()
                           .getEvents()
                           .stream()
                           .filter(event -> event.getUnion() != null)
                           .forEach(event -> save.getCountries()
                                                 .values()
                                                 .stream()
                                                 .filter(other -> !c.equals(other))
                                                 .filter(other -> (other.getMonarch() != null && other.getMonarch().getId().getId().equals(event.getUnion()))
                                                                  || (CollectionUtils.isNotEmpty(other.getPreviousMonarchs()) && other.getPreviousMonarchs()
                                                                                                                                      .stream()
                                                                                                                                      .anyMatch(m -> m.getId()
                                                                                                                                                      .equals(event.getUnion()))))
                                                 .findFirst()
                                                 .ifPresent(country -> {
                                                     AtomicReference<LocalDate> endDate = new AtomicReference<>();

                                                     c.getHistory()
                                                      .getEvents()
                                                      .stream()
                                                      .filter(e -> event.getDate().isBefore(e.getDate()))
                                                      .filter(e -> ObjectUtils.firstNonNull(e.getMonarch(), e.getMonarchConsort(), e.getMonarchHeir(),
                                                                                            e.getMonarchForeignHeir()) != null)
                                                      .filter(e -> ObjectUtils.firstNonNull(e.getMonarch(), e.getMonarchConsort(), e.getMonarchHeir(),
                                                                                            e.getMonarchForeignHeir())
                                                                              .getCountry()
                                                                              .getTagAt(e.getDate())
                                                                              .equals(c.getTagAt(e.getDate())))
                                                      .findFirst()
                                                      .ifPresent(e -> endDate.set(e.getDate()));

                                                     if (endDate.get() == null) {
                                                         endDate.set(c.getDiedAt());
                                                     }

                                                     if (endDate.get() == null) {
                                                         this.dependencies.add(
                                                                 new DependencyDTO(country.getTag(), c.getTag(), event.getDate(), "personal_union",
                                                                                   endDate.get()));
                                                     } else {
                                                         if (!country.getTagAt(endDate.get()).equals(country.getTagAt(event.getDate()))) {
                                                             List<Map.Entry<LocalDate, String>> changes = country.getChangedTags()
                                                                                                                 .entrySet()
                                                                                                                 .stream()
                                                                                                                 .filter(entry -> entry.getKey()
                                                                                                                                       .isAfter(
                                                                                                                                               event.getDate()))
                                                                                                                 .toList();

                                                             for (int i = 0; i < changes.size(); i++) {
                                                                 if (i == 0) {
                                                                     this.dependencies.add(
                                                                             new DependencyDTO(changes.get(i).getValue(), c.getTagAt(event.getDate()),
                                                                                               event.getDate(), "personal_union", changes.get(i).getKey()));
                                                                 } else {
                                                                     this.dependencies.add(new DependencyDTO(changes.get(i).getValue(),
                                                                                                             c.getTagAt(changes.get(i - 1).getKey()),
                                                                                                             changes.get(i - 1).getKey(), "personal_union",
                                                                                                             changes.get(i).getKey()));
                                                                 }
                                                             }

                                                             this.dependencies.add(new DependencyDTO(country.getTagAt(changes.get(changes.size() - 1).getKey()),
                                                                                                     c.getTagAt(changes.get(changes.size() - 1).getKey()),
                                                                                                     changes.get(changes.size() - 1).getKey(), "personal_union",
                                                                                                     endDate.get()));
                                                         } else {
                                                             this.dependencies.add(new DependencyDTO(country.getTagAt(endDate.get()), c.getTagAt(endDate.get()),
                                                                                                     event.getDate(), "personal_union", endDate.get()));
                                                         }
                                                     }
                                                 })));
        this.dependencies.removeIf(dependency -> dependency.getDate().equals(dependency.getEndDate()));
    }

    public List<DependencyDTO> getDependencies() {
        return dependencies;
    }

    public List<RelationDTO> getAlliances() {
        return alliances;
    }
}
