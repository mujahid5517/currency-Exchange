package currencyExchange.models;

import java.io.Serializable;

public class Currency implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String name;
    private double rate;
    private String symbol;
    private String signification;

    // Required by RMI (MUST HAVE) - Default constructor
    public Currency() {
        this.code = "";
        this.name = "";
        this.rate = 0.0;
        this.symbol = "";
        this.signification = "";
    }

    // Constructor for creating from API data (just code and name)
    public Currency(String code, String name) {
        this.code = code;
        this.name = name;
        this.rate = 0.0; // Rate will be fetched separately
        this.symbol = code; // Symbol is usually the same as code
        this.signification = name; // Signification is usually the name
    }

    // Full constructor
    public Currency(String code, String name, double rate, String symbol, String signification) {
        this.code = code;
        this.name = name;
        this.rate = rate;
        this.symbol = symbol;
        this.signification = signification;
    }

    // REMOVE OR FIX this constructor - it's causing the error!
    // public Currency(String symbol, String signification) {
    //     throw new UnsupportedOperationException("Not supported yet.");
    // }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSignification() {
        return signification;
    }

    public void setSignification(String signification) {
        this.signification = signification;
    }

    @Override
    public String toString() {
        return "Currency{code='" + code + "', name='" + name +
                "', rate=" + rate + ", symbol='" + symbol +
                "', signification='" + signification + "'}";
    }
    
    // Optional: Add equals and hashCode for better collection handling
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Currency currency = (Currency) obj;
        return code.equals(currency.code);
    }
    
    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
