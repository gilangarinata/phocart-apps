package studio.vim.phocart

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import ja.burhanrashid52.photoeditor.PhotoEditor
import kotlinx.android.synthetic.main.activity_edit_photo2.*

import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import studio.vim.phocart.adapter.BackgroundAdapter
import studio.vim.phocart.adapter.ColorAdapter
import studio.vim.phocart.model.BackgroundModel
import java.net.HttpURLConnection
import java.net.URL

class EditPhotoActivity2 : AppCompatActivity(), ColorAdapter.AdapterListener, BackgroundAdapter.AdapterListener {

    var mPhotoEditor: PhotoEditor? = null
    private val colors = listOf(
            Color.argb(255, 255, 255, 255),
            Color.argb(132, 123, 222, 123),
            Color.argb(112, 112, 0, 122),
            Color.argb(123, 211, 123, 111),
            Color.argb(121, 122, 111, 211),
            Color.argb(121, 164, 234, 123),
            Color.argb(124, 125, 125, 142),
            Color.argb(112, 112, 0, 122),
            Color.argb(211, 211, 121, 76),
            Color.argb(123, 211, 211, 12),
    )

    private val background = listOf(
            BackgroundModel(R.drawable.bg_scene_2_free,null,R.drawable.display_scene_4_free,false),
            BackgroundModel(R.drawable.bg_scene_3_free,R.drawable.fg_scene_3_free,R.drawable.display_scene_5_free,false),
            BackgroundModel(R.drawable.bg_scene_1_free,null,R.drawable.display_scene_1_free,false),
            BackgroundModel(R.drawable.bg_scene_2_free,null,R.drawable.display_scene_2_free,false),
            BackgroundModel(R.drawable.bg_scene_3_free,R.drawable.fg_scene_3_free,R.drawable.display_scene_3_free,false),

    )

    private var cartoonUrl = ""
    private var convertedBitmap : Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo2)
        getDataBundle()
        init()

    }


    private fun getDataBundle(){
        cartoonUrl = intent.getStringExtra("cartoon").toString()
//        cartoonUrl = "http://35.194.39.36/uploads/final1615790883055.jpg.png"
    }


    private fun init(){
        photoEditorView.setBackgroundColor(colors[0])
        mPhotoEditor = PhotoEditor.Builder(this, photoEditorView)
                .setPinchTextScalable(true)
                .build()

        rvColorPallete.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        val colorAdapter = ColorAdapter(this,this, colors)
        rvColorPallete.adapter = colorAdapter

        rvFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        val backgroundAdapter = BackgroundAdapter(this,this,background)
        rvFilter.adapter = backgroundAdapter

        doAsync {
            val url = URL(cartoonUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            val myBitmap = BitmapFactory.decodeStream(input)
            convertedBitmap = myBitmap
            uiThread {
                convertedBitmap?.let {
                    mPhotoEditor?.addImage(it)
                }
            }
        }

    }


    override fun onColorSelected(color: Int) {
        mPhotoEditor?.clearForeGround()
        photoEditorView.source.setImageResource(0)
        photoEditorView.setBackgroundColor(color)
    }

    override fun onBackgroundSelected(background: BackgroundModel) {
        mPhotoEditor?.clearForeGround()
        photoEditorView.source.setImageResource(background.background)
        background.foreGround?.let {
            val foreground = BitmapFactory.decodeResource(resources,
                    it)
            mPhotoEditor?.addForeGround(foreground)
        }
    }

}