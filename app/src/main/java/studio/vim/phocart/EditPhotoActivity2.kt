package studio.vim.phocart

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditor.OnSaveListener
import ja.burhanrashid52.photoeditor.SaveSettings
import kotlinx.android.synthetic.main.activity_edit_photo2.*
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import studio.vim.phocart.adapter.BackgroundAdapter
import studio.vim.phocart.adapter.ColorAdapter
import studio.vim.phocart.adapter.ToolGradientAdapter
import studio.vim.phocart.dialogs.BackDialog
import studio.vim.phocart.model.BackgroundModel
import studio.vim.phocart.utils.PrefManager
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

open class EditPhotoActivity2 : AppCompatActivity(), ColorAdapter.AdapterListener, BackgroundAdapter.AdapterListener, ToolGradientAdapter.AdapterListener {

    val FILE_PROVIDER_AUTHORITY = "studio.vim.phocart.provider"
    private var mInterstitialAd: InterstitialAd? = null

    @VisibleForTesting
    var mSaveImageUri: Uri? = null
    var mPhotoEditor: PhotoEditor? = null

    private var countAction = 0

    private val colors = listOf(
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#F7F6CF"),
            Color.parseColor("#B6D8F2"),
            Color.parseColor("#F4CFDF"),
            Color.parseColor("#5784BA"),
            Color.parseColor("#9AC8EB"),
            Color.parseColor("#CCD4BF"),
            Color.parseColor("#E7CBA9"),
            Color.parseColor("#EEBAB2"),
            Color.parseColor("#F5BFD2"),
            Color.parseColor("#BEB4C5"),
            Color.parseColor("#E6A57E"),
            Color.parseColor("#9AD9DB"),
            Color.parseColor("#98D4BB"),

            )

    private val gradients = mutableMapOf<Int, List<Int>>(
            0 to listOf(Color.parseColor("#ffafbd"), Color.parseColor("#ffc3a0")),
            1 to listOf(Color.parseColor("#2193b0"), Color.parseColor("#6dd5ed")),
            2 to listOf(Color.parseColor("#cc2b5e"), Color.parseColor("#753a88")),
            3 to listOf(Color.parseColor("#ee9ca7"), Color.parseColor("#ffdde1")),
            4 to listOf(Color.parseColor("#42275a"), Color.parseColor("#734b6d")),
            5 to listOf(Color.parseColor("#bdc3c7"), Color.parseColor("#2c3e50")),
            6 to listOf(Color.parseColor("#de6262"), Color.parseColor("#ffb88c")),
            7 to listOf(Color.parseColor("#06beb6"), Color.parseColor("#48b1bf")),
            5 to listOf(Color.parseColor("#eb3349"), Color.parseColor("#f45c43")),
            6 to listOf(Color.parseColor("#dd5e89"), Color.parseColor("#f7bb97")),
            7 to listOf(Color.parseColor("#56ab2f"), Color.parseColor("#a8e063")),
            6 to listOf(Color.parseColor("#614385"), Color.parseColor("#516395")),
            7 to listOf(Color.parseColor("#eecda3"), Color.parseColor("#ef629f")),
    )

    private val background = listOf(
            BackgroundModel(R.drawable.bg_scene_2_free, null, R.drawable.display_scene_4_free, false),
            BackgroundModel(R.drawable.bg_scene_3_free, R.drawable.fg_scene_3_free, R.drawable.display_scene_5_free, false),
            BackgroundModel(R.drawable.bg_scene_1_free, null, R.drawable.display_scene_1_free, false),
            BackgroundModel(R.drawable.bg_scene_2_free, null, R.drawable.display_scene_2_free, false),
            BackgroundModel(R.drawable.scene_6, null, R.drawable.display_6, false),
            BackgroundModel(R.drawable.scene_7, null, R.drawable.display_7, false),
            BackgroundModel(R.drawable.scene_8, null, R.drawable.display_8, false),
            BackgroundModel(R.drawable.scene_9, null, R.drawable.display_9, false),
    )

    private fun initAds() {
        containerAds.visibility = View.VISIBLE
        val adRequest2 = AdRequest.Builder().build()
        adViewTop.loadAd(adRequest2)
    }

    private fun showInterstititalAds() {
        if (!prefManager.isPurchaseUser()) {
            var adRequest3 = AdRequest.Builder().build()
            InterstitialAd.load(this, "ca-app-pub-5314437672129370/6869159865", adRequest3, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("TAG", adError?.message)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("TAG", "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(this@EditPhotoActivity2)
                    } else {
                        Log.d("TAG", "The interstitial ad wasn't ready yet.")
                    }
                }
            })
        }
    }

    private var cartoonUrl = ""
    private var convertedBitmap: Bitmap? = null
    private lateinit var prefManager: PrefManager

    private fun showProAccount() {
        startActivity(Intent(this, ProActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo2)
        prefManager = PrefManager(this)
        if (!prefManager.isPurchaseUser()) {
            initAds()
        }
        showInterstititalAds()
        getDataBundle()
        init()
        btnSave.setOnClickListener {
            saveImage(false)
        }
        btnShare.setOnClickListener {
            saveImage(true)
        }
    }

    private fun shareImage() {
        if (mSaveImageUri == null) {
            showSnackbar("Please save image to share.")
            return
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, buildFileProviderUri(mSaveImageUri!!))
        startActivity(Intent.createChooser(intent, "Share Your Photo"))
    }

    private fun buildFileProviderUri(uri: Uri): Uri? {
        return FileProvider.getUriForFile(this,
                FILE_PROVIDER_AUTHORITY,
                File(uri.path))
    }


    private fun getDataBundle() {
        cartoonUrl = intent.getStringExtra("cartoon").toString()
//        cartoonUrl = "http://35.194.39.36/uploads/final1615790883055.jpg.png"
    }


    private fun init() {
        photoEditorView.setBackgroundColor(colors[0])
        mPhotoEditor = PhotoEditor.Builder(this, photoEditorView)
                .setPinchTextScalable(true)
                .build()

        ivBack.setOnClickListener { this.onBackPressed() }

        rvColorPallete.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val colorAdapter = ColorAdapter(this, this, colors, null)
        rvColorPallete.adapter = colorAdapter

        rvFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val backgroundAdapter = BackgroundAdapter(this, this, background)
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

    override fun onColorGradientSelected(gradients: List<Int>) {
        lytOtherTools.visibility = View.VISIBLE
        rvOtherTools.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        var mapGradients = mutableMapOf(
                0 to gradients,
                1 to gradients,
                2 to gradients,
                3 to gradients,
                4 to gradients,
                5 to gradients,
                6 to gradients,
                7 to gradients,
        )

        val adapter = ToolGradientAdapter(this, this, mapGradients)
        rvOtherTools.adapter = adapter

        val gd = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(gradients[0], gradients.get(1)))
        gd.cornerRadius = 0f
        photoEditorView.source.setImageResource(0)
        photoEditorView.backgroundDrawable = gd
    }

    override fun onBackPressed() {
        val backDialog = BackDialog()
        backDialog.showDialog(this, "Discard Changes?", object : BackDialog.BackDialogListener {
            override fun onExitClicked() {
                this@EditPhotoActivity2.finish()
            }
        })
    }

    override fun onBackgroundSelected(background: BackgroundModel, pos: Int) {
        if (countAction > 4) {
            showInterstititalAds()
            countAction = 0
        }
        when (pos) {
            0 -> {
                lytOtherTools.visibility = View.GONE
                val colorAdapter = ColorAdapter(this, this, colors, null)
                rvColorPallete.adapter = colorAdapter
                rvColorPallete.visibility = View.VISIBLE
            }
            1 -> {
                val colorAdapter = ColorAdapter(this, this, colors, gradients)
                rvColorPallete.adapter = colorAdapter
                rvColorPallete.visibility = View.VISIBLE
            }
            else -> {
                lytOtherTools.visibility = View.GONE
                rvColorPallete.visibility = View.GONE
                mPhotoEditor?.clearForeGround()
                photoEditorView.source.setImageResource(background.background)
                background.foreGround?.let {
                    val foreground = BitmapFactory.decodeResource(resources,
                            it)
                    mPhotoEditor?.addForeGround(foreground)
                }
            }
        }

        countAction++

    }

    override fun onGradientToolSelected(orientation: GradientDrawable.Orientation, colors: List<Int>) {
        val gd = GradientDrawable(
                orientation, intArrayOf(colors[0], colors[1]))
        gd.cornerRadius = 0f
        photoEditorView.source.setImageResource(0)
        photoEditorView.backgroundDrawable = gd
    }

    private val READ_WRITE_STORAGE = 52
    private fun requestPermission(permission: String): Boolean {
        val isGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(permission),
                    READ_WRITE_STORAGE)
        }
        return isGranted
    }

    private var mProgressDialog: ProgressDialog? = null
    protected fun showLoading(message: String) {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setMessage(message)
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.show()
    }

    protected open fun hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    @SuppressLint("MissingPermission")
    private fun saveImage(isShare: Boolean) {
        if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showLoading("Saving...")
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + File.separator + ""
                    + System.currentTimeMillis() + ".png")
            try {
                file.createNewFile()
                val saveSettings = SaveSettings.Builder()
                        .setClearViewsEnabled(true)
                        .setTransparencyEnabled(true)
                        .build()
                mPhotoEditor!!.saveAsFile(file.absolutePath, saveSettings, object : OnSaveListener {
                    override fun onSuccess(imagePath: String) {
                        hideLoading()
                        showSnackbar("Image Saved Successfully")
                        mSaveImageUri = Uri.fromFile(File(imagePath))
                        photoEditorView.source.setImageURI(mSaveImageUri)
                        galleryAddPic(this@EditPhotoActivity2, file.absolutePath)
                        if (isShare) {
                            shareImage()
                        }
                    }

                    override fun onFailure(exception: Exception) {
                        hideLoading()
                        showSnackbar("Failed to save Image")
                    }
                })
            } catch (e: IOException) {
                e.printStackTrace()
                hideLoading()
                e.message?.let { showSnackbar(it) }
            }
        }
    }

    open fun galleryAddPic(context: Context, imagePath: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(imagePath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
    }

    protected open fun showSnackbar(message: String) {
        val view = findViewById<View>(android.R.id.content)
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }


}