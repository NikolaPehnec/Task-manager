package hr.fer.projekt.taskmanager.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


const val dbName = "baza TM"

class DatabaseHelper(val context: Context) : SQLiteOpenHelper(context, dbName, null, 1) {

    val tabZadatak = "zadatak"
    val zadId = "idZadatak"
    val zadNaziv = "naziv"
    val zadOpis = "opis"
    val zadDatumVrijeme = "datumVrijemeZadatak"
    val zadPonavljajuci = "ponavljajuci"
    val zadTrajanje = "trajanje"
    val zadPonavljanjeDani = "daniPonavljanja"
    val zadKorisnik = "idKorisnik"
    val zadStatus = "idStatus"
    val zadPopis = "idPopis"
    val zadGrupa = "idGrupa"

    val tabKorisnik = "korisnik"
    val korId = "idKorisnik"
    val korImePrezime = "imePrezime"

    val tabStatus = "status"
    val statusId = "idStatus"
    val statusOznaka = "oznaka"

    val tabPopisZadataka = "popis"
    val popisId = "idPopis"
    val popisIme = "imePopis"

    val tabGrupaZadataka = "grupaZadataka"
    val grupaId = "idGrupa"
    val grupaIme = "imeGrupa"
    val grupaBoja = "bojaGrupa"
    val grupaPrioritet = "prioritetGrupa"

    val tabObavijest = "obavijest"
    val obavijestDatumVrijeme = "datumVrijeme"
    val obavijestNapomena = "napomena"
    val obavijestIdAlarm = "alarm"
    val obavijestIdZadatak = "idZadatak"


    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(
            "CREATE TABLE " + tabZadatak + " (" + zadId + " INTEGER PRIMARY KEY, " + zadNaziv +
                    " TEXT, " + zadOpis + " TEXT, " + zadTrajanje + " TEXT, " + zadPonavljanjeDani + " TEXT, " +
                    zadPonavljajuci + " TEXT, " + zadDatumVrijeme + " TEXT, " + zadKorisnik + " INTEGER, " + zadStatus + " INTEGER, " +
                    zadPopis + " INTEGER, " + zadGrupa + " INTEGER, " +
                    "FOREIGN KEY (" + zadKorisnik + ") REFERENCES " + tabKorisnik + "(" + korId + ")," +
                    "FOREIGN KEY (" + zadPopis + ") REFERENCES " + tabPopisZadataka + "(" + popisId + ")," +
                    "FOREIGN KEY (" + zadStatus + ") REFERENCES " + tabStatus + "(" + statusId + ")," +
                    "FOREIGN KEY (" + zadGrupa + ") REFERENCES " + tabGrupaZadataka + "(" + grupaId + "));"
        )
        db.execSQL("CREATE TABLE $tabKorisnik ($korId INTEGER PRIMARY KEY, $korImePrezime TEXT );")
        db.execSQL("CREATE TABLE $tabStatus ($statusId INTEGER PRIMARY KEY, $statusOznaka TEXT );")
        db.execSQL(
            "CREATE TABLE $tabPopisZadataka ($popisId INTEGER PRIMARY KEY, $popisIme TEXT);"
        )
        db.execSQL(
            "CREATE TABLE $tabGrupaZadataka ($grupaId INTEGER PRIMARY KEY, $grupaIme TEXT, $grupaBoja TEXT, $grupaPrioritet INTEGER );"
        )
        db.execSQL(
            "CREATE TABLE $tabObavijest ($obavijestDatumVrijeme TEXT, $obavijestNapomena TEXT, $obavijestIdAlarm TEXT," +
                    " $obavijestIdZadatak INTEGER, FOREIGN KEY($obavijestIdZadatak) REFERENCES $tabZadatak ($zadId));"
        )

