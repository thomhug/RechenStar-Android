package ch.rechenstar.app.domain.model

object ExerciseConstants {

    // Adaptive Schwierigkeit

    /** Loesezeit unter der automatisiertes Wissen angenommen wird (Sekunden) */
    const val FAST_TIME_THRESHOLD: Double = 3.0

    /** Loesezeit ueber der das Kind ueberfordert ist (Sekunden) */
    const val SLOW_TIME_THRESHOLD: Double = 7.0

    /** Anzahl Aufgaben zwischen Schwierigkeits-Anpassungen */
    const val ADAPTATION_CHECK_INTERVAL: Int = 2

    /** Groesse des rollierenden Fensters fuer Frustrations-Erkennung */
    const val FRUSTRATION_WINDOW_SIZE: Int = 4

    /** Genauigkeit unter der im Frustrations-Fenster eine Stufe gesenkt wird */
    const val FRUSTRATION_ACCURACY_THRESHOLD: Double = 0.4

    // Start-Schwierigkeit (basierend auf Durchschnitts-Genauigkeit der letzten 30 Tage)

    /** Ab dieser Genauigkeit: Start auf Schwer */
    const val START_HARD_THRESHOLD: Double = 0.9

    /** Ab dieser Genauigkeit: Start auf Mittel */
    const val START_MEDIUM_THRESHOLD: Double = 0.7

    /** Ab dieser Genauigkeit: Start auf Leicht */
    const val START_EASY_THRESHOLD: Double = 0.5

    // Aufgaben-Generierung

    /** Wahrscheinlichkeit, eine schwache Aufgabe einzustreuen (wenn vorhanden) */
    const val WEAK_EXERCISE_CHANCE: Double = 0.3

    /** Wahrscheinlichkeit fuer Lueckenaufgaben (bei berechtigten Kategorien) */
    const val GAP_FILL_CHANCE: Double = 0.3

    /** Minimum-Faktor bei Multiplikation (1*n ist trivial) */
    const val MINIMUM_MULTIPLICATION_FACTOR: Int = 2

    /** Faktoren, die bei Schwer-Multiplikation (grosses 1x1) ausgeschlossen werden */
    val EXCLUDED_HARD_MULTIPLICATION_FACTORS: Set<Int> = setOf(10, 20)

    // Session

    /** Maximale aufgezeichnete Zeit pro Aufgabe (Sekunden). Verhindert AFK-Verzerrung. */
    const val TIME_SPENT_CAP: Double = 10.0

    /** Maximale Fehlversuche bevor die Loesung angezeigt wird */
    const val MAX_ATTEMPTS: Int = 2
}
