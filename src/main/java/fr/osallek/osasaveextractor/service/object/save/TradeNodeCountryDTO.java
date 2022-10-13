package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.trade.TradeNodeCountry;

public class TradeNodeCountryDTO {

    private String tag;

    private double value;

    private Double provincePower;

    private boolean hasCapital;

    private Double money;

    private Double shipPower;

    private Double steerPower;

    private Integer lightShip;

    public TradeNodeCountryDTO(TradeNodeCountry tradeNodeCountry) {
        this.tag = tradeNodeCountry.getCountry();
        this.value = tradeNodeCountry.getVal();
        this.provincePower = tradeNodeCountry.getProvincePower();
        this.hasCapital = tradeNodeCountry.hasCapital();
        this.money = tradeNodeCountry.getMoney();
        this.shipPower = tradeNodeCountry.getShipPower();
        this.steerPower = tradeNodeCountry.getSteerPower();
        this.lightShip = tradeNodeCountry.getNbLightShip();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Double getProvincePower() {
        return provincePower;
    }

    public void setProvincePower(Double provincePower) {
        this.provincePower = provincePower;
    }

    public boolean isHasCapital() {
        return hasCapital;
    }

    public void setHasCapital(boolean hasCapital) {
        this.hasCapital = hasCapital;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    public Double getShipPower() {
        return shipPower;
    }

    public void setShipPower(Double shipPower) {
        this.shipPower = shipPower;
    }

    public Double getSteerPower() {
        return steerPower;
    }

    public void setSteerPower(Double steerPower) {
        this.steerPower = steerPower;
    }

    public Integer getLightShip() {
        return lightShip;
    }

    public void setLightShip(Integer lightShip) {
        this.lightShip = lightShip;
    }
}
