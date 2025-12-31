package currencyExchange;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import currencyExchange.utils.ConfigManager; // Add this import

public class Main extends Application {

    public static void main(String[] args) {
        System.out.println("=== Starting Currency Exchange Client ===");
        System.out.println("Make sure RMI server is running first!");

        // Get server address from ConfigManager
        String serverHost = ConfigManager.getServerHost();
        int serverPort = ConfigManager.getServerPort();
        System.out.println("Server should be at: rmi://" + serverHost + ":" + serverPort + "/CurrencyService");

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Loading FXML interface...");

        try {
            Parent root = FXMLLoader.load(getClass()
                    .getResource("/currencyExchange/views/Dashboard.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Currency Exchange - Client");
            stage.show();

            System.out.println("Client interface loaded successfully");

        } catch (Exception e) {
            System.out.println("Failed to load FXML: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}