package currencyExchange.rmi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import currencyExchange.models.Currency;
import currencyExchange.models.HistoricalCurrency;
import currencyExchange.repository.Converter;
import currencyExchange.repository.ConverterImp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CurrencyServiceRMIImpl extends UnicastRemoteObject implements CurrencyServiceRMI {
    
    private final Converter converter;
    private int requestCount = 0;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public CurrencyServiceRMIImpl() throws RemoteException {
        super();
        
        log("╔══════════════════════════════════════════════════╗", "INFO");
        log("║         CURRENCY SERVICE INITIALIZED           ║", "INFO");
        log("╚══════════════════════════════════════════════════╝", "INFO");
        
        // Create the converter
        this.converter = new ConverterImp();
        
        log("Service: Currency Exchange RMI Service", "INFO");
        log("Status:  Ready", "INFO");
        log("──────────────────────────────────────────────────", "INFO");
    }

    @Override
    public Double convert(String from, String to, Double amount) throws RemoteException, IOException {
        requestCount++;
        String requestId = "REQ-" + String.format("%04d", requestCount);
        
        try {
            log("[" + requestId + "] CONVERT " + amount + " " + from + " → " + to, "REQUEST");
            
            double result = converter.convert(from, to, amount);
            double rate = result / amount;
            
            log("Result: " + String.format("%.2f", result) + " " + to, "SUCCESS");
            log("Rate:   1 " + from + " = " + String.format("%.6f", rate) + " " + to, "INFO");
            
            return result;
            
        } catch (Exception e) {
            log("Conversion failed: " + e.getMessage(), "ERROR");
            throw e;
        }
    }

    @Override
    public List<Currency> getAllCurrencies() throws RemoteException, IOException {
        requestCount++;
        String requestId = "REQ-" + String.format("%04d", requestCount);
        
        try {
            log("[" + requestId + "] GET ALL CURRENCIES", "REQUEST");
            
            List<Currency> currencies = converter.getAllCurrencies();
            
            log("Found: " + currencies.size() + " currencies", "SUCCESS");
            
            if (!currencies.isEmpty()) {
                log("──────────────────────────────────────────────────", "INFO");
                log("TOP 5 CURRENCIES:", "INFO");
                for (int i = 0; i < Math.min(5, currencies.size()); i++) {
                    Currency c = currencies.get(i);
                    log(String.format("%2d. %-6s - %s", i+1, c.getCode(), c.getName()), "INFO");
                }
                if (currencies.size() > 5) {
                    log("... and " + (currencies.size() - 5) + " more currencies", "INFO");
                }
                log("──────────────────────────────────────────────────", "INFO");
            }
            
            return currencies;
            
        } catch (Exception e) {
            log("Failed to fetch currencies: " + e.getMessage(), "ERROR");
            throw e;
        }
    }

    @Override
    public List<String> getAllSymbols(List<Currency> list) throws RemoteException {
        requestCount++;
        String requestId = "REQ-" + String.format("%04d", requestCount);
        
        try {
            log("[" + requestId + "] EXTRACT SYMBOLS", "REQUEST");
            
            List<String> symbols = new ArrayList<>();
            for (Currency currency : list) {
                symbols.add(currency.getCode());
            }
            
            log("Extracted: " + symbols.size() + " symbols", "SUCCESS");
            
            if (!symbols.isEmpty()) {
                String firstFive = String.join(", ", symbols.subList(0, Math.min(5, symbols.size())));
                log("Sample: " + firstFive, "INFO");
            }
            
            return symbols;
            
        } catch (Exception e) {
            log("Failed to extract symbols: " + e.getMessage(), "ERROR");
            throw new RemoteException("Failed to get symbols", e);
        }
    }

    @Override
    public List<HistoricalCurrency> getHistoricalValues(String base, Integer durationDays, String symbol) 
            throws RemoteException, IOException {
        requestCount++;
        String requestId = "REQ-" + String.format("%04d", requestCount);
        
        try {
            log("[" + requestId + "] HISTORICAL DATA " + base + "/" + symbol + 
                " (" + durationDays + " days)", "REQUEST");
            
            // Use the converter to get real data
            List<HistoricalCurrency> historicalData = converter.getHistoricalValues(base, durationDays, symbol);
            
            log("Retrieved: " + historicalData.size() + " data points", "SUCCESS");
            
            // Calculate statistics
            if (!historicalData.isEmpty()) {
                double min = historicalData.stream().mapToDouble(h -> h.getValue()).min().orElse(0);
                double max = historicalData.stream().mapToDouble(h -> h.getValue()).max().orElse(0);
                double avg = historicalData.stream().mapToDouble(h -> h.getValue()).average().orElse(0);
                
                log("──────────────────────────────────────────────────", "INFO");
                log("STATISTICS:", "INFO");
                log("  Minimum:  " + String.format("%.4f", min), "INFO");
                log("  Maximum:  " + String.format("%.4f", max), "INFO");
                log("  Average:  " + String.format("%.4f", avg), "INFO");
                if (min > 0) {
                    log("  Change:   " + String.format("%+.2f%%", ((max-min)/min)*100), "INFO");
                }
                log("──────────────────────────────────────────────────", "INFO");
            }
            
            return historicalData;
            
        } catch (Exception e) {
            log("Historical data error: " + e.getMessage(), "ERROR");
            throw new RemoteException("Historical data error", e);
        }
    }
    
    // Enhanced logging method
    private void log(String message, String type) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String prefix;
        
        switch (type) {
            case "REQUEST":
                prefix = "📥 REQUEST";
                break;
            case "SUCCESS":
                prefix = "✅ SUCCESS";
                break;
            case "ERROR":
                prefix = "❌ ERROR";
                break;
            case "INFO":
                prefix = "ℹ️  INFO";
                break;
            default:
                prefix = "➡️  LOG";
        }
        
        System.out.println("[" + timestamp + "] " + prefix + " | " + message);
    }
    
    // Display statistics
    public void displayStatistics() {
        log("╔══════════════════════════════════════════════════╗", "INFO");
        log("║              SERVICE STATISTICS                 ║", "INFO");
        log("╚══════════════════════════════════════════════════╝", "INFO");
        log("Total Requests: " + requestCount, "INFO");
        log("Status:         Operational", "INFO");
        log("──────────────────────────────────────────────────", "INFO");
    }
}