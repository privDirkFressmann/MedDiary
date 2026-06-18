package com.meddiary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Appointment::class, Checkup::class, FamilyMember::class, Attachment::class], version = 4, exportSchema = false)
abstract class MedicalDatabase : RoomDatabase() {

    abstract fun appointmentDao(): AppointmentDao
    abstract fun checkupDao(): CheckupDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        @Volatile
        private var INSTANCE: MedicalDatabase? = null

        fun getDatabase(context: Context): MedicalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalDatabase::class.java,
                    "medical_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun getDefaultAdultCheckups(personName: String): List<Checkup> {
            return listOf(
                Checkup(
                    id = "dentist_checkup",
                    personName = personName,
                    title = "Zahnärztliche Vorsorge",
                    category = "Zahnmedizin",
                    description = "Halbjährliche Kontrolluntersuchung zur Vorbeugung von Zahn-, Mund- und Kieferkrankheiten.",
                    recommendedAge = "Ab 18 Jahren",
                    intervalMonths = 6,
                    gender = "ALL"
                ),
                Checkup(
                    id = "health_checkup_35",
                    personName = personName,
                    title = "Gesundheits-Check-up",
                    category = "Allgemeinmedizin",
                    description = "Früherkennung von Herz-Kreislauf-Erkrankungen, Nierenerkrankungen und Diabetes mellitus.",
                    recommendedAge = "Ab 35 Jahren (alle 3 Jahre)",
                    intervalMonths = 36,
                    gender = "ALL"
                ),
                Checkup(
                    id = "skin_cancer_screening",
                    personName = personName,
                    title = "Hautkrebs-Screening",
                    category = "Dermatologie",
                    description = "Gezielte Untersuchung der gesamten Haut auf Hautkrebserkrankungen.",
                    recommendedAge = "Ab 35 Jahren (alle 2 Jahre)",
                    intervalMonths = 24,
                    gender = "ALL"
                ),
                Checkup(
                    id = "cervical_cancer_screening",
                    personName = personName,
                    title = "Gebärmutterhalskrebs-Vorsorge",
                    category = "Gynäkologie",
                    description = "Untersuchung zur Früherkennung von Krebserkrankungen der Genitalorgane.",
                    recommendedAge = "Frauen ab 20 Jahren (jährlich)",
                    intervalMonths = 12,
                    gender = "F"
                ),
                Checkup(
                    id = "breast_cancer_screening",
                    personName = personName,
                    title = "Mammographie-Screening",
                    category = "Radiologie",
                    description = "Röntgenuntersuchung der Brust zur Früherkennung von Brustkrebs.",
                    recommendedAge = "Frauen von 50 bis 69 Jahren (alle 2 Jahre)",
                    intervalMonths = 24,
                    gender = "F"
                ),
                Checkup(
                    id = "prostate_cancer_screening",
                    personName = personName,
                    title = "Prostatakrebs-Vorsorge",
                    category = "Urologie",
                    description = "Abtastuntersuchung zur Früherkennung von Krebserkrankungen der Prostata.",
                    recommendedAge = "Männer ab 45 Jahren (jährlich)",
                    intervalMonths = 12,
                    gender = "M"
                ),
                Checkup(
                    id = "colon_cancer_screening",
                    personName = personName,
                    title = "Darmkrebs-Früherkennung",
                    category = "Gastroenterologie",
                    description = "Beratung, Stuhltest auf okkultes Blut oder Darmspiegelung (Koloskopie).",
                    recommendedAge = "Ab 50 Jahren",
                    intervalMonths = 60,
                    gender = "ALL"
                ),
                Checkup(
                    id = "vaccination_booster",
                    personName = personName,
                    title = "Tetanus, Diphtherie, Pertussis",
                    category = "Impfung",
                    description = "Auffrischimpfung gegen Wundstarrkrampf, Keuchhusten und Diphtherie.",
                    recommendedAge = "Alle 10 Jahre",
                    intervalMonths = 120,
                    gender = "ALL"
                )
            )
        }

        fun getDefaultChildCheckups(personName: String): List<Checkup> {
            return listOf(
                Checkup(
                    id = "dentist_child",
                    personName = personName,
                    title = "Zahnärztliche Früherkennung",
                    category = "Zahnmedizin",
                    description = "Halbjährlicher Zahn-Check für Kinder zur Kariesprophylaxe.",
                    recommendedAge = "Ab dem 1. Zahn (alle 6 Monate)",
                    intervalMonths = 6,
                    gender = "ALL"
                ),
                Checkup(
                    id = "u1_checkup",
                    personName = personName,
                    title = "U1-Untersuchung",
                    category = "Kinderheilkunde",
                    description = "Erstuntersuchung direkt nach der Geburt (Apgar-Score, Nabelschnur, erste Reaktionen).",
                    recommendedAge = "Direkt nach der Geburt",
                    intervalMonths = 0,
                    gender = "ALL"
                ),
                Checkup(
                    id = "u2_checkup",
                    personName = personName,
                    title = "U2-Untersuchung",
                    category = "Kinderheilkunde",
                    description = "Basis-Check (Organe, Hüfte, Reflexe, Blutabnahme).",
                    recommendedAge = "3. bis 10. Lebenstag",
                    intervalMonths = 0,
                    gender = "ALL"
                ),
                Checkup(
                    id = "u3_checkup",
                    personName = personName,
                    title = "U3-Untersuchung",
                    category = "Kinderheilkunde",
                    description = "Entwicklungs-Check (Trinkverhalten, Hüft-Ultraschall, Reflexe).",
                    recommendedAge = "4. bis 5. Lebenswoche",
                    intervalMonths = 0,
                    gender = "ALL"
                ),
                Checkup(
                    id = "u4_checkup",
                    personName = personName,
                    title = "U4-Untersuchung",
                    category = "Kinderheilkunde",
                    description = "Wachstum & Motorik (Kopfkontrolle, Bewegungen, Impfberatung).",
                    recommendedAge = "3. bis 4. Lebensmonat",
                    intervalMonths = 0,
                    gender = "ALL"
                ),
                Checkup(
                    id = "u5_checkup",
                    personName = personName,
                    title = "U5-Untersuchung",
                    category = "Kinderheilkunde",
                    description = "Körperbeherrschung (Umdrehen, Greifen, Seh- und Hörtest).",
                    recommendedAge = "6. bis 7. Lebensmonat",
                    intervalMonths = 0,
                    gender = "ALL"
                ),
                Checkup(
                    id = "u6_checkup",
                    personName = personName,
                    title = "U6-Untersuchung",
                    category = "Kinderheilkunde",
                    description = "Erstes Stehen & Krabbeln (Entwicklungsstand, Zähne).",
                    recommendedAge = "10. bis 12. Lebensmonat",
                    intervalMonths = 0,
                    gender = "ALL"
                ),
                Checkup(
                    id = "u7_checkup",
                    personName = personName,
                    title = "U7-Untersuchung",
                    category = "Kinderheilkunde",
                    description = "Sprach- & Spielentwicklung (Erste Wörter, freies Laufen).",
                    recommendedAge = "21. bis 24. Lebensmonat",
                    intervalMonths = 0,
                    gender = "ALL"
                ),
                Checkup(
                    id = "u7a_checkup",
                    personName = personName,
                    title = "U7a-Untersuchung",
                    category = "Kinderheilkunde",
                    description = "Zahn- & Kieferstellung, Sehtest, Sprachentwicklung.",
                    recommendedAge = "34. bis 36. Lebensmonat",
                    intervalMonths = 0,
                    gender = "ALL"
                ),
                Checkup(
                    id = "u8_checkup",
                    personName = personName,
                    title = "U8-Untersuchung",
                    category = "Kinderheilkunde",
                    description = "Selbstständigkeit & Motorik (Koordination, Hör- und Sehtest).",
                    recommendedAge = "46. bis 48. Lebensmonat",
                    intervalMonths = 0,
                    gender = "ALL"
                ),
                Checkup(
                    id = "u9_checkup",
                    personName = personName,
                    title = "U9-Untersuchung",
                    category = "Kinderheilkunde",
                    description = "Schulvorbereitung (Grobmotorik, Sprache, Sozialverhalten).",
                    recommendedAge = "60. bis 64. Lebensmonat",
                    intervalMonths = 0,
                    gender = "ALL"
                ),
                Checkup(
                    id = "j1_checkup",
                    personName = personName,
                    title = "J1-Jugenduntersuchung",
                    category = "Kinderheilkunde",
                    description = "Gesundheits-Check für Teenager (Wachstum, Haltung, Impfungen, Beratung).",
                    recommendedAge = "12 bis 14 Jahre",
                    intervalMonths = 0,
                    gender = "ALL"
                ),
                Checkup(
                    id = "j2_checkup",
                    personName = personName,
                    title = "J2-Jugenduntersuchung",
                    category = "Kinderheilkunde",
                    description = "Zusätzlicher Check für Jugendliche (Haltung, Schilddrüse, Allergien).",
                    recommendedAge = "16 bis 17 Jahre",
                    intervalMonths = 0,
                    gender = "ALL"
                )
            )
        }
    }
}