        ubaciPopise(db)
    }

    private fun ubaciPopise(db: SQLiteDatabase?) {
        val cv = ContentValues()
        cv.put(popisId, 1)
        cv.put(popisIme, "Popis 1")
        db!!.insert(tabPopisZadataka, popisId, cv)

        val cv2 = ContentValues()
        cv2.put(statusId, 1)
        cv2.put(statusOznaka, "Zadan")
        db.insert(tabStatus, statusId, cv2)

        val cv3 = ContentValues()
        cv3.put(statusId, 2)
        cv3.put(statusOznaka, "Završen")
        db.insert(tabStatus, statusId, cv3)

        val cv4 = ContentValues()
        cv4.put(grupaId, 1)
        cv4.put(grupaIme, "Hitno")
        cv4.put(grupaPrioritet, 1)
        cv4.put(grupaBoja, "#F2635F")
        db.insert(tabGrupaZadataka, grupaId, cv4)

        val cv5 = ContentValues()
        cv5.put(grupaId, 2)
        cv5.put(grupaIme, "Srednje")
        cv5.put(grupaPrioritet, 2)
        cv5.put(grupaBoja, "#0277BD")
        db.insert(tabGrupaZadataka, grupaId, cv5)

        val cv6 = ContentValues()
        cv6.put(grupaId, 3)
        cv6.put(grupaIme, "Odgodivo")
        cv6.put(grupaPrioritet, 3)
        cv6.put(grupaBoja, "#CCCCCC")
        db.insert(tabGrupaZadataka, grupaId, cv6)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    }

    public fun ZapisiPodatke(
        tablica: String?,
        polja: Array<String?>,
        vrijednosti: Array<String?>
    ): Long {
        val db = this.readableDatabase
        return try {
            val cv = ContentValues()
            for (i in polja.indices) {
                cv.put(polja[i], vrijednosti[i])
            }
            db.insert(tablica, null, cv)
        } catch (e: RuntimeException) {
            -1
        } finally {
            db.close()
        }
    }

    fun VratiPodatkeRaw(upit: String?): Cursor? {
        val db = this.writableDatabase
        return db.rawQuery(upit, null)
    }

    fun DeletePodatke(tablica: String?, whereDio: String?, whereVrijed: Array<String?>?) {
        val db = this.writableDatabase
        db.delete(tablica, whereDio, whereVrijed)
        db.close()
    }

    fun UpdatePodatke(
        tablica: String?,
        polja: Array<String?>,
        vrijednosti: Array<String>,
        whereDio: String?,
        whereVrijed: Array<String?>?
    ) {
        val db = this.writableDatabase
        val cv = ContentValues()
        for (i in polja.indices) {
            if (vrijednosti[i].compareTo("null") == 0) {
                cv.putNull(polja[i])
            } else {
                cv.put(polja[i], vrijednosti[i])
            }
        }
        db.update(tablica, cv, whereDio, whereVrijed)
        db.close()
    }

    fun izbrisiSveTabliceIAlarme() {
        val db = this.writableDatabase

        try {
            //Brisanje alarma
            var obavijestiZadatka: ArrayList<Obavijest> = ArrayList()
            val cursor = VratiPodatkeRaw("SELECT * FROM $tabObavijest")
            if (cursor != null && cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val alarmId = cursor.getInt(cursor.getColumnIndexOrThrow(obavijestIdAlarm))
                    val datumVrijeme =
                        cursor.getString(cursor.getColumnIndexOrThrow(obavijestDatumVrijeme))
                    val obavijest = Obavijest("", datumVrijeme, "")
                    obavijest.alarmId = alarmId
                    obavijestiZadatka.add(obavijest)
                }
            }
            for (obavijest in obavijestiZadatka) {
                obavijest.cancelAlarm(context)
            }

            //brisanje tablica
            val tables = arrayOf(
                tabZadatak,
                tabGrupaZadataka,
                tabPopisZadataka,
                tabObavijest,
                tabStatus,
                tabKorisnik
            )
            for (table in tables) {
                val dropQuery = "DELETE FROM $table"
                db.execSQL(dropQuery)
            }

            //inicijalno punjenje tablica
            ubaciPopise(db)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}