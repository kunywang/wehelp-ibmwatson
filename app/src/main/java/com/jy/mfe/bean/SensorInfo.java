package com.jy.mfe.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * @author kunpn
 */
public class SensorInfo implements Serializable {
    private int water;
    private int temperature;
    private int power;
    private int chemical;

    @JSONField(serialize = false)
    public boolean isEquals(SensorInfo si){
        if(this.getWater() != si.getWater() || this.getChemical() != si.getChemical() ||this.getPower() != si.getPower() || this.getTemperature() != si.getTemperature())
        {
            return false;
        }

        return true;
    }

    @JSONField(serialize = false)
    public void setValue(SensorInfo si){
        this.setWater(si.getWater()) ;
        this.setChemical(si.getChemical());
        this.setPower(si.getPower()) ;
        this.setTemperature(si.getTemperature());
    }

    public int getWater() {
        return water;
    }
    public void setWater(int water) {
        this.water = water;
    }

    public int getTemperature() {
        return temperature;
    }
    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getPower() {
        return power;
    }
    public void setPower(int power) {
        this.power = power;
    }

    public int getChemical() {
        return chemical;
    }
    public void setChemical(int chemical) {
        this.chemical = chemical;
    }
}
