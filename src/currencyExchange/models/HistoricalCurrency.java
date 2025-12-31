package currencyExchange.models;

import java.io.Serializable;

public class HistoricalCurrency implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String day;
    private double value;
    
    public HistoricalCurrency() {
    }
    
    public HistoricalCurrency(String day, double value) {
        this.day = day;
        this.value = value;
    }
    
    public String getDay() {
        return day;
    }
    
    public void setDay(String day) {
        this.day = day;
    }
    
    public double getValue() {
        return value;
    }
    
    public void setValue(double value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return "HistoricalCurrency{day='" + day + "', value=" + value + "}";
    }
}
