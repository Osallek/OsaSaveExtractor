package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.eu4parser.model.save.trade.SaveTradeNode;
import fr.osallek.osasaveextractor.common.Constants;

import java.util.List;
import java.util.stream.Collectors;

public class TradeNodeDTO extends NamedLocalisedDTO {

    private double retention;

    private ColorDTO color;

    private List<TradeNodeCountryDTO> countries;

    private List<TradeNodeIncomingDTO> incoming;

    public TradeNodeDTO(Save save, SaveTradeNode tradeNode) {
        super(save.getGame().getLocalisation(tradeNode.getName()), tradeNode.getName());
        this.color = save.getGame().getTradeNode(tradeNode.getName()).getColor() == null ? Constants.stringToColor(this.name) :
                     new ColorDTO(save.getGame().getTradeNode(tradeNode.getName()).getColor());
        this.retention = tradeNode.getRetention();
        this.countries = tradeNode.getCountries().values().stream().filter(c -> c.getVal() != null && c.getVal() > 0).map(TradeNodeCountryDTO::new).toList();
        this.incoming = tradeNode.getIncoming().stream().map(TradeNodeIncomingDTO::new).collect(Collectors.toList());
        this.incoming.add(new TradeNodeIncomingDTO(tradeNode.getLocalValue()));
    }

    public double getRetention() {
        return retention;
    }

    public void setRetention(double retention) {
        this.retention = retention;
    }

    public ColorDTO getColor() {
        return color;
    }

    public void setColor(ColorDTO color) {
        this.color = color;
    }

    public List<TradeNodeCountryDTO> getCountries() {
        return countries;
    }

    public void setCountries(List<TradeNodeCountryDTO> countries) {
        this.countries = countries;
    }

    public List<TradeNodeIncomingDTO> getIncoming() {
        return incoming;
    }

    public void setIncoming(List<TradeNodeIncomingDTO> incoming) {
        this.incoming = incoming;
    }
}
