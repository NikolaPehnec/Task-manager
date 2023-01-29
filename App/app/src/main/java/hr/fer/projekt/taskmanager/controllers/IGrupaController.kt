package hr.fer.projekt.taskmanager.controllers

import hr.fer.projekt.taskmanager.model.Grupa

interface IGrupaController {
    fun onZapisiGrupa(grupa: Grupa)
    fun onUpdateGrupa(grupa: Grupa)
    fun getAllGrupe(): List<Grupa>
}