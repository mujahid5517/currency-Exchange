package currencyExchange.connectionApi;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import currencyExchange.models.HistoricalCurrency;

public class JsonConversion {

    // Primary API for Currencies and Rates (Supports 150+ countries)
    private static final String CURRENCY_API_URL = "https://latest.currency-api.pages.dev/v1/currencies.json";
    private static final String RATES_API_BASE = "https://latest.currency-api.pages.dev/v1/currencies";

    // Secondary API for Historical Data (Supports major currencies, efficient range
    // queries)
    private static final String FRANKFURTER_API_URL = "https://api.frankfurter.app";

    // Cache for currencies
    private static Map<String, String> cachedSymbols = null;
    private static long lastCacheTime = 0;
    private static final long CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 hours

    public Map<String, String> getDataSymbols() throws IOException {
        // Check cache first
        long currentTime = System.currentTimeMillis();
        if (cachedSymbols != null && (currentTime - lastCacheTime) < CACHE_DURATION) {
            System.out.println("      ✓ Using cached currency data (" + cachedSymbols.size() + " currencies)");
            return cachedSymbols;
        }

        try {
            System.out.println("      ↪ Connecting to Currency API (Full List)...");

            URL url = new URL(CURRENCY_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("API request failed with code " + responseCode);
            }

            String response;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                response = readAll(reader);
            }

            // Parse the response
            Map<String, String> symbols = parseCurrencyDataResponse(response);

            // Cache the result
            cachedSymbols = symbols;
            lastCacheTime = currentTime;

            return symbols;

        } catch (Exception e) {
            System.out.println("      ✗ API Connection Failed: " + e.getMessage());

            if (cachedSymbols != null && !cachedSymbols.isEmpty()) {
                System.out.println("      ⚠️  Using expired cache");
                return cachedSymbols;
            }

            System.out.println("      ⚠️  Using fallback currency list");
            return getFallbackCurrencies();
        }
    }

    private Map<String, String> parseCurrencyDataResponse(String json) {
        // Use TreeMap for alphabetical sorting
        Map<String, String> symbols = new TreeMap<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> keys = jsonObject.keys();

            while (keys.hasNext()) {
                String code = keys.next();
                String name = jsonObject.getString(code);

                // API returns lowercase codes, convert to Uppercase for consistency
                if (name != null && !name.isEmpty()) {
                    symbols.put(code.toUpperCase(), name);
                }
            }

            System.out.println("      ✓ Parsed " + symbols.size() + " currency symbols");
            return symbols;
        } catch (Exception e) {
            System.out.println("      ✗ JSON Parsing Error: " + e.getMessage());
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

    public double getExchangeRate(String fromCurrency, String toCurrency) throws IOException {
        try {
            if (fromCurrency.equalsIgnoreCase(toCurrency))
                return 1.0;

            System.out.println("      ↪ Fetching live exchange rate (" + fromCurrency + " -> " + toCurrency + ")...");

            // URL: .../currencies/usd.json
            String urlString = RATES_API_BASE + "/" + fromCurrency.toLowerCase() + ".json";
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("Failed to get exchange rate. Response code: " + responseCode);
            }

            String response;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                response = readAll(reader);
            }

            return parseExchangeRateResponse(response, fromCurrency, toCurrency);

        } catch (Exception e) {
            System.out.println("      ✗ Rate fetch failed: " + e.getMessage());
            double fallback = getFallbackRate(fromCurrency, toCurrency);
            if (fallback != -1)
                return fallback;
            throw new IOException("Failed to get exchange rate", e);
        }
    }

    private double parseExchangeRateResponse(String json, String from, String to) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            // Structure: { "date": "...", "usd": { "eur": 0.92, ... } }

            String fromLower = from.toLowerCase();
            String toLower = to.toLowerCase();

            if (jsonObject.has(fromLower)) {
                JSONObject rates = jsonObject.getJSONObject(fromLower);
                if (rates.has(toLower)) {
                    return rates.getDouble(toLower);
                }
            }
            throw new RuntimeException("Rate for " + to + " not found in response");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse exchange rate", e);
        }
    }

    public List<HistoricalCurrency> getHistoricalData(String base, String symbol, int days) throws IOException {
        try {
            System.out
                    .println("      ↪ Fetching historical data for " + base + "/" + symbol + " (" + days + " days)...");

            // Try Frankfurter first (Efficient, but limited currencies)
            try {
                return getHistoricalDataFrankfurter(base, symbol, days);
            } catch (Exception e) {
                System.out.println("      ⚠️  Frankfurter API failed (likely unsupported currency): " + e.getMessage());
                System.out.println("      ↪ Falling back to generated data for minor currency...");
                // In a real app, we could fetch day-by-day from currency-api, but that's slow
                // (8 requests).
                // For now, we'll throw to let the caller handle fallback generation.
                throw e;
            }

        } catch (Exception e) {
            System.out.println("      ✗ Historical fetch failed: " + e.getMessage());
            throw new IOException("Failed to get historical data", e);
        }
    }

    private List<HistoricalCurrency> getHistoricalDataFrankfurter(String base, String symbol, int days)
            throws IOException {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String startStr = startDate.format(formatter);
        String endStr = endDate.format(formatter);

        String urlString = FRANKFURTER_API_URL + "/" + startStr + ".." + endStr +
                "?from=" + base + "&to=" + symbol;

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Frankfurter API returned " + responseCode);
        }

        String response;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            response = readAll(reader);
        }

        List<HistoricalCurrency> history = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(response);

        if (jsonObject.has("rates")) {
            JSONObject rates = jsonObject.getJSONObject("rates");
            Iterator<String> dates = rates.keys();

            while (dates.hasNext()) {
                String date = dates.next();
                JSONObject dayRates = rates.getJSONObject(date);
                if (dayRates.has(symbol)) {
                    double value = dayRates.getDouble(symbol);
                    history.add(new HistoricalCurrency(date, value));
                }
            }

            history.sort((h1, h2) -> h1.getDay().compareTo(h2.getDay()));
            System.out.println("      ✓ Parsed " + history.size() + " historical points (Frankfurter)");
            return history;
        }
        throw new IOException("No rates found");
    }

    private String readAll(BufferedReader reader) throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        return response.toString();
    }

    private Map<String, String> getFallbackCurrencies() {
        Map<String, String> fallback = new TreeMap<>();
        fallback.put("USD", "United States Dollar");
        fallback.put("EUR", "Euro");
        fallback.put("GBP", "British Pound Sterling");
        fallback.put("JPY", "Japanese Yen");
        fallback.put("AUD", "Australian Dollar");
        fallback.put("CAD", "Canadian Dollar");
        fallback.put("CHF", "Swiss Franc");
        fallback.put("CNY", "Chinese Yuan");
        fallback.put("INR", "Indian Rupee");
        fallback.put("AED", "United Arab Emirates Dirham");
        return fallback;
    }

    private double getFallbackRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals("USD") && toCurrency.equals("EUR"))
            return 0.92;
        if (fromCurrency.equals("EUR") && toCurrency.equals("USD"))
            return 1.08;
        if (fromCurrency.equals("USD") && toCurrency.equals("GBP"))
            return 0.79;
        if (fromCurrency.equals("GBP") && toCurrency.equals("USD"))
            return 1.26;
        return -1;
    }
}