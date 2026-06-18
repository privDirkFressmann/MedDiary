# MedDiary - Medizinischer Termin- & Vorsorgeplaner

MedDiary ist eine moderne, native Android-App, die dir dabei hilft, deine medizinischen Termine zu verwalten und den Überblick über wichtige gesetzliche Vorsorgeuntersuchungen zu behalten.

Die App ist mit **Kotlin** und **Jetpack Compose** (Material 3) entwickelt und verwendet eine lokale **Room-Datenbank** zur sicheren Speicherung deiner Daten auf dem Gerät.

## Features

1. **Dashboard (Startbildschirm)**:
   - Übersicht über anstehende Arzttermine.
   - Statusanzeige zu fälligen Vorsorgeuntersuchungen.
   - Schnellzugriff auf Kalender und Vorsorge-Katalog.

2. **Terminverwaltung (Kalender & Hinzufügen)**:
   - Termine mit Fachrichtung, Arztname, Datum, Uhrzeit, Notizen und optionaler Erinnerung eintragen.
   - Kalenderansicht, sortiert und gruppiert nach Monat und Jahr.
   - Termine direkt in der Liste als erledigt markieren oder löschen.

3. **Vorsorge-Katalog**:
   - Vordefinierte gesetzliche Vorsorgeuntersuchungen (Zahnarzt, Gesundheits-Check-up, Hautkrebs-Screening, etc.) basierend auf den deutschen Empfehlungen.
   - Filterbar nach Fachrichtung (z.B. Zahnmedizin, Krebsvorsorge, Impfungen).
   - Eigene (benutzerdefinierte) Vorsorgeuntersuchungen hinzufügen mit individuelsem Intervall in Monaten.
   - Automatische Berechnung des nächsten Fälligkeitsdatums nach Markierung als "erledigt".
   - Visuelle Kennzeichnung überfälliger oder bald fälliger Untersuchungen (Rot/Orange).

## Projektstruktur

- **`app/src/main/java/com/meddiary/`**:
  - [MainActivity.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/MainActivity.kt): Einstiegspunkt der App, lädt die Navigation.
  - [MedDiaryApplication.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/MedDiaryApplication.kt): Initialisiert die Room-Datenbank.
  - **`data/`**: Room-Entities ([Appointment.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/data/Appointment.kt), [Checkup.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/data/Checkup.kt)), DAOs ([AppointmentDao.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/data/AppointmentDao.kt), [CheckupDao.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/data/CheckupDao.kt)) und Datenbank-Definition ([MedicalDatabase.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/data/MedicalDatabase.kt)).
  - **`ui/`**: 
    - `theme/`: Material 3 Farbpalette ([Color.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/ui/theme/Color.kt), [Theme.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/ui/theme/Theme.kt)) und Typografie ([Type.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/ui/theme/Type.kt)).
    - `screens/`: Compose UI Screens ([HomeScreen.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/ui/screens/HomeScreen.kt), [AddEditAppointmentScreen.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/ui/screens/AddEditAppointmentScreen.kt), [CheckupsScreen.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/ui/screens/CheckupsScreen.kt), [CalendarScreen.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/ui/screens/CalendarScreen.kt)).
    - `components/`: Wiederverwendbare UI-Elemente ([AppointmentCard.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/ui/components/AppointmentCard.kt), [CheckupCard.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/ui/components/CheckupCard.kt)).
  - **`navigation/`**: Routen-Deklarationen ([Screen.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/navigation/Screen.kt)) und Navigation-Controller ([AppNavigation.kt](file:///home/dirk/Projects/MedDiary/app/src/main/java/com/meddiary/navigation/AppNavigation.kt)).

## So startest du das Projekt

1. Öffne **Android Studio** (Hedgehog / Iguana / Jellyfish / Koala oder neuer).
2. Wähle **Open** und navigiere zu diesem Verzeichnis: `/home/dirk/Projects/MedDiary`.
3. Android Studio erkennt das Gradle-Projekt automatisch und führt den Gradle-Sync durch.
4. Klicke auf den **Run Button** (grüner Play-Pfeil), um die App auf einem Emulator oder deinem physischen Android-Gerät zu starten.

## Voraussetzungen

- Android SDK (Min API Level 26 / Android 8.0+)
- Java Development Kit (JDK) 17
