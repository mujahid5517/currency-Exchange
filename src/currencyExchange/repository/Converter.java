package currencyExchange.repository;

import currencyExchange.models.Currency;
import currencyExchange.models.HistoricalCurrency;
import java.io.IOException;
import java.util.List;

public interface Converter {
    List<Currency> getAllCurrencies() throws IOException;

    double convert(String fromCurrency, String toCurrency, double amount) throws IOException;

    List<HistoricalCurrency> getHistoricalValues(String base, Integer durationDays, String symbol) throws IOException;
}