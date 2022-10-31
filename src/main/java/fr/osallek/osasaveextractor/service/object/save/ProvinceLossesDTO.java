package fr.osallek.osasaveextractor.service.object.save;

import java.time.LocalDate;

public class ProvinceLossesDTO {

    private LocalDate date;

    private Long losses;

    public ProvinceLossesDTO(LocalDate date, Long losses) {
        this.date = date;
        this.losses = losses;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getLosses() {
        return losses;
    }

    public void setLosses(Long losses) {
        this.losses = losses;
    }
}
