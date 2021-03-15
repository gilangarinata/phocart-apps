package studio.vim.phocart.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import studio.vim.phocart.R
import studio.vim.phocart.model.PhotoModel

class ListPhotoAdapter(val listener : AdapterListener, val context : Context, val items : List<PhotoModel?>) : RecyclerView.Adapter<ListPhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        internal var ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)

        internal fun bind(position: Int) {
            ivPhoto.setOnClickListener {
                items[position]?.let {
                    listener.onPhotoSelected(it)
                }
            }
            items[position]?.let {
                Glide.with(context).load(it.uri).into(ivPhoto)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(position)
    }

    interface AdapterListener{
        fun onPhotoSelected(photoModel: PhotoModel)
    }
}