package hr.fer.projekt.taskmanager.adapters

import android.content.Context
import hr.fer.projekt.taskmanager.adapters.VremenaArrayAdapter.ItemClickListener
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import hr.fer.projekt.taskmanager.R
import android.widget.TextView

class VremenaArrayAdapter(
    var context: Context,
    private val vremena: MutableList<String>,
    private var mItemClickListener: ItemClickListener
) : RecyclerView.Adapter<VremenaArrayAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.vrijeme_row, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val v = vremena[position]
        holder.vrijeme.text = v
    }

    fun changeTimeAtIndex(newTime: String, index: Int) {
        vremena[index] = newTime
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return vremena.size
    }

    fun addItemClickListener(listener: ItemClickListener) {
        mItemClickListener = listener
    }

    interface ItemClickListener {
        fun onItemClick(vrijeme: String?, position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        val vrijeme: TextView

        init {
            vrijeme = view.findViewById(R.id.colVrijeme)
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val vrijeme = vremena[adapterPosition]
            mItemClickListener.onItemClick(vrijeme, adapterPosition)
        }
    }
}