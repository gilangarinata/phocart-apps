package studio.vim.phocart

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.android.synthetic.main.activity_home.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import studio.vim.phocart.adapter.ListPhotoAdapter
import studio.vim.phocart.adapter.SpaceItemDecoration
import studio.vim.phocart.dialogs.BackDialog
import studio.vim.phocart.dialogs.RemoveBackgroundDialog
import studio.vim.phocart.model.PhotoModel
import studio.vim.phocart.utils.ImageFilePath
import studio.vim.phocart.utils.ImageUtils
import studio.vim.phocart.utils.PrefManager
import studio.vim.phocart.utils.Resource
import studio.vim.phocart.viewmodel.GalleryViewModel
import java.io.File
import java.io.IOException


class HomeActivity : AppCompatActivity(), ListPhotoAdapter.AdapterListener {

    private val adapter by lazy {
        ListPhotoAdapter(this, this, pictures)
    }

    private val galleryViewModel: GalleryViewModel by viewModels()

    private val pictures by lazy {
        ArrayList<PhotoModel>(galleryViewModel.getGallerySize(this))
    }

    private val CAMERA_REQUEST = 52
    private val PICK_REQUEST = 53

    var mCountDownTimer: CountDownTimer? = null
    var i = 20
    val timeout: Long = 60000
    val tick: Long = 500
    var isCamera = false
    private var mInterstitialAd: InterstitialAd? = null
    private var prefManager: PrefManager? = null

    override fun onBackPressed() {
        val backDialog = BackDialog()
        backDialog.showDialog(this, "Are you sure want to exit this app?", object : BackDialog.BackDialogListener {
            override fun onExitClicked() {
                this@HomeActivity.finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (prefManager == null) prefManager = PrefManager(this)
        prefManager?.let {
            val isPurchaseUser = it.isPurchaseUser()
            if (isPurchaseUser) {
                btnPro.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        prefManager = PrefManager(this)
        requestReadStoragePermission()
        requestWriteStoragePermission()
        initApiRequest()
        initProgressProcessing()
        if (!prefManager?.isPurchaseUser()!!) {
            initAds()
        }
        btnPro.setOnClickListener {
            startActivity(Intent(this, ProActivity::class.java))
        }
        btnSetting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        btnCamera.setOnClickListener {
            isCamera = true
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra("val", CAMERA_REQUEST)
            resultLauncher.launch(cameraIntent)
        }

        btnGallery.setOnClickListener {
            isCamera = false
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.putExtra("val", PICK_REQUEST)
            resultLauncher.launch(intent)
        }
    }

    private fun initAds() {
        containerAds.visibility = View.VISIBLE
        val adRequest2 = AdRequest.Builder().build()
        adViewTop.loadAd(adRequest2)

        var adRequest3 = AdRequest.Builder().build()
        InterstitialAd.load(this, "ca-app-pub-5314437672129370/5944280378", adRequest3, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("TAG", adError?.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("TAG", "Ad was loaded.")
                mInterstitialAd = interstitialAd
                if (mInterstitialAd != null) {
                    mInterstitialAd?.show(this@HomeActivity)
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.")
                }
            }
        })

        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d("TAG", "Ad was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Log.d("TAG", "Ad failed to show.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d("TAG", "Ad showed fullscreen content.")
                mInterstitialAd = null
            }
        }

    }

    fun getImageUri(inImage: Bitmap?): Uri? {
        val OutImage = Bitmap.createScaledBitmap(inImage!!, 1000, 1000, true)
        val path = MediaStore.Images.Media.insertImage(contentResolver, OutImage, "Title", null)
        return Uri.parse(path)
    }

    private fun getRealPathFromURI(uri: Uri?): String? {
        var path = ""
        if (contentResolver != null) {
            val cursor = contentResolver.query(uri!!, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let {
                if (isCamera) {
                    try {
                        val srcBmp = data.extras!!["data"] as Bitmap?
                        val photo = if (srcBmp!!.getWidth() >= srcBmp.getHeight()) {
                            Bitmap.createBitmap(
                                    srcBmp,
                                    srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                                    0,
                                    srcBmp.getHeight(),
                                    srcBmp.getHeight()
                            )
                        } else {
                            Bitmap.createBitmap(
                                    srcBmp,
                                    0,
                                    srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                                    srcBmp.getWidth(),
                                    srcBmp.getWidth()
                            )
                        }

                        val tempUri = getImageUri(photo)
                        val finalFile = File(getRealPathFromURI(tempUri))
                        val backgroundDialog = RemoveBackgroundDialog()
                        backgroundDialog.showDialog(this, "", object : RemoveBackgroundDialog.BackgroundDialogListener {
                            override fun onNoBackgroundSelected() {
                                selectPhoto(finalFile.toString(), false)
                            }

                            override fun onUseBackgroundSelected() {
                                selectPhoto(finalFile.toString(), true)
                            }

                        })

                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }else {
                    try {
                        val uri = data.data
                        val picturePath = ImageFilePath.getPath(this, uri)
                        val backgroundDialog = RemoveBackgroundDialog()
                        backgroundDialog.showDialog(this, "", object : RemoveBackgroundDialog.BackgroundDialogListener {
                            override fun onNoBackgroundSelected() {
                                selectPhoto(picturePath, false)
                            }

                            override fun onUseBackgroundSelected() {
                                selectPhoto(picturePath, true)
                            }

                        })
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
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
                progressBar.setProgress((i * 100 / (timeout / tick)).toInt())
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
        val pageSize = 250
        rvPhoto.layoutManager = layoutManager
        rvPhoto.addItemDecoration(SpaceItemDecoration(8))
        rvPhoto.adapter = adapter
        loadPictures(pageSize)
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            hideProgressProcessingImage()
        }
    }

    private fun initApiRequest() {
        galleryViewModel.homeData.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let {
                        (mCountDownTimer as CountDownTimer).onFinish()
                        (mCountDownTimer as CountDownTimer).cancel()
                        var intent = Intent(this, EditPhotoActivity2::class.java)
                        intent.putExtra("cartoon", it.path)
                        intent.putExtra("isok", true)
                        startForResult.launch(intent)
                    }
                }
                is Resource.Loading -> {

                }
                is Resource.Error -> {
                    Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
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
        val backgroundDialog = RemoveBackgroundDialog()
        backgroundDialog.showDialog(this, "", object : RemoveBackgroundDialog.BackgroundDialogListener {
            override fun onNoBackgroundSelected() {
                selectPhoto(photoModel.uri, false)
            }

            override fun onUseBackgroundSelected() {
                selectPhoto(photoModel.uri, true)
            }

        })
    }

    private fun selectPhoto(url: String?, isUsingBackground: Boolean) {
        try {
            val imagePath = ImageUtils(this).compressImage(url)
            val file = File(imagePath)
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            galleryViewModel.processData(body, isUsingBackground)
            Glide.with(this).load(imagePath).into(ivPreview)
            showProgressProcessingImage()
        } catch (e: Exception) {
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