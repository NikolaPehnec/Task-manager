package hr.fer.projekt.taskmanager.controllers

import android.content.Context
import android.widget.Toast
import hr.fer.projekt.taskmanager.model.DatabaseHelper
import hr.fer.projekt.taskmanager.model.Popis
import hr.fer.projekt.taskmanager.view.IView

class PopisController(
    val context: Context,
    val view: IView
) : IPopisController {

    private var dbHelper = DatabaseHelper(context)
    private val _view: IView = view

    override fun onZapisiPopis(popis: Popis) {
        try {
            dbHelper.ZapisiPodatke(
                dbHelper.tabPopisZadataka,
                arrayOf(
                    dbHelper.popisIme
                ),
                arrayOf(popis.naziv)
            )
            _view.onDataSaveSuccess()
        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
    }

    override fun getAllPopisi(): List<Popis> {
        var popisi = mutableListOf<Popis>()
        try {
            val cur = dbHelper.VratiPodatkeRaw("SELECT * FROM ${dbHelper.tabPopisZadataka}")
            if (cur != null && cur.count > 0) {
                while (cur.moveToNext()) {
                    val id = cur.getString(cur.getColumnIndexOrThrow(dbHelper.popisId))
                    val naziv = cur.getString(cur.getColumnIndexOrThrow(dbHelper.popisIme))
                    popisi.add(Popis(id, naziv))
                }
                popisi.sortWith(Comparator { t, t2 -> t.id.compareTo(t2.id) })
            }

        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
        return popisi
    }

    override fun deletePopisById(idPopis: String) {
        try {
            dbHelper.DeletePodatke(
                dbHelper.tabPopisZadataka, dbHelper.popisId + "= ?",
                arrayOf(idPopis)
            )
            _view.onDataSaveSuccess()
        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
    }
}