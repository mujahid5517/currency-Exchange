package currencyExchange.services;

import currencyExchange.models.Currency;
import currencyExchange.repository.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CurrencyService {

    private final Converter converter;

    public CurrencyService(Converter converter) {
        this.converter = converter;
    }

    public Double convert(String from, String to, Double amount) throws IOException {
        return converter.convert(from, to, amount);
    }

    public List<Currency> getAllCurrencies() throws IOException {
        return converter.getAllCurrencies();
    }

    public List<String> getAllSymbols(List<Currency> currencies) {
        List<String> symbols = new ArrayList<>();

        if (currencies != null) {
            for (Currency currency : currencies) {
                symbols.add(currency.getCode());
            }
        }

        return symbols;
    }
}