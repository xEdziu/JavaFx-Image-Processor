# JavaFX Image Processor

## Opis projektu

Aplikacja **JavaFX Image Processor** to prosty edytor obrazów napisany w języku Java z wykorzystaniem biblioteki JavaFX. Umożliwia wczytywanie, przetwarzanie oraz zapisywanie obrazów w formacie JPG. Program pozwala na wykonywanie podstawowych operacji graficznych, takich jak negatyw, progowanie, konturowanie, skalowanie oraz obracanie obrazów.

Projekt został zrealizowany w ramach laboratorium z przedmiotu Platformy programistyczne .Net i Java – Laboratorium 6.

## Wymagania

- Java 17 lub nowsza
- Maven (do budowania projektu)
- JavaFX (może być dołączony przez Maven)
- System operacyjny: Windows, Linux lub macOS

## Uruchomienie

1. Sklonuj repozytorium:
   ```
   git clone https://github.com/xEdziux/JavaFx-Image-Processor.git
   ```
2. Przejdź do katalogu projektu:
   ```
   cd JavaFx-Image-Processor
   ```
3. Zbuduj projekt za pomocą Mavena:
   ```
   mvn clean install
   ```
4. Uruchom aplikację:
   ```
   mvn javafx:run
   ```
   lub uruchom klasę `HelloApplication` z poziomu IDE.

## Funkcjonalności

- **Wczytywanie obrazu** – obsługa plików JPG.
- **Negatyw** – generowanie negatywu obrazu.
- **Progowanie** – binarizacja obrazu na podstawie wybranego progu (0–255).
- **Konturowanie** – wykrywanie krawędzi na obrazie.
- **Skalowanie** – zmiana rozmiaru obrazu do zadanych wymiarów.
- **Obracanie** – obrót obrazu o 90° w lewo lub w prawo.
- **Zapis obrazu** – zapis przetworzonego obrazu do katalogu `Obrazy` użytkownika.
- **Logowanie operacji** – każda operacja jest zapisywana do pliku `log.txt` z informacją o czasie wykonania.

## Struktura projektu

```
src/
 └─ main/
     ├─ java/
     │   └─ dev/goral/javafximageprocessor/
     │        ├─ HelloApplication.java
     │        ├─ HelloController.java
     │        ├─ ImageProcessor.java
     │        └─ LoggerService.java
     └─ resources/
         └─ dev/goral/javafximageprocessor/
             ├─ main-view.fxml
             └─ images/
                 └─ logo-pwr.jpg
```

## Instrukcja obsługi

1. Uruchom aplikację.
2. Wczytaj obraz JPG za pomocą przycisku **Wczytaj obraz**.
3. Wybierz operację z listy i kliknij **Wykonaj**.
4. Możesz obracać lub skalować obraz.
5. Zapisz przetworzony obraz przyciskiem **Zapisz obraz**.

## Autor

- Adrian Goral
