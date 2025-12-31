package currencyExchange.services;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import currencyExchange.models.Currency;
import currencyExchange.models.HistoricalCurrency;
import currencyExchange.rmi.CurrencyServiceRMI;

public class CurrencyServiceRMIProxy {

    private CurrencyServiceRMI rmiService;

    public CurrencyServiceRMIProxy(String serverAddress) throws Exception {
        this(serverAddress, 1099); // Default port
    }

    public CurrencyServiceRMIProxy(String serverAddress, int port) throws Exception {
        String rmiUrl = "rmi://" + serverAddress + ":" + port + "/CurrencyService";
        System.out.println("Connecting to: " + rmiUrl);

        try {
            rmiService = (CurrencyServiceRMI) Naming.lookup(rmiUrl);
            System.out.println("✓ Successfully connected to RMI service at " + serverAddress);
        } catch (Exception e) {
            System.err.println("✗ Failed to connect: " + e.getMessage());
            throw new Exception("Cannot connect to RMI server at " + serverAddress + ". Make sure server is running.",
                    e);
        }
    }

    public Double convert(String from, String to, Double amount) throws IOException {
        try {
            return rmiService.convert(from, to, amount);
        } catch (RemoteException e) {
            throw new IOException("Conversion failed: " + e.getMessage(), e);
        }
    }

    public List<Currency> getAllCurrencies() throws IOException {
        try {
            return rmiService.getAllCurrencies();
        } catch (RemoteException e) {
            throw new IOException("Failed to get currencies: " + e.getMessage(), e);
        }
    }

    public List<String> getAllSymbols(List<Currency> list) throws IOException {
        try {
            return rmiService.getAllSymbols(list);
        } catch (RemoteException e) {
            throw new IOException("Failed to get symbols: " + e.getMessage(), e);
        }
    }

    public List<HistoricalCurrency> getHistoricalValues(String base, Integer durationDays, String symbol)
            throws IOException {
        try {
            return rmiService.getHistoricalValues(base, durationDays, symbol);
        } catch (RemoteException e) {
            throw new IOException("Failed to get historical data: " + e.getMessage(), e);
        }
    }

    public boolean isConnected() {
        return rmiService != null;
    }
}