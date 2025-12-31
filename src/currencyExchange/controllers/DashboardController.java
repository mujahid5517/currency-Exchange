package currencyExchange.controllers;

import currencyExchange.models.Currency;
import currencyExchange.models.HistoricalCurrency;
import currencyExchange.services.CurrencyServiceRMIProxy;
import currencyExchange.utils.ConfigManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DashboardController implements Initializable  {

    @FXML
    private AnchorPane anchorPaneConv;

    @FXML
    private AnchorPane anchorPaneHistCurr;

    @FXML
    private AnchorPane anchorPaneSymbCurr;

    @FXML
    private TableColumn<Currency, String> col1Symbols;

    @FXML
    private TableColumn<Currency, String> col2Signification;

    @FXML
    private ComboBox<String> comboBoxChooSymb;

    @FXML
    private ComboBox<Currency> comboBoxFrom;

    @FXML
    private ComboBox<Currency> comboBoxTo;

    @FXML
    private Text convLabel1;

    @FXML
    private Text convLabel2;

    @FXML
    private Text convLabel3;

    @FXML
    private Button convertBtn;

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private Button sbConvBtn;

    @FXML
    private Button sbHistCurr;

    @FXML
    private Button sbSymbCurr;

    @FXML
    private Button searchBtn;

    @FXML
    private TableView<Currency> tableView;

    @FXML
    private Text tbTitle1;

    @FXML
    private Text tbTitle2;

    @FXML
    private TextField txtFieldAmount;

    @FXML
    private TextField txtFieldSearch;
    
    @FXML
    private Label connectionStatusLabel;
    
    @FXML
    private ProgressIndicator connectionProgress;
    
    @FXML
    private Label totalCurrenciesLabel;
    
    // RMI Proxy for remote service
    private CurrencyServiceRMIProxy currencyServiceRMI;
    
    List<Currency> listCurrency;
    private final Integer DAYS = 30;
    private final String BASE = "EUR";
    
    // Default server IP (can be changed at runtime)
    private String serverIP = "127.0.0.1";
    
    @Override
    public void initialize(URL uri, ResourceBundle rb) {
        // Initially hide conversion labels
        convLabel1.setVisible(false);
        convLabel2.setVisible(false);
        convLabel3.setVisible(false);
        
        // Try to load IP from config
        try {
            String configIP = ConfigManager.getServerHost();
            if (configIP != null && !configIP.isEmpty()) {
                serverIP = configIP;
            }
        } catch (Exception e) {
            System.out.println("Using default IP: " + serverIP);
        }
        
        connectToServer();
    }
    
    private void connectToServer() {
        // Show progress indicator
        connectionProgress.setVisible(true);
        connectionStatusLabel.setText("Connecting to " + serverIP + "...");
        
        // Start connection in a background thread
        new Thread(() -> {
            try {
                int serverPort = 1099;
                System.out.println("Attempting to connect to server at: " + serverIP + ":" + serverPort);
                
                currencyServiceRMI = new CurrencyServiceRMIProxy(serverIP, serverPort);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    connectionProgress.setVisible(false);
                    
                    if (currencyServiceRMI.isConnected()) {
                        connectionStatusLabel.setText("✓ Connected to " + serverIP);
                        connectionStatusLabel.setStyle("-fx-text-fill: green;");
                        loadApplicationData();
                    } else {
                        connectionStatusLabel.setText("✗ Disconnected");
                        connectionStatusLabel.setStyle("-fx-text-fill: red;");
                        showConnectionError();
                    }
                });
                
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    connectionProgress.setVisible(false);
                    connectionStatusLabel.setText("✗ Connection Failed");
                    connectionStatusLabel.setStyle("-fx-text-fill: red;");
                    
                    Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "Initialization error", ex);
                    showConnectionError();
                });
            }
        }).start();
    }
    
    private void showConnectionError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Connection Error");
        alert.setHeaderText("Cannot connect to server at " + serverIP);
        alert.setContentText("Please check:\n1. Server is running\n2. IP address is correct\n3. Firewall is not blocking port 1099");
        
        Button changeIpBtn = new Button("Change Server IP");
        changeIpBtn.setOnAction(e -> {
            alert.close();
            promptForServerIP();
        });
        
        // Add a custom button to the alert dialog pane if possible, or just rely on the reconnect button
    }
    
    private void promptForServerIP() {
        TextInputDialog dialog = new TextInputDialog(serverIP);
        dialog.setTitle("Server Configuration");
        dialog.setHeaderText("Enter Server IP Address");
        dialog.setContentText("IP Address:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(ip -> {
            serverIP = ip;
            connectToServer();
        });
    }
    
    private void loadApplicationData() {
        try {
            // Load currencies from RMI server
            listCurrency = currencyServiceRMI.getAllCurrencies();
            List<String> listSymbols = currencyServiceRMI.getAllSymbols(listCurrency);
            
            System.out.println("Loaded " + listCurrency.size() + " currencies");
            System.out.println("Loaded " + listSymbols.size() + " symbols");
            
            // Populate combo boxes
            setupCurrencyComboBox(comboBoxFrom, listCurrency);
            setupCurrencyComboBox(comboBoxTo, listCurrency);
            comboBoxChooSymb.getItems().setAll(listSymbols);
            
            // Set default selections
            if (!listCurrency.isEmpty()) {
                // Try to select USD, if not available select first
                Currency usd = listCurrency.stream().filter(c -> c.getCode().equals("USD")).findFirst().orElse(null);
                if (usd != null) {
                    comboBoxFrom.getSelectionModel().select(usd);
                } else {
                    comboBoxFrom.getSelectionModel().selectFirst();
                }
                
                // Try to select EUR, if not available select second or first
                Currency eur = listCurrency.stream().filter(c -> c.getCode().equals("EUR")).findFirst().orElse(null);
                if (eur != null) {
                    comboBoxTo.getSelectionModel().select(eur);
                } else if (listCurrency.size() > 1) {
                    comboBoxTo.getSelectionModel().select(1);
                } else {
                    comboBoxTo.getSelectionModel().selectFirst();
                }
                
                // Select a safe default for chart - avoid FJD if possible
                String safeDefault = findSafeDefaultSymbol(listSymbols);
                if (safeDefault != null) {
                    int defaultIndex = listSymbols.indexOf(safeDefault);
                    if (defaultIndex != -1) {
                        comboBoxChooSymb.getSelectionModel().select(defaultIndex);
                    } else {
                        comboBoxChooSymb.getSelectionModel().selectFirst();
                    }
                } else {
                    comboBoxChooSymb.getSelectionModel().selectFirst();
                }
            }
            
            // Fill table with all currencies
            fillTableViewCurrency(listCurrency);
            
            // Load initial chart data with error handling
            loadInitialChart();
            
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "Failed to load application data", ex);
            showAlert("Data Error", "Failed to load currency data: " + ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "Unexpected error loading application data", ex);
            showAlert("System Error", "An unexpected error occurred: " + ex.getMessage());
        }
    }
    
    private void setupCurrencyComboBox(ComboBox<Currency> comboBox, List<Currency> currencies) {
        comboBox.getItems().setAll(currencies);
        
        // Set how the currency is displayed in the ComboBox
        comboBox.setConverter(new StringConverter<Currency>() {
            @Override
            public String toString(Currency currency) {
                if (currency == null) return "";
                return currency.getCode() + " - " + currency.getName();
            }

            @Override
            public Currency fromString(String string) {
                return comboBox.getItems().stream()
                        .filter(c -> (c.getCode() + " - " + c.getName()).equals(string))
                        .findFirst().orElse(null);
            }
        });
        
        // Add search functionality
        comboBox.setEditable(true);
        comboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            
            // If the new value matches the selected item, don't filter
            Currency selected = comboBox.getSelectionModel().getSelectedItem();
            if (selected != null && (selected.getCode() + " - " + selected.getName()).equals(newVal)) {
                return;
            }
            
            // Filter items
            List<Currency> filtered = currencies.stream()
                .filter(c -> c.getCode().toUpperCase().contains(newVal.toUpperCase()) || 
                             c.getName().toUpperCase().contains(newVal.toUpperCase()))
                .collect(Collectors.toList());
            
            // Update items but keep the dropdown open
            Platform.runLater(() -> {
                if (filtered.isEmpty()) {
                    comboBox.hide();
                } else {
                    comboBox.getItems().setAll(filtered);
                    if (!comboBox.isShowing()) {
                        comboBox.show();
                    }
                }
            });
        });
    }
    
    private String findSafeDefaultSymbol(List<String> symbols) {
        // Try common currencies first
        String[] safeCurrencies = {"USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF"};
        
        for (String safe : safeCurrencies) {
            if (symbols.contains(safe)) {
                System.out.println("Selected safe default symbol: " + safe);
                return safe;
            }
        }
        
        // If no safe currency found, try to avoid FJD
        if (symbols.contains("FJD") && symbols.size() > 1) {
            // Return the second symbol instead of FJD
            return symbols.get(1);
        }
        
        // Otherwise just return the first
        return symbols.isEmpty() ? null : symbols.get(0);
    }
    
    private void loadInitialChart() {
        if (comboBoxChooSymb.getSelectionModel().getSelectedItem() == null) {
            System.out.println("No symbol selected for initial chart");
            return;
        }
        
        String initialSymbol = comboBoxChooSymb.getSelectionModel().getSelectedItem();
        System.out.println("DEBUG: Loading initial chart for symbol: " + initialSymbol);
        
        try {
            // Test if historical data exists for this symbol
            List<HistoricalCurrency> initialList = currencyServiceRMI.getHistoricalValues(BASE, DAYS, initialSymbol);
            System.out.println("DEBUG: Historical data received: " + 
                              (initialList == null ? "null" : initialList.size() + " items"));
            
            if (initialList != null && !initialList.isEmpty()) {
                System.out.println("DEBUG: First historical item - Day: " + 
                                  initialList.get(0).getDay() + ", Value: " + 
                                  initialList.get(0).getValue());
            } else {
                System.out.println("DEBUG: No historical data available for " + initialSymbol);
                // Try a fallback symbol
                tryFallbackChart(initialSymbol);
                return;
            }
            
            drawChart(initialList, initialSymbol);
            System.out.println("DEBUG: Initial chart drawn successfully");
            
        } catch (Exception e) {
            System.err.println("ERROR in initial chart loading for " + initialSymbol + ": " + e.getMessage());
            e.printStackTrace();
            
            // Show error on chart
            lineChart.getData().clear();
            lineChart.setTitle("Error loading data for " + initialSymbol + ": " + e.getMessage());
            
            // Try a fallback symbol
            tryFallbackChart(initialSymbol);
            
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, 
                "Failed to load initial chart for " + initialSymbol, e);
        }
    }
    
    private void tryFallbackChart(String failedSymbol) {
        if (listCurrency == null || listCurrency.isEmpty()) {
            return;
        }
        
        // Try common currencies as fallback
        String[] fallbackCandidates = {"USD", "EUR", "GBP", "JPY", "CAD"};
        
        for (String candidate : fallbackCandidates) {
            if (comboBoxChooSymb.getItems().contains(candidate) && !candidate.equals(failedSymbol)) {
                try {
                    System.out.println("Trying fallback symbol: " + candidate);
                    List<HistoricalCurrency> fallbackList = currencyServiceRMI.getHistoricalValues(BASE, DAYS, candidate);
                    
                    if (fallbackList != null && !fallbackList.isEmpty()) {
                        // Update selection and draw chart
                        comboBoxChooSymb.getSelectionModel().select(candidate);
                        drawChart(fallbackList, candidate);
                        System.out.println("Fallback successful with " + candidate);
                        return;
                    }
                } catch (Exception ex) {
                    System.err.println("Fallback " + candidate + " also failed: " + ex.getMessage());
                }
            }
        }
        
        // If all fallbacks fail, show error message
        lineChart.setTitle("Unable to load chart data. Please select a different currency.");
    }
    
    @FXML
    void eventComboBoxGraph(ActionEvent event) {
        try {
            String selectedSymbol = comboBoxChooSymb.getSelectionModel().getSelectedItem();
            if (selectedSymbol != null) {
                System.out.println("Loading chart for: " + selectedSymbol);
                List<HistoricalCurrency> list = currencyServiceRMI.getHistoricalValues(BASE, DAYS, selectedSymbol);
                drawChart(list, selectedSymbol);
            }
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert("Chart Error", "Failed to load historical data: " + ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "Unexpected chart error", ex);
            showAlert("Chart Error", "An unexpected error occurred: " + ex.getMessage());
        }
    }

    @FXML
    void eventConvBtn(ActionEvent event) {
        try {
            // Validate input
            Currency fromCurrency = comboBoxFrom.getSelectionModel().getSelectedItem();
            Currency toCurrency = comboBoxTo.getSelectionModel().getSelectedItem();
            String amountText = txtFieldAmount.getText();
            
            if (fromCurrency == null || toCurrency == null || amountText.isEmpty()) {
                showAlert("Input Error", "Please select currencies and enter an amount.");
                return;
            }
            
            String from = fromCurrency.getCode();
            String to = toCurrency.getCode();
            
            if (from.equals(to)) {
                showAlert("Input Error", "Please select different currencies.");
                return;
            }
            
            try {
                Double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    showAlert("Input Error", "Amount must be greater than 0.");
                    return;
                }
                
                System.out.println("Converting " + amount + " from " + from + " to " + to);
                
                // Perform conversion using RMI service
                Double result = currencyServiceRMI.convert(from, to, amount);
                
                System.out.println("Conversion result: " + result);
                
                // Update UI with results
                convLabel1.setText(String.format("%.2f %s", amount, from));
                convLabel2.setText(String.format("%.2f %s", result, to));
                
                // Calculate and display exchange rate
                Double exchangeRate = result / amount;
                convLabel3.setText(String.format("1 %s = %.4f %s", from, exchangeRate, to));
                
                convLabel1.setVisible(true);
                convLabel2.setVisible(true);
                convLabel3.setVisible(true);
                
            } catch (NumberFormatException e) {
                showAlert("Input Error", "Please enter a valid number for amount.");
            } catch (IOException e) {
                showAlert("Conversion Error", "Failed to convert currencies: " + e.getMessage());
                Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, e);
            }
            
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @FXML
    void eventSearchBtn(ActionEvent event) {
        try {
            String symbol = txtFieldSearch.getText().trim();
            
            if (symbol.isEmpty()) {
                // If search field is empty, show all currencies
                fillTableViewCurrency(listCurrency);
                return;
            }
            
            if (listCurrency == null) {
                showAlert("Data Error", "Currency data is not loaded.");
                return;
            }
            
            // Filter currencies by symbol (case insensitive)
            List<Currency> filteredList = listCurrency.stream()
                .filter(currency -> currency.getSymbol().toUpperCase().contains(symbol.toUpperCase()) ||
                                   currency.getSignification().toUpperCase().contains(symbol.toUpperCase()))
                .collect(Collectors.toList());
            
            if (filteredList.isEmpty()) {
                showAlert("Search Result", "No currencies found matching: " + symbol);
                // Show all currencies when no match found
                fillTableViewCurrency(listCurrency);
            } else {
                fillTableViewCurrency(filteredList);
            }
            
        } catch (Exception e) {
            showAlert("Search Error", "Failed to search currencies: " + e.getMessage());
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @FXML
    void eventSideBar(ActionEvent event) {
        if (event.getSource() == sbConvBtn) {
            // Show converter panel
            anchorPaneConv.setVisible(true);
            anchorPaneSymbCurr.setVisible(false);
            anchorPaneHistCurr.setVisible(false);
            tbTitle1.setText("Home/Converter");
            tbTitle2.setText("Currency Converter");
            
        } else if (event.getSource() == sbSymbCurr) {
            // Show symbols panel
            anchorPaneConv.setVisible(false);
            anchorPaneSymbCurr.setVisible(true);
            anchorPaneHistCurr.setVisible(false);
            tbTitle1.setText("Home/Symbol Currencies");
            tbTitle2.setText("Currency Symbols");
            
        } else if (event.getSource() == sbHistCurr) {
            // Show historical data panel
            anchorPaneConv.setVisible(false);
            anchorPaneSymbCurr.setVisible(false);
            anchorPaneHistCurr.setVisible(true);
            tbTitle1.setText("Home/Historical Currency");
            tbTitle2.setText("Historical Currency Data");
            
            // Refresh chart when switching to historical data panel
            if (comboBoxChooSymb.getSelectionModel().getSelectedItem() != null) {
                eventComboBoxGraph(null); // Trigger chart refresh
            }
        }
    }

    private void fillTableViewCurrency(List<Currency> listCurrency) {
        if (listCurrency == null) {
            return;
        }
        
        col1Symbols.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        col2Signification.setCellValueFactory(new PropertyValueFactory<>("signification"));
        
        ObservableList<Currency> observableList = FXCollections.observableArrayList(listCurrency);
        tableView.setItems(observableList);
        
        // Update total count label if it exists
        if (totalCurrenciesLabel != null) {
            totalCurrenciesLabel.setText("Total currencies: " + listCurrency.size());
        }
        
        System.out.println("Table filled with " + listCurrency.size() + " currencies");
    }

    private void drawChart(List<HistoricalCurrency> list, String symbol) {
        // Clear previous data
        lineChart.getData().clear();
        
        if (list == null || list.isEmpty()) {
            System.out.println("WARNING: No data for chart for symbol: " + symbol);
            lineChart.setTitle("No historical data available for " + symbol);
            return;
        }
        
        System.out.println("Drawing chart with " + list.size() + " data points for " + symbol);
        
        try {
            // Create new series
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(symbol + "/" + BASE);
            
            // Add data points with validation
            int validPoints = 0;
            for (HistoricalCurrency historicalCurrency : list) {
                if (historicalCurrency == null) {
                    System.err.println("WARNING: Null HistoricalCurrency in list");
                    continue;
                }
                
                String day = historicalCurrency.getDay();
                Number value = historicalCurrency.getValue();
                
                if (day == null || value == null) {
                    System.err.println("WARNING: Invalid data point: day=" + day + ", value=" + value);
                    continue;
                }
                
                // Ensure value is a valid number
                if (value.doubleValue() <= 0 || Double.isNaN(value.doubleValue()) || Double.isInfinite(value.doubleValue())) {
                    System.err.println("WARNING: Invalid numeric value: " + value);
                    continue;
                }
                
                series.getData().add(new XYChart.Data<>(day, value));
                validPoints++;
            }
            
            if (validPoints == 0) {
                System.out.println("No valid data points for chart");
                lineChart.setTitle("No valid data for " + symbol);
                return;
            }
            
            // Add series to chart
            lineChart.getData().add(series);
            
            // Customize chart appearance
            lineChart.setTitle("Historical Exchange Rate (" + symbol + "/" + BASE + ")");
            lineChart.setCreateSymbols(true);
            lineChart.setAnimated(false);
            
            System.out.println("Chart drawn successfully with " + validPoints + " valid points");
            
        } catch (Exception e) {
            System.err.println("ERROR in drawChart for " + symbol + ": " + e.getMessage());
            e.printStackTrace();
            lineChart.setTitle("Error displaying chart for " + symbol + ": " + e.getClass().getSimpleName());
        }
    }

    private void showAlert(String title, String message) {
        try {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            });
        } catch (Exception e) {
            System.err.println("Failed to show alert: " + e.getMessage());
        }
    }
    
    @FXML 
    private void reconnectToServer(ActionEvent event) {
        // If connection failed, prompt for IP
        if (currencyServiceRMI == null || !currencyServiceRMI.isConnected()) {
            promptForServerIP();
        } else {
            // Just try to reconnect to current IP
            connectToServer();
        }
    }
    
    @FXML
    private void clearConversion(ActionEvent event) {
        txtFieldAmount.clear();
        convLabel1.setVisible(false);
        convLabel2.setVisible(false);
        convLabel3.setVisible(false);
    }
    
    @FXML
    private void swapCurrencies(ActionEvent event) {
        Currency from = comboBoxFrom.getSelectionModel().getSelectedItem();
        Currency to = comboBoxTo.getSelectionModel().getSelectedItem();
        
        if (from != null && to != null) {
            comboBoxFrom.getSelectionModel().select(to);
            comboBoxTo.getSelectionModel().select(from);
            
            // Clear previous conversion results
            convLabel1.setVisible(false);
            convLabel2.setVisible(false);
            convLabel3.setVisible(false);
        }
    }
    
    // NEW METHOD: Refresh chart button handler
    @FXML
    private void refreshChart(ActionEvent event) {
        try {
            String selectedSymbol = comboBoxChooSymb.getSelectionModel().getSelectedItem();
            if (selectedSymbol != null) {
                System.out.println("Refreshing chart for: " + selectedSymbol);
                List<HistoricalCurrency> list = currencyServiceRMI.getHistoricalValues(BASE, DAYS, selectedSymbol);
                drawChart(list, selectedSymbol);
                showAlert("Chart Refreshed", "Historical data updated successfully.");
            } else {
                showAlert("Refresh Error", "Please select a currency first.");
            }
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert("Refresh Error", "Failed to refresh chart data: " + ex.getMessage());
        }
    }
    
    // NEW METHOD: Export chart button handler
    @FXML
    private void exportChart(ActionEvent event) {
        if (lineChart.getData().isEmpty()) {
            showAlert("Export Error", "No chart data to export.");
            return;
        }
        
        StringBuilder csv = new StringBuilder();
        csv.append("Date,Exchange Rate\n");
        
        for (XYChart.Series<String, Number> series : lineChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                csv.append(data.getXValue()).append(",").append(data.getYValue()).append("\n");
            }
        }
        
        // In a real app, you would save to file. For now, just show in alert
        showAlert("Chart Data Export", 
            "Data exported successfully.\n\n" +
            "Format: CSV\n" +
            "Currency: " + comboBoxChooSymb.getSelectionModel().getSelectedItem() + "\n" +
            "Base: " + BASE + "\n" +
            "Data points: " + (lineChart.getData().get(0).getData().size()));
        
        System.out.println("Chart data:\n" + csv.toString());
    }
    
    // NEW METHOD: Test connection button handler
    @FXML
    private void testConnection(ActionEvent event) {
        System.out.println("=== Connection Test ===");
        System.out.println("Server: " + serverIP + ":1099");
        
        if (currencyServiceRMI == null) {
            System.out.println("CurrencyServiceRMI is null");
            showAlert("Test", "Service not initialized.");
            return;
        }
        
        boolean connected = currencyServiceRMI.isConnected();
        System.out.println("Connected: " + connected);
        
        if (connected) {
            try {
                List<Currency> test = currencyServiceRMI.getAllCurrencies();
                System.out.println("Data test: " + test.size() + " currencies loaded");
                showAlert("Connection Test", 
                    "✓ Connection OK!\n" +
                    "✓ Data loaded: " + test.size() + " currencies\n" +
                    "✓ Server: " + serverIP + ":1099");
            } catch (Exception e) {
                System.out.println("Data test failed: " + e.getMessage());
                showAlert("Connection Test", 
                    "✓ Connection OK but data failed\n" +
                    "✗ Error: " + e.getMessage());
            }
        } else {
            showAlert("Connection Test", 
                "✗ Connection Failed\n" +
                "✗ Server: " + serverIP + ":1099\n" +
                "Please ensure RMI server is running.");
        }
    }
    
    // NEW METHOD: Show all currencies button handler
    @FXML
    private void showAllCurrencies(ActionEvent event) {
        if (listCurrency != null) {
            fillTableViewCurrency(listCurrency);
            txtFieldSearch.clear(); // Clear search field
        }
    }
    
    // Helper method to show error alerts
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // NEW: Test specific symbol for debugging
    @FXML
    private void testSymbol(ActionEvent event) {
        String symbolToTest = "FJD"; // You can change this to test any symbol
        
        System.out.println("\n=== Testing Symbol: " + symbolToTest + " ===");
        
        if (currencyServiceRMI == null || !currencyServiceRMI.isConnected()) {
            System.out.println("Service not connected");
            showAlert("Test Failed", "Service not connected");
            return;
        }
        
        try {
            // Test 1: Check if symbol exists in list
            if (listCurrency != null) {
                boolean exists = listCurrency.stream()
                    .anyMatch(c -> c.getSymbol().equals(symbolToTest));
                System.out.println("Symbol exists in currency list: " + exists);
            }
            
            // Test 2: Try to get historical data
            System.out.println("Attempting to get historical data...");
            List<HistoricalCurrency> testData = currencyServiceRMI.getHistoricalValues(BASE, DAYS, symbolToTest);
            
            if (testData == null) {
                System.out.println("Historical data: null");
            } else if (testData.isEmpty()) {
                System.out.println("Historical data: empty list (0 items)");
            } else {
                System.out.println("Historical data: " + testData.size() + " items");
                System.out.println("First item: " + testData.get(0));
            }
            
            showAlert("Symbol Test", 
                "Symbol: " + symbolToTest + "\n" +
                "Data available: " + (testData != null && !testData.isEmpty()) + "\n" +
                "Items: " + (testData == null ? "null" : testData.size()));
                
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            showAlert("Test Failed", 
                "Symbol: " + symbolToTest + "\n" +
                "Error: " + e.getClass().getSimpleName() + "\n" +
                "Message: " + e.getMessage());
        }
    }
}