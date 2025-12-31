package currencyExchange.connectionApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;

public class ApiConnection {
    private final String url;
    private final String apiKey;

    public ApiConnection(String url, String apiKey) {
        this.url = url;
        this.apiKey = apiKey;
    }

    public StringBuilder getRate(String from, String to, Double amount) throws MalformedURLException, IOException {
        String urlStr = url + "/convert?to=" + to + "&from=" + from + "&amount=" + amount;
        return getResponse(urlStr);
    }

    public StringBuilder getSymbolsWithSignification() throws MalformedURLException, IOException {
        String urlStr = url + "/symbols";
        return getResponse(urlStr);
    }

    public StringBuilder getHistoricalCurrency(String base, LocalDate startDate, LocalDate endDate, String symbol)
            throws MalformedURLException, IOException {
        String urlStr = url + "/timeseries?start_date=" + startDate + "&end_date=" + endDate +
                "&base=" + base + "&symbols=" + symbol;
        return getResponse(urlStr);
    }

    private StringBuilder getResponse(String urlStr) throws MalformedURLException, IOException {
        StringBuilder response = new StringBuilder();

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("apikey", apiKey);

        int responseCode = conn.getResponseCode();
        System.out.println("API Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } else {
            System.out.println("GET request failed: " + responseCode);
            response.append("{\"error\":\"API request failed with code " + responseCode + "\"}");
        }

        conn.disconnect();
        return response;
    }
}