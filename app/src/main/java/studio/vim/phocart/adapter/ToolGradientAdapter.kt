package studio.vim.phocart.adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.backgroundDrawable
import studio.vim.phocart.R


class ToolGradientAdapter(val listener: AdapterListener, private val context: Context, val items: MutableMap<Int, List<Int>>) : RecyclerView.Adapter<ToolGradientAdapter.ColorViewHolder>() {

    var isSelected: MutableList<Boolean> = mutableListOf()

    init {
        for (color in items) {
            isSelected.add(false)
        }
    }

    private fun resetSelected(position: Int) {
        isSelected.clear()
        items.forEach {
            if (it.key == position) {
                isSelected.add(true)
            } else {
                isSelected.add(false)
            }
        }
        notifyDataSetChanged()
    }

    inner class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var ivColor: ImageView = itemView.findViewById(R.id.ivBgColor)
        var ivBorder: ImageView = itemView.findViewById(R.id.ivBorder)
        var ivColorGradient: ImageView = itemView.findViewById(R.id.ivBgColorGradient)
        var ivBorderGradient: ImageView = itemView.findViewById(R.id.ivBorderGradient)

        internal fun bind(position: Int) {
            if (items != null) {
                ivColor.visibility = View.GONE
                ivBorder.visibility = View.GONE
                ivColorGradient.visibility = View.VISIBLE

                val orientation = when (position) {
                    0 -> {
                        GradientDrawable.Orientation.LEFT_RIGHT
                    }
                    1 -> {
                        GradientDrawable.Orientation.TOP_BOTTOM
                    }
                    2 -> {
                        GradientDrawable.Orientation.BOTTOM_TOP
                    }
                    3 -> {
                        GradientDrawable.Orientation.RIGHT_LEFT
                    }
                    4 -> {
                        GradientDrawable.Orientation.BL_TR
                    }
                    5 -> {
                        GradientDrawable.Orientation.BR_TL
                    }
                    6 -> {
                        GradientDrawable.Orientation.TL_BR
                    }
                    else -> {
                        GradientDrawable.Orientation.TR_BL
                    }
                }

                val gd = GradientDrawable(
                        orientation, intArrayOf(items[position]!!.get(0)!!, items[position]!!.get(1)!!))
                gd.cornerRadius = 0f
                ivColorGradient.backgroundDrawable = gd
                itemView.setOnClickListener {
                    listener.onGradientToolSelected(gd.orientation, items[position]!!)
                    resetSelected(position)
                }
                if (isSelected[position]) {
                    ivBorderGradient.visibility = View.VISIBLE
                } else {
                    ivBorderGradient.visibility = View.GONE
                }
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

    interface AdapterListener {
        fun onGradientToolSelected(orientation: GradientDrawable.Orientation, colors: List<Int>)
    }
}