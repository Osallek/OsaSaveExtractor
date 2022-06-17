package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.country.CountryState;

public class CountryStateDTO {

    private final Double prosperity;

    private final Boolean patriarch;

    private final Boolean pasha;

    private final String holyOrder;

    private final String edict;

    public CountryStateDTO(CountryState state) {
        this.prosperity = state.getProsperity();
        this.patriarch = state.hasStatePatriarch();
        this.pasha = state.hasStatePasha();
        this.holyOrder = state.getHolyOrder() != null ? state.getHolyOrder().getName() : null;
        this.edict = state.getActiveEdict() != null ? state.getActiveEdict().getWhich().getName() : null;
    }

    public Double getProsperity() {
        return prosperity;
    }

    public Boolean getPatriarch() {
        return patriarch;
    }

    public Boolean getPasha() {
        return pasha;
    }

    public String getHolyOrder() {
        return holyOrder;
    }

    public String getEdict() {
        return edict;
    }
}
