package studio.vim.phocart

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ja.burhanrashid52.photoeditor.PhotoEditor
import kotlinx.android.synthetic.main.activity_edit_photo.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import studio.vim.phocart.adapter.ColorAdapter
import studio.vim.phocart.utils.ImageUtils
import java.net.HttpURLConnection
import java.net.URL

class EditPhotoActivity : AppCompatActivity(), ColorAdapter.AdapterListener {

    var mPhotoEditor: PhotoEditor? = null
    val colors = listOf(
            Color.argb(255, 255, 255, 255),
            Color.argb(132, 123, 222, 123),
            Color.argb(112, 112, 0, 122),
            Color.argb(123, 211, 123, 111)
    )

    private var cartoonUrl = ""
    private var convertedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo)
        getDataBundle()
        init()
    }

    private fun getDataBundle(){
        cartoonUrl = intent.getStringExtra("cartoon").toString()
    }

    private fun init(){
        photoEditorView.setBackgroundColor(colors[0])
        mPhotoEditor = PhotoEditor.Builder(this, photoEditorView)
                .setPinchTextScalable(true)
                .build()

        rvColorPallete.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        val colorAdapter = ColorAdapter(this,this, colors)
        rvColorPallete.adapter = colorAdapter

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
        photoEditorView.setBackgroundColor(color)
    }


}