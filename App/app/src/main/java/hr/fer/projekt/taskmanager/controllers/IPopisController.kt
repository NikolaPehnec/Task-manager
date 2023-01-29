package hr.fer.projekt.taskmanager.controllers

import hr.fer.projekt.taskmanager.model.Popis

interface IPopisController {
    fun onZapisiPopis(popis:Popis)
    fun getAllPopisi():List<Popis>
    fun deletePopisById(idPopis:String)
}