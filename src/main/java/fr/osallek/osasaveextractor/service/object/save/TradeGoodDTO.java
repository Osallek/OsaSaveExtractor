package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.TradeGood;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.osasaveextractor.common.Constants;

public class TradeGoodDTO extends NamedImageLocalisedDTO {

    private ColorDTO color;

    public TradeGoodDTO(Save save, TradeGood tradeGood) {
        super(save.getGame().getLocalisation(tradeGood.getName()), tradeGood.getWritenTo(), tradeGood.getName());
        this.color = tradeGood.getColor() == null ? Constants.stringToColor(this.name) : new ColorDTO(tradeGood.getColor());
    }

    public ColorDTO getColor() {
        return color;
    }

    public void setColor(ColorDTO color) {
        this.color = color;
    }
}
