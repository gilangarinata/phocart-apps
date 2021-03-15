package studio.vim.phocart

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_home.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import studio.vim.phocart.adapter.ListPhotoAdapter
import studio.vim.phocart.adapter.SpaceItemDecoration
import studio.vim.phocart.model.PhotoModel
import studio.vim.phocart.utils.ImageUtils
import studio.vim.phocart.utils.Resource
import studio.vim.phocart.viewmodel.GalleryViewModel
import java.io.File


class HomeActivity : AppCompatActivity(), ListPhotoAdapter.AdapterListener {

    private val adapter by lazy {
        ListPhotoAdapter(this,this, pictures)
    }

    private val galleryViewModel: GalleryViewModel by viewModels()

    private val pictures by lazy {
        ArrayList<PhotoModel>(galleryViewModel.getGallerySize(this))
    }

    var mCountDownTimer: CountDownTimer? = null
    var i = 20
    val timeout : Long = 60000
    val tick : Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        requestReadStoragePermission()
        requestWriteStoragePermission()
        initApiRequest()
        initProgressProcessing()
    }

    private fun initProgressProcessing(){
        tvCancel.setOnClickListener {
            hideProgressProcessingImage()
            i = 20
        }
    }

    private fun showProgressProcessingImage(){
        lytProcess.visibility = View.VISIBLE
        i = 20
        progressBar.setProgress(i)
        mCountDownTimer = object : CountDownTimer(timeout, tick) {
            override fun onTick(millisUntilFinished: Long) {
                i++
                progressBar.setProgress((i * 100 / (timeout/tick)).toInt())
            }

            override fun onFinish() {
                i = 20
                progressBar.setProgress(100)
            }
        }
        (mCountDownTimer as CountDownTimer).start()
    }

    private fun hideProgressProcessingImage(){
        lytProcess.visibility = View.GONE
    }

    private fun requestReadStoragePermission() {
        val readStorage = Manifest.permission.READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                        this,
                        readStorage
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(readStorage), 3)
        } else init()
    }

    private fun requestWriteStoragePermission() {
        val writeStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                        this,
                        writeStorage
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(writeStorage), 3)
        }
    }

    private fun init() {
        val layoutManager = GridLayoutManager(this, 3)
        val pageSize = 20
        rvPhoto.layoutManager = layoutManager
        rvPhoto.addItemDecoration(SpaceItemDecoration(8))
        rvPhoto.adapter = adapter
        loadPictures(pageSize)
    }

    private fun initApiRequest(){
        galleryViewModel.homeData.observe(this, Observer {response ->
            when(response){
                is Resource.Success -> {
                    response.data?.let {
                        (mCountDownTimer as CountDownTimer).onFinish()
                        (mCountDownTimer as CountDownTimer).cancel()
                        var intent = Intent(this,EditPhotoActivity::class.java)
                        intent.putExtra("cartoon",it.path)
                        startActivity(intent)
                    }
                }
                is Resource.Loading -> {

                }
                is Resource.Error -> {
                    Toast.makeText(this,response.message,Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun loadPictures(pageSize: Int) {
        galleryViewModel.getImagesFromGallery(this, pageSize) {
            if (it.isNotEmpty()) {
                pictures.addAll(it)
                adapter.notifyItemRangeInserted(pictures.size, it.size)
            }
            Log.i("GalleryListSize", "${pictures.size}")
        }
    }

    override fun onPhotoSelected(photoModel: PhotoModel) {
        try{
            val imagePath = ImageUtils(this).compressImage(photoModel.uri)
            val file = File(imagePath)
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file",file.name,requestFile)
            galleryViewModel.processData(body)
            Glide.with(this).load(imagePath).into(ivPreview)
            showProgressProcessingImage()
        }catch (e : Exception){
            e.message?.let { Log.e("ERRCONV", it) }
        }

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            init()
        else {
            super.onBackPressed()
        }
    }

}