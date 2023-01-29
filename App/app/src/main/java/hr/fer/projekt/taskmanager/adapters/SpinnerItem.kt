package hr.fer.projekt.taskmanager.adapters

class SpinnerItem(id: String, name: String, dodatno:String) {

    val id: String = id
    val name: String = name
    val dodatno: String = name

    override fun toString(): String {
        return name
    }
}