package hr.fer.projekt.taskmanager.view

interface IView {
    fun onDatabaseError(message:String)
    fun onDataSaveSuccess()
}