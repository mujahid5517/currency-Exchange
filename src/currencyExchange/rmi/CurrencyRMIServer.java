// File name: CurrencyRMIServer.java
package currencyExchange.rmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class CurrencyRMIServer {
    public static void main(String[] args) {
        try {
            printHeader();
            
            // For local testing, use localhost
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            
            System.out.println("\n================================================================\n");
            System.out.println("                INITIALIZING RMI SERVER");
            System.out.println("\n================================================================\n");
            
            System.out.println("[1/4] Checking network configuration...");
            System.out.println("    Hostname: 127.0.0.1 (localhost)");
            System.out.println("    Port: 1099");
            
            System.out.println("\n[2/4] Creating RMI Registry...");
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("    [OK] RMI Registry created on port 1099");
            } catch (Exception e) {
                System.out.println("    [INFO] Registry already exists, reusing...");
            }
            
            System.out.println("\n[3/4] Starting Currency Service...");
            CurrencyServiceRMI service = new CurrencyServiceRMIImpl();
            
            System.out.println("\n[4/4] Binding service to registry...");
            Naming.rebind("rmi://127.0.0.1:1099/CurrencyService", service);
            System.out.println("    [OK] Currency Service bound successfully");
            
            printSuccess();
            
            System.out.println("\n================================================================\n");
            System.out.println("                     SERVER READY");
            System.out.println("\n================================================================\n");
            
            System.out.println("SERVER INFORMATION:");
            System.out.println("+------------------------------------------------+");
            System.out.println("| URL:    rmi://127.0.0.1:1099/CurrencyService  |");
            System.out.println("| Host:   localhost                             |");
            System.out.println("| Port:   1099                                  |");
            System.out.println("| Status: [RUNNING]                             |");
            System.out.println("+------------------------------------------------+\n");
            
            System.out.println("Waiting for client connections...");
            System.out.println("Press Ctrl + C to stop the server");
            System.out.println("\n" + "=".repeat(64));
            
            System.out.println("\nCONNECTION LOG:");
            System.out.println("-".repeat(50));
            
            // Keep server running
            Thread.currentThread().join();
            
        } catch (Exception e) {
            printError(e);
        }
    }
    
    private static void printHeader() {
        System.out.println("\n");
        System.out.println("========================================================");
        System.out.println("       CURRENCY EXCHANGE RMI SERVER v1.0");
        System.out.println("            Developed with Java RMI");
        System.out.println("========================================================\n");
    }
    
    private static void printSuccess() {
        System.out.println("\n");
        System.out.println("========================================================");
        System.out.println("           SERVER STARTUP SUCCESSFUL");
        System.out.println("========================================================");
        System.out.println();
        System.out.println("    Server is now ready to accept client connections.");
        System.out.println();
    }
    
    private static void printError(Exception e) {
        System.out.println("\n");
        System.out.println("========================================================");
        System.out.println("                     SERVER ERROR");
        System.out.println("========================================================");
        System.out.println();
        System.out.println("Error Type: " + e.getClass().getSimpleName());
        System.out.println("Message: " + e.getMessage());
        
        if (e.getMessage() != null && e.getMessage().contains("Port already in use")) {
            System.out.println("\nTROUBLESHOOTING:");
            System.out.println("1. Port 1099 is already in use by another process");
            System.out.println("2. Open new Command Prompt and run:");
            System.out.println("   taskkill /F /IM java.exe");
            System.out.println("3. Then restart the server");
        }
        
        System.out.println("\nPress Enter to exit...");
        try {
            System.in.read();
        } catch (Exception ex) {
            // Ignore
        }
    }
}