package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.trade.TradeNodeIncoming;

public class TradeNodeIncomingDTO {

    private String from;

    private double value;

    private double added;

    public TradeNodeIncomingDTO(Double value) {
        this.value = value;
        this.added = 0d;
    }

    public TradeNodeIncomingDTO(TradeNodeIncoming tradeNodeIncoming) {
        this.from = tradeNodeIncoming.getFrom().getName();
        this.value = tradeNodeIncoming.getValue();
        this.added = tradeNodeIncoming.getAdd();
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getAdded() {
        return added;
    }

    public void setAdded(double added) {
        this.added = added;
    }
}
