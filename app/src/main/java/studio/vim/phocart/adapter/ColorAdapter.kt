package studio.vim.phocart.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import studio.vim.phocart.R

class ColorAdapter(val listener : AdapterListener, private val context : Context, val items : List<Int?>) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    var isSelected : MutableList<Boolean> = mutableListOf()

    init {
        for(color in items){
            isSelected.add(false)
        }
    }

    private fun resetSelected(position : Int){
        isSelected.clear()
        items.forEachIndexed { index, color ->
            if(index == position){
                isSelected.add(true)
            }else{
                isSelected.add(false)
            }
        }
        notifyDataSetChanged()
    }

    inner class ColorViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        var ivColor: ImageView = itemView.findViewById(R.id.ivBgColor)
        var ivBorder: ImageView = itemView.findViewById(R.id.ivBorder)

        internal fun bind(position: Int) {
            items[position]?.let { ivColor.setColorFilter(it) }
            itemView.setOnClickListener {
                items[position]?.let { it1 -> listener.onColorSelected(it1) }
                resetSelected(position)
            }

            if(isSelected[position]){
                ivBorder.visibility = View.VISIBLE
            }else{
                ivBorder.visibility = View.GONE
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(LayoutInflater.from(context).inflate(R.layout.item_color, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(position)
    }

    interface AdapterListener{
        fun onColorSelected(color: Int)
    }
}