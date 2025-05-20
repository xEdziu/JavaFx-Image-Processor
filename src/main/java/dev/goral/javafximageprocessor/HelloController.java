package dev.goral.javafximageprocessor;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class HelloController {

    @FXML private ImageView originalImageView;
    @FXML private ImageView processedImageView;
    @FXML private ComboBox<String> operationComboBox;
    @FXML private Button saveButton;
    @FXML private Button scaleButton;
    @FXML private Button rotateLeftButton;
    @FXML private Button rotateRightButton;
    @FXML private ImageView logoImageView;

    private Image originalImage;
    private Image processedImage;

    @FXML
    public void initialize() {
        operationComboBox.getItems().addAll("Negatyw", "Progowanie", "Konturowanie", "Skalowanie");
        saveButton.setDisable(true);
        scaleButton.setDisable(true);
        rotateLeftButton.setDisable(true);
        rotateRightButton.setDisable(true);

        try {
            Image logo = new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream(
                    "/dev/goral/javafximageprocessor/images/logo-pwr.jpg"
            )));
            logoImageView.setImage(logo);
        } catch (Exception e) {
            System.out.println("Błąd ładowania loga: " + e.getMessage());
        }
    }

    @FXML
    private void onLoadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj obraz JPG");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki JPG", "*.jpg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            String name = file.getName().toLowerCase();
            if (!name.endsWith(".jpg")) {
                showToast("Niedozwolony format pliku");
                return;
            }

            try {
                long start = System.currentTimeMillis();
                originalImage = new Image(file.toURI().toString());
                processedImage = null;
                originalImageView.setImage(originalImage);
                processedImageView.setImage(null);
                saveButton.setDisable(false);
                scaleButton.setDisable(false);
                rotateLeftButton.setDisable(false);
                rotateRightButton.setDisable(false);
                long end = System.currentTimeMillis();
                showToast("Pomyślnie załadowano plik");
                LoggerService.log("Wczytano obraz: " + file.getName(), "INFO", end - start);
            } catch (Exception e) {
                showToast("Nie udało się załadować pliku");
                LoggerService.logError("Wczytywanie obrazu", e);
            }
        }
    }

    @FXML
    private void onApplyOperation() {
        String selected = operationComboBox.getValue();
        if (selected == null) {
            showToast("Nie wybrano operacji do wykonania");
            return;
        }

        Image input = processedImage != null ? processedImage : originalImage;

        switch (selected) {
            case "Negatyw" -> {
                processedImage = ImageProcessor.generateNegative(input);
                processedImageView.setImage(processedImage);
                showToast("Negatyw został wygenerowany pomyślnie!");
            }
            case "Progowanie" -> showThresholdDialog();
            case "Konturowanie" -> {
                processedImage = ImageProcessor.applyEdgeDetection(input);
                processedImageView.setImage(processedImage);
                showToast("Konturowanie zostało przeprowadzone pomyślnie!");
            }
            case "Skalowanie" -> onScaleImage(); // skalowanie przez combo (alternatywnie przez przycisk)
        }
    }

    @FXML
    private void onSaveImage() {
        if (processedImage == null) {
            showToast("Na pliku nie zostały wykonane żadne operacje!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Zapis obrazu");
        dialog.setHeaderText("Podaj nazwę pliku (3–100 znaków)");
        dialog.setContentText("Nazwa:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (name.length() < 3) {
                showToast("Wpisz co najmniej 3 znaki");
                return;
            }

            if (name.length() > 100) {
                showToast("Zbyt długa nazwa pliku");
                return;
            }

            File outputDir = new File(System.getProperty("user.home") + "/Pictures");
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                showToast("Nie udało się utworzyć katalogu docelowego: " + outputDir.getAbsolutePath());
                return;
            }

            File outputFile = new File(outputDir, name + ".jpg");
            if (outputFile.exists()) {
                showToast("Plik już istnieje. Podaj inną nazwę!");
                return;
            }

            try {
                long start = System.currentTimeMillis();
                BufferedImage raw = SwingFXUtils.fromFXImage(processedImage, null);
                BufferedImage rgb = new BufferedImage(raw.getWidth(), raw.getHeight(), BufferedImage.TYPE_INT_RGB);
                var g = rgb.createGraphics();
                g.drawImage(raw, 0, 0, java.awt.Color.WHITE, null);
                g.dispose();
                long end = System.currentTimeMillis();

                if (ImageIO.write(rgb, "jpg", outputFile)) {
                    showToast("Zapisano obraz w pliku: " + outputFile.getAbsolutePath());
                    LoggerService.log("Zapisano obraz do pliku: " + outputFile.getName(), "INFO", end - start);
                } else {
                    showToast("Nie udało się zapisać obrazu.");
                    LoggerService.log("Błąd zapisu obrazu", "WARNING", end - start);
                }
            } catch (IOException e) {
                showToast("Błąd zapisu: " + e.getMessage());
                LoggerService.logError("Błąd zapisu obrazu", e);
            }
        });
    }

    @FXML
    private void onRotateLeft() {
        Image input = processedImage != null ? processedImage : originalImage;
        processedImage = ImageProcessor.rotate(input, false);
        processedImageView.setImage(processedImage);
        showToast("Obrócono w lewo");
    }

    @FXML
    private void onRotateRight() {
        Image input = processedImage != null ? processedImage : originalImage;
        processedImage = ImageProcessor.rotate(input, true);
        processedImageView.setImage(processedImage);
        showToast("Obrócono w prawo");
    }

    @FXML
    private void onScaleImage() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Skalowanie obrazu");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField widthField = new TextField();
        widthField.setPromptText("Szerokość (max 3000)");
        TextField heightField = new TextField();
        heightField.setPromptText("Wysokość (max 3000)");
        Button resetButton = new Button("Przywróć oryginalny rozmiar");

        grid.add(new Label("Szerokość:"), 0, 0);
        grid.add(widthField, 1, 0);
        grid.add(new Label("Wysokość:"), 0, 1);
        grid.add(heightField, 1, 1);
        grid.add(resetButton, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        resetButton.setOnAction(_ -> {
            processedImage = originalImage;
            processedImageView.setImage(originalImage);
            dialog.close();
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int newWidth = Integer.parseInt(widthField.getText());
                int newHeight = Integer.parseInt(heightField.getText());

                if (newWidth <= 0 || newWidth > 3000 || newHeight <= 0 || newHeight > 3000) {
                    showToast("Wymiary muszą być w zakresie 1–3000");
                    return;
                }

                Image input = processedImage != null ? processedImage : originalImage;
                processedImage = ImageProcessor.scaleImage(input, newWidth, newHeight);
                processedImageView.setImage(processedImage);
                showToast("Obraz przeskalowany");
            } catch (NumberFormatException e) {
                showToast("Wprowadź poprawne liczby całkowite!");
            }
        }
    }

    private void showThresholdDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Progowanie");
        dialog.setHeaderText("Wpisz wartość progu (0–255)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField thresholdField = new TextField();
        thresholdField.setPromptText("Wartość progu");
        grid.add(new Label("Próg:"), 0, 0);
        grid.add(thresholdField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int threshold = Integer.parseInt(thresholdField.getText());
                if (threshold < 0 || threshold > 255) {
                    showToast("Próg musi być w zakresie 0–255");
                    return;
                }

                Image input = processedImage != null ? processedImage : originalImage;
                processedImage = ImageProcessor.applyThreshold(input, threshold);
                processedImageView.setImage(processedImage);
                showToast("Progowanie zakończone");
            } catch (NumberFormatException e) {
                showToast("Wprowadź poprawną liczbę całkowitą!");
            }
        }
    }

    private void showToast(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
