package studio.vim.phocart.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import studio.vim.phocart.R
import studio.vim.phocart.model.BackgroundModel

class BackgroundAdapter(val listener : AdapterListener, private val context : Context, val items : List<BackgroundModel?>) : RecyclerView.Adapter<BackgroundAdapter.BackgroundViewHolder>() {

    var isSelected : MutableList<Boolean> = mutableListOf()

    init {
        for(item in items){
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

    inner class BackgroundViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        var ivColor: ImageView = itemView.findViewById(R.id.ivBgColor)
        var ivBorder: ImageView = itemView.findViewById(R.id.ivBorder)

        internal fun bind(position: Int) {
            items[position]?.let { it.display?.let { it1 -> ivColor.setImageResource(it1) } }
            itemView.setOnClickListener {
                items[position]?.let { it1 -> listener.onBackgroundSelected(it1) }
                resetSelected(position)
            }

            if(isSelected[position]){
                ivBorder.visibility = View.VISIBLE
            }else{
                ivBorder.visibility = View.GONE
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackgroundViewHolder {
        return BackgroundViewHolder(LayoutInflater.from(context).inflate(R.layout.item_background, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: BackgroundViewHolder, position: Int) {
        holder.bind(position)
    }

    interface AdapterListener{
        fun onBackgroundSelected(background: BackgroundModel)
    }
}