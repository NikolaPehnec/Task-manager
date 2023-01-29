package hr.fer.projekt.taskmanager.controllers

import android.content.Context
import hr.fer.projekt.taskmanager.model.DatabaseHelper
import hr.fer.projekt.taskmanager.model.Obavijest
import hr.fer.projekt.taskmanager.model.Zadatak
import hr.fer.projekt.taskmanager.view.IView

class ObavijestController(
    val context: Context,
    val view: IView
) : IObavijestController {

    private var dbHelper = DatabaseHelper(context)
    private val _view: IView = view

    override fun onZapisiObavijest(obavijest: Obavijest) {
        try {
            dbHelper.ZapisiPodatke(
                dbHelper.tabObavijest,
                arrayOf(
                    dbHelper.obavijestIdZadatak,
                    dbHelper.obavijestDatumVrijeme,
                    dbHelper.obavijestNapomena,
                    dbHelper.obavijestIdAlarm
                ),
                arrayOf(
                    obavijest.idZadatak,
                    obavijest.datumVrijeme,
                    obavijest.napomena,
                    obavijest.idAlarm
                )
            )
            _view.onDataSaveSuccess()
        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
    }

    override fun updateObavijestStatusGotovo(zadatak: Zadatak) {
        try {
            dbHelper.UpdatePodatke(
                dbHelper.tabObavijest,
                arrayOf(dbHelper.obavijestNapomena),
                arrayOf("gotovo"),
                dbHelper.obavijestIdZadatak + "=? AND " + dbHelper.obavijestDatumVrijeme + "=?",
                arrayOf(zadatak.id, zadatak.datum)
            )
            _view.onDataSaveSuccess()
        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
    }

    override fun updateObavijestStatusNijeGotovo(zadatak: Zadatak) {
        try {
            dbHelper.UpdatePodatke(
                dbHelper.tabObavijest,
                arrayOf(dbHelper.obavijestNapomena),
                arrayOf("prvi"),
                dbHelper.obavijestIdZadatak + "=? AND " + dbHelper.obavijestDatumVrijeme + "=?",
                arrayOf(zadatak.id, zadatak.datum)
            )
            _view.onDataSaveSuccess()
        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
    }

    override fun deleteObavijestByZadatakID(zadatakId: String) {
        try {
            dbHelper.DeletePodatke(
                dbHelper.tabObavijest,
                dbHelper.obavijestIdZadatak + "=?",
                arrayOf(zadatakId)
            )
            _view.onDataSaveSuccess()
        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
    }
}