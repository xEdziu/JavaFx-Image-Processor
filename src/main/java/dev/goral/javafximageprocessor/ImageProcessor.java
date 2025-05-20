package dev.goral.javafximageprocessor;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

public class ImageProcessor {

    public static Image generateNegative(Image inputImage) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();
        WritableImage result = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        int threads = 4;
        Thread[] workers = new Thread[threads];
        int partHeight = height / threads;

        long start = System.currentTimeMillis();

        for (int t = 0; t < threads; t++) {
            final int startY = t * partHeight;
            final int endY = (t == threads - 1) ? height : startY + partHeight;

            workers[t] = new Thread(() -> {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        Color c = reader.getColor(x, y);
                        Color neg = new Color(1.0 - c.getRed(), 1.0 - c.getGreen(), 1.0 - c.getBlue(), c.getOpacity());
                        writer.setColor(x, y, neg);
                    }
                }
            });
            workers[t].start();
        }

        for (Thread thread : workers) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                LoggerService.logError("Negatyw", e);
            }
        }

        long end = System.currentTimeMillis();
        LoggerService.log("Negatyw", "INFO", end - start);
        return result;
    }

    public static Image applyThreshold(Image inputImage, int threshold) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();
        WritableImage result = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        int threads = 4;
        Thread[] workers = new Thread[threads];
        int partHeight = height / threads;

        long start = System.currentTimeMillis();

        for (int t = 0; t < threads; t++) {
            final int startY = t * partHeight;
            final int endY = (t == threads - 1) ? height : startY + partHeight;

            workers[t] = new Thread(() -> {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        Color color = reader.getColor(x, y);
                        double brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
                        Color binary = (brightness * 255 >= threshold) ? Color.WHITE : Color.BLACK;
                        writer.setColor(x, y, binary);
                    }
                }
            });
            workers[t].start();
        }

        for (Thread thread : workers) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                LoggerService.logError("Progowanie", e);
            }
        }

        long end = System.currentTimeMillis();
        LoggerService.log("Progowanie", "INFO", end - start);
        return result;
    }

    public static Image applyEdgeDetection(Image inputImage) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();
        WritableImage result = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        int threads = 4;
        Thread[] workers = new Thread[threads];
        int partHeight = height / threads;

        long start = System.currentTimeMillis();

        for (int t = 0; t < threads; t++) {
            final int startY = t * partHeight;
            final int endY = (t == threads - 1) ? height - 1 : Math.min(height - 1, startY + partHeight);

            workers[t] = new Thread(() -> {
                for (int y = Math.max(1, startY); y < endY; y++) {
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
            });
            workers[t].start();
        }

        for (Thread thread : workers) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                LoggerService.logError("Konturowanie", e);
            }
        }

        long end = System.currentTimeMillis();
        LoggerService.log("Konturowanie", "INFO", end - start);
        return result;
    }

    public static Image scaleImage(Image inputImage, int width, int height) {
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

    public static Image rotate(Image inputImage, boolean clockwise) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();

        WritableImage rotated = new WritableImage(height, width);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = rotated.getPixelWriter();

        long start = System.currentTimeMillis();
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
        long end = System.currentTimeMillis();
        LoggerService.log("Obrócono obraz " + (clockwise ? "w prawo" : "w lewo"), "INFO", end - start);

        return rotated;
    }
}