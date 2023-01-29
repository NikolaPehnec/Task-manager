package hr.fer.projekt.taskmanager.controllers

import android.content.Context
import android.widget.Toast
import hr.fer.projekt.taskmanager.model.DatabaseHelper
import hr.fer.projekt.taskmanager.model.Zadatak
import hr.fer.projekt.taskmanager.view.IView

class ZadatakController(
    val context: Context,
    val view: IView
) : IZadatakController {

    private var dbHelper = DatabaseHelper(context)
    private val _view: IView = view

    override fun onZapisiZadatak(zadatak: Zadatak) {
        try {
            dbHelper.ZapisiPodatke(
                dbHelper.tabZadatak,
                arrayOf(
                    dbHelper.zadNaziv,
                    dbHelper.zadOpis,
                    dbHelper.zadKorisnik,
                    dbHelper.zadStatus,
                    dbHelper.zadPopis,
                    dbHelper.zadGrupa,
                    dbHelper.zadDatumVrijeme,
                    dbHelper.zadPonavljajuci,
                    dbHelper.zadTrajanje,
                    dbHelper.zadPonavljanjeDani
                ),
                arrayOf(
                    zadatak.naziv,
                    zadatak.opis,
                    "1",
                    "1",
                    zadatak.popis,
                    zadatak.grupa!!.id,
                    zadatak.datum,
                    if (zadatak.ponavljajuciZadatak) "1" else "0",
                    zadatak.vrijeme,
                    zadatak.daniPonavljanja
                )
            )
            _view.onDataSaveSuccess()
        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
    }

    override fun deleteZadatakByID(zadatakId: String) {
        try {
            dbHelper.DeletePodatke(
                dbHelper.tabZadatak, dbHelper.zadId + "= ?",
                arrayOf(zadatakId)
            )
            _view.onDataSaveSuccess()
        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
    }

    override fun deleteZadatakByPopisID(popisId: String) {
        try {
            dbHelper.DeletePodatke(
                dbHelper.tabZadatak, dbHelper.zadPopis + "= ?",
                arrayOf(popisId)
            )
            _view.onDataSaveSuccess()
        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
    }

    override fun updateZadatakStatusZavrsen(zadatak: Zadatak) {
        try {
            dbHelper.UpdatePodatke(
                dbHelper.tabZadatak,
                arrayOf(dbHelper.zadStatus),
                arrayOf("2"),
                dbHelper.zadId + "=?",
                arrayOf(zadatak.id)
            )
            _view.onDataSaveSuccess()
        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
    }

    override fun updateZadatakStatusNeizvrsen(zadatak: Zadatak) {
        try {
            dbHelper.UpdatePodatke(
                dbHelper.tabZadatak,
                arrayOf(dbHelper.zadStatus),
                arrayOf("1"),
                dbHelper.zadId + "=?",
                arrayOf(zadatak.id)
            )
            _view.onDataSaveSuccess()
        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
    }

}