package hr.fer.projekt.taskmanager.adapters

import android.content.Context
import androidx.annotation.RequiresApi
import android.os.Build
import hr.fer.projekt.taskmanager.model.Zadatak
import hr.fer.projekt.taskmanager.adapters.ZadaciArrayAdapter.ZadatakItemClickListener
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import hr.fer.projekt.taskmanager.R
import androidx.core.content.ContextCompat
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import java.util.ArrayList

class ZadaciArrayAdapter @RequiresApi(api = Build.VERSION_CODES.O) constructor(
    var context: Context,
    items: List<Zadatak>?,
    dovrseniZadaci: Boolean,
    listener: ZadatakItemClickListener
) : RecyclerView.Adapter<ZadaciArrayAdapter.ViewHolder>() {
    private var zadaci: MutableList<Zadatak> = ArrayList()
    private val mItemClickListener: ZadatakItemClickListener
    private val dovrseniZadaci: Boolean

    init {
        zadaci.addAll(items!!)
        this.dovrseniZadaci = dovrseniZadaci
        mItemClickListener = listener
        sortZadaci()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.zadatak_row, viewGroup, false)
        return ViewHolder(view)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun sortZadaci() {
        zadaci.sortWith(java.util.Comparator { (_, _, _, _, _, _, _, grupa), (_, _, _, _, _, _, _, grupa1) ->
            if (grupa == null) {
                1
            } else if (grupa != null && grupa1 != null) {
                grupa.prioritet!!.compareTo(grupa1.prioritet!!)
            } else {
                -1
            }
        })
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (_, naziv, _, _, _, vrijeme, datum, grupa, ponavljajuciZadatak, aktualniZadatak) = zadaci[position]
        if (dovrseniZadaci) {
            holder.picDone.visibility = View.VISIBLE
            holder.picDone.isEnabled = true
            holder.picDone.animate().alpha(1f)
            holder.zadatakCB.visibility = View.GONE
            holder.picRepeat.visibility = View.GONE
        } else {
            if (!aktualniZadatak) {
                holder.cardView.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.colorLightGray2)
                holder.picRepeat.visibility = View.VISIBLE
            } else {
                holder.picRepeat.visibility = View.GONE
                holder.picDone.visibility = View.GONE
                holder.zadatakCB.visibility = View.VISIBLE
                holder.zadatakCB.isChecked = false
                holder.naziv.setTextColor(context.getColor(R.color.colorPrimary))
                holder.datum.setTextColor(context.getColor(R.color.colorPrimary))
                holder.vrijeme.setTextColor(context.getColor(R.color.colorPrimary))
                holder.cardView.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.colorWhite)
                if (grupa != null) {
                    holder.cardView.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor(grupa.boja))
                    holder.naziv.setTextColor(context.getColor(R.color.colorWhite))
                    holder.datum.setTextColor(context.getColor(R.color.colorWhite))
                    holder.vrijeme.setTextColor(context.getColor(R.color.colorWhite))
                } else {
                    holder.naziv.setTextColor(context.getColor(R.color.colorPrimary))
                    holder.datum.setTextColor(context.getColor(R.color.colorPrimary))
                    holder.vrijeme.setTextColor(context.getColor(R.color.colorPrimary))
                }
            }
        }
        if (ponavljajuciZadatak) holder.picRepeatMali.visibility =
            View.VISIBLE else holder.picRepeatMali.visibility = View.GONE
        holder.naziv.text = naziv
        if (datum != null && datum != "") {
            holder.layout.visibility = View.VISIBLE
            holder.datum.text = datum
            holder.vrijeme.text = vrijeme
        } else {
            holder.layout.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return zadaci.size
    }

    fun refillZadaci(zadaci: MutableList<Zadatak>) {
        this.zadaci = zadaci
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        zadaci.removeAt(position)
        notifyItemRemoved(position)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun addItem(zadatak: Zadatak) {
        zadaci.add(zadatak)
        //notifyItemInserted(zadaci.size() - 1);
        sortZadaci()
        notifyDataSetChanged()
    }

    fun listSize(): Int {
        return zadaci.size
    }

    interface ZadatakItemClickListener {
        fun onItemClick(zadatak: Zadatak?)
        fun onZadatakZavrsenClick(Zadatak: Zadatak?, position: Int)
        fun onPictureDoneClick(zadatak: Zadatak?, position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val naziv: TextView
        val datum: TextView
        val vrijeme: TextView
        val layout: LinearLayout
        val picDone: ImageView
        val picRepeatMali: ImageView
        val picRepeat: ImageView
        val cardView: CardView
        val zadatakCB: CheckBox

        init {
            naziv = view.findViewById(R.id.colNaziv)
            datum = view.findViewById(R.id.colDatum)
            vrijeme = view.findViewById(R.id.colVrijeme)
            layout = view.findViewById(R.id.layoutVrijemeDatum)
            picDone = view.findViewById(R.id.picDone)
            picRepeat = view.findViewById(R.id.picRepeat)
            picRepeatMali = view.findViewById(R.id.colRepeateImg)
            cardView = view.findViewById(R.id.zadatakCV)
            zadatakCB = view.findViewById(R.id.zadatakCB)
            view.setOnClickListener(this)
            zadatakCB.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    Handler().postDelayed({
                        mItemClickListener.onZadatakZavrsenClick(
                            zadaci[adapterPosition],
                            adapterPosition
                        )
                    }, 400)
                }
            }
            picDone.setOnClickListener {
                picDone.isEnabled = false
                picDone.animate().alpha(0f).duration = 500
                Handler().postDelayed({
                    mItemClickListener.onPictureDoneClick(
                        zadaci[adapterPosition],
                        adapterPosition
                    )
                }, 500)
            }
        }

        override fun onClick(v: View) {
            if (!dovrseniZadaci && zadaci[adapterPosition].aktualniZadatak) mItemClickListener.onItemClick(
                zadaci[adapterPosition]
            ) else if (!zadaci[adapterPosition].aktualniZadatak) Toast.makeText(
                context,
                "Ponavljanje zadatka koje će se aktivirati u budućnosti!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun daniUString(daniInteger: List<Int>): String {
        var dani = ""
        if (daniInteger.contains(1)) dani += "Pon "
        if (daniInteger.contains(2)) dani += "Uto "
        if (daniInteger.contains(3)) dani += "Sri "
        if (daniInteger.contains(4)) dani += "Čet "
        if (daniInteger.contains(5)) dani += "Pet "
        if (daniInteger.contains(6)) dani += "Sub "
        if (daniInteger.contains(7)) dani += "Ned"
        return dani
    }
}