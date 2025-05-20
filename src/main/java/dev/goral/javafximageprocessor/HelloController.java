package dev.goral.javafximageprocessor;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.geometry.Insets;

import javax.imageio.ImageIO;
import java.awt.*;
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
    @FXML private ImageView logoImageView;
    @FXML private Button rotateLeftButton;
    @FXML private Button rotateRightButton;

    private Image originalImage;
    private Image processedImage;

    @FXML
    public void initialize() {
        operationComboBox.getItems().addAll("Negatyw", "Progowanie", "Konturowanie");
        saveButton.setDisable(true);

        try {
            Image logo = new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream(
                    "/dev/goral/javafximageprocessor/images/logo-pwr.jpg"
            )));
            logoImageView.setImage(logo);
        } catch (Exception e) {
            System.out.println("Nie udało się załadować loga: " + e.getMessage());
        }
    }

    @FXML
    private void onLoadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj obraz JPG");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki JPG", "*.jpg"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                originalImage = new Image(file.toURI().toString());
                originalImageView.setImage(originalImage);
                processedImageView.setImage(null);
                scaleButton.setDisable(false);
                saveButton.setDisable(false);
                rotateLeftButton.setDisable(false);
                rotateRightButton.setDisable(false);
                showToast("Pomyślnie załadowano plik");
            } catch (Exception e) {
                showToast("Nie udało się załadować pliku");
            }
            String name = file.getName().toLowerCase();
            if (!name.endsWith(".jpg")) {
                showToast("Niedozwolony format pliku");
            }
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
                showToast("Plik " + name + ".jpg już istnieje w systemie. Podaj inną nazwę pliku!");
                return;
            }

            try {
                BufferedImage bImageRaw = SwingFXUtils.fromFXImage(processedImage, null);
                BufferedImage bImage = new BufferedImage(bImageRaw.getWidth(), bImageRaw.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = bImage.createGraphics();
                g.drawImage(bImageRaw, 0, 0, java.awt.Color.WHITE, null);
                g.dispose();

                if (ImageIO.write(bImage, "jpg", outputFile)) {
                    showToast("Zapisano obraz w pliku " + outputFile.getAbsolutePath());
                } else {
                    showToast("Nie udało się zapisać obrazu w pliku " + outputFile.getAbsolutePath());
                }
            } catch (IOException e) {
                System.out.println("Nie udało się zapisać pliku: " + e.getMessage());
                showToast("Wystąpił błąd podczas zapisu pliku: " + e.getMessage());
            }
        });
    }

    @FXML
    private void onScaleImage() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Skalowanie obrazu");

        // Layout dialogu
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

        // Obsługa przycisku reset
        resetButton.setOnAction(e -> {
            if (originalImage == null) {
                showToast("Nie można przywrócić oryginalnego rozmiaru, ponieważ nie załadowano obrazu");
                return;
            }
            System.out.println(e.toString());
            processedImageView.setImage(originalImage);
            processedImage = originalImage;
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

                processedImage = scaleImage(processedImage != null ? processedImage : originalImage, newWidth, newHeight);
                processedImageView.setImage(processedImage);
                showToast("Obraz przeskalowany");
            } catch (NumberFormatException e) {
                showToast("Podaj poprawne liczby całkowite!");
            }
        }
    }

    private Image scaleImage(Image inputImage, int width, int height) {
        if (inputImage == null) return null;

        WritableImage scaledImage = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = scaledImage.getPixelWriter();

        double xRatio = inputImage.getWidth() / width;
        double yRatio = inputImage.getHeight() / height;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int srcX = (int) (x * xRatio);
                int srcY = (int) (y * yRatio);
                Color color = reader.getColor(srcX, srcY);
                writer.setColor(x, y, color);
            }
        }

        return scaledImage;
    }

    @FXML
    private void onRotateLeft() {
        rotateImage(false); // false = w lewo
    }

    @FXML
    private void onRotateRight() {
        rotateImage(true); // true = w prawo
    }

    private void rotateImage(boolean clockwise) {
        Image input = processedImage != null ? processedImage : originalImage;
        if (input == null) {
            showToast("Brak obrazu do obrotu.");
            return;
        }

        int width = (int) input.getWidth();
        int height = (int) input.getHeight();

        WritableImage rotated = new WritableImage(height, width);
        PixelReader reader = input.getPixelReader();
        PixelWriter writer = rotated.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                if (clockwise) {
                    writer.setColor(height - 1 - y, x, color);
                } else {
                    writer.setColor(y, width - 1 - x, color);
                }
            }
        }
        processedImage = rotated;
        processedImageView.setImage(rotated);
        showToast("Wykonano obrót o 90° " + (clockwise ? "w prawo" : "w lewo"));
    }

    @FXML
    private void onApplyOperation() {
        String selected = operationComboBox.getValue();
        if (selected == null) {
            showToast("Nie wybrano operacji do wykonania");
            return;
        }

        if (selected.equals("Negatyw")) {
            processedImage = generateNegative(originalImage);
            processedImageView.setImage(processedImage);
            showToast("Negatyw został wygenerowany pomyślnie!");
        }

        if (selected.equals("Progowanie")) {
            showThresholdDialog();
        }

        if (selected.equals("Konturowanie")) {
            Image input = processedImage != null ? processedImage : originalImage;
            processedImage = applyEdgeDetection(input);
            processedImageView.setImage(processedImage);
            showToast("Konturowanie zostało przeprowadzone pomyślnie!");
        }

    }

    private Image generateNegative(Image inputImage) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();

        WritableImage result = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                Color negative = new Color(1.0 - color.getRed(), 1.0 - color.getGreen(), 1.0 - color.getBlue(), color.getOpacity());
                writer.setColor(x, y, negative);
            }
        }
        return result;
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
                processedImage = applyThreshold(input, threshold);
                processedImageView.setImage(processedImage);
                showToast("Progowanie zostało przeprowadzone pomyślnie!");
            } catch (NumberFormatException e) {
                showToast("Wprowadź poprawną liczbę całkowitą!");
            }
        }
    }

    private Image applyThreshold(Image inputImage, int threshold) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();

        WritableImage result = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                double brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
                if (brightness * 255 >= threshold) {
                    writer.setColor(x, y, Color.WHITE);
                } else {
                    writer.setColor(x, y, Color.BLACK);
                }
            }
        }
        return result;
    }

    private Image applyEdgeDetection(Image inputImage) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();

        WritableImage result = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                Color center = reader.getColor(x, y);
                Color right = reader.getColor(x + 1, y);
                Color bottom = reader.getColor(x, y + 1);

                double centerGray = (center.getRed() + center.getGreen() + center.getBlue()) / 3.0;
                double rightGray = (right.getRed() + right.getGreen() + right.getBlue()) / 3.0;
                double bottomGray = (bottom.getRed() + bottom.getGreen() + bottom.getBlue()) / 3.0;

                double dx = Math.abs(centerGray - rightGray);
                double dy = Math.abs(centerGray - bottomGray);
                double edge = dx + dy;

                Color edgeColor = edge > 0.1 ? Color.WHITE : Color.BLACK;
                writer.setColor(x, y, edgeColor);
            }
        }

        return result;
    }


    private void showToast(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
