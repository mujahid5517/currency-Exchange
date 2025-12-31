package currencyExchange.rmi;

import currencyExchange.models.Currency;
import currencyExchange.models.HistoricalCurrency;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface CurrencyServiceRMI extends Remote {
    
    // This should match CurrencyServiceRMIImpl
    Double convert(String from, String to, Double amount) throws RemoteException, IOException;
    
    List<Currency> getAllCurrencies() throws RemoteException, IOException;
    
    List<String> getAllSymbols(List<Currency> list) throws RemoteException;
    
    List<HistoricalCurrency> getHistoricalValues(String base, Integer durationDays, String symbol) 
            throws RemoteException, IOException;
}