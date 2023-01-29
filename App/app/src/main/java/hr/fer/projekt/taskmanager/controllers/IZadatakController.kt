package hr.fer.projekt.taskmanager.controllers

import hr.fer.projekt.taskmanager.model.Zadatak

interface IZadatakController {
    fun onZapisiZadatak(zadatak: Zadatak)
    fun deleteZadatakByID(zadatakId: String)
    fun deleteZadatakByPopisID(popisId: String)
    fun updateZadatakStatusZavrsen(zadatak: Zadatak)
    fun updateZadatakStatusNeizvrsen(zadatak: Zadatak)
}