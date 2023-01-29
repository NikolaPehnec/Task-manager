package hr.fer.projekt.taskmanager.adapters

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import hr.fer.projekt.taskmanager.R
import hr.fer.projekt.taskmanager.model.Grupa

class GrupaArrayAdapter @RequiresApi(api = Build.VERSION_CODES.O) constructor(
    context: Context?,
    grupe: List<Grupa?>?
) : ArrayAdapter<Grupa?>(
    context!!, 0, grupe!!
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView!!, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView!!, parent)
    }

    private fun initView(position: Int, convertView: View, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView =
                LayoutInflater.from(context).inflate(R.layout.grupa_spinner_item, parent, false)
        }
        val prioritet = convertView.findViewById<TextView>(R.id.grupaPrioritet)
        val naziv = convertView.findViewById<TextView>(R.id.grupaNaziv)
        val boja = convertView.findViewById<ImageView>(R.id.grupaBoja)
        val grupa = getItem(position)
        if (grupa != null) {
            prioritet.text = grupa.prioritet
            naziv.text = grupa.naziv
            boja.setBackgroundColor(Color.parseColor(grupa.boja!!))
        }
        return convertView
    }
}