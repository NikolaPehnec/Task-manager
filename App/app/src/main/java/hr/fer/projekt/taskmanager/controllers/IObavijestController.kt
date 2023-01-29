package hr.fer.projekt.taskmanager.controllers

import hr.fer.projekt.taskmanager.model.Obavijest
import hr.fer.projekt.taskmanager.model.Zadatak

interface IObavijestController {
    fun onZapisiObavijest(obavijest:Obavijest)
    fun updateObavijestStatusGotovo(zadatak: Zadatak)
    fun updateObavijestStatusNijeGotovo(zadatak: Zadatak)
    fun deleteObavijestByZadatakID(zadatakId: String)

}