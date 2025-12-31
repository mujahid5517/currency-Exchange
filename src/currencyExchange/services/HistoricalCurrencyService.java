package currencyExchange.services;

import currencyExchange.models.HistoricalCurrency;
import currencyExchange.repository.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HistoricalCurrencyService {

    private final Converter converter;

    public HistoricalCurrencyService(Converter converter) {
        this.converter = converter;
    }

    public List<HistoricalCurrency> HistoricalDataCurrency(String base, Integer durationDays, String symbol)
            throws IOException {
        // TODO: Implement actual historical data retrieval
        // For now, return empty list or mock data

        System.out.println("HistoricalCurrencyService: Getting historical data for " +
                base + "/" + symbol + " over " + durationDays + " days");

        // Return empty list or implement actual API call
        return new ArrayList<>();

        // If you need to implement this, you might need to add a method to your
        // Converter
        // like: converter.getHistoricalRates(base, symbol, durationDays);
    }
}