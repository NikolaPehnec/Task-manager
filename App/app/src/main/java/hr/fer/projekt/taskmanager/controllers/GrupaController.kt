package hr.fer.projekt.taskmanager.controllers

import android.content.Context
import android.widget.Toast
import hr.fer.projekt.taskmanager.model.DatabaseHelper
import hr.fer.projekt.taskmanager.model.Grupa
import hr.fer.projekt.taskmanager.view.IView

class GrupaController(
    val context: Context,
    val view:IView
) : IGrupaController {

    private var dbHelper = DatabaseHelper(context)
    private val _view:IView=view

    override fun onZapisiGrupa(grupa: Grupa) {
        try {
            dbHelper.ZapisiPodatke(
                dbHelper.tabGrupaZadataka,
                arrayOf(dbHelper.grupaIme, dbHelper.grupaBoja, dbHelper.grupaPrioritet),
                arrayOf(grupa.naziv, grupa.boja, grupa.prioritet)
            )
            _view.onDataSaveSuccess()
        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
    }

    override fun onUpdateGrupa(grupa: Grupa) {
        try {
            dbHelper.UpdatePodatke(
                dbHelper.tabGrupaZadataka,
                arrayOf(dbHelper.grupaIme, dbHelper.grupaBoja),
                arrayOf(grupa.naziv!!, grupa.boja!!),
                dbHelper.grupaId + "=?",
                arrayOf(grupa.id)
            )
            _view.onDataSaveSuccess()
        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
    }

    override fun getAllGrupe(): List<Grupa> {
        var grupe= mutableListOf<Grupa>()
        try {
            val cur = dbHelper.VratiPodatkeRaw("SELECT * FROM ${dbHelper.tabGrupaZadataka}")
            if (cur != null && cur.count > 0) {
                while (cur.moveToNext()) {
                    val id = cur.getString(cur.getColumnIndexOrThrow(dbHelper.grupaId))
                    val naziv = cur.getString(cur.getColumnIndexOrThrow(dbHelper.grupaIme))
                    val boja = cur.getString(cur.getColumnIndexOrThrow(dbHelper.grupaBoja))
                    val prioritet =
                        cur.getString(cur.getColumnIndexOrThrow(dbHelper.grupaPrioritet))
                    grupe.add(Grupa(id, naziv, boja, prioritet))
                }
                grupe.sortWith(Comparator { t, t2 -> t.prioritet!!.compareTo(t2.prioritet!!) })
            }

        } catch (e: Exception) {
            System.err.print(e.message)
            _view.onDatabaseError(e.message!!)
        }
        return grupe
    }


}