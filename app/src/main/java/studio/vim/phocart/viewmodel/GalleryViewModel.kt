package studio.vim.phocart.viewmodel

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Response
import studio.vim.phocart.model.PhotoModel
import studio.vim.phocart.network.api.ApiResponse
import studio.vim.phocart.network.api.repository.HomeRepository
import studio.vim.phocart.utils.Resource
import java.util.*

class GalleryViewModel : ViewModel() {
    private val compositeDisposable = CompositeDisposable()
    private var startingRow = 0
    private var rowsToLoad = 0
    private var allLoaded = false
    private val homeRepository = HomeRepository()

    val homeData: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()

    fun getImagesFromGallery(context: Context, pageSize: Int, list: (List<PhotoModel>) -> Unit) {
        compositeDisposable.add(
                Single.fromCallable {
                    fetchGalleryImages(context, pageSize)
                }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            list(it)
                        }, {
                            it.printStackTrace()
                        })
        )
    }

    fun processData(file : MultipartBody.Part) = viewModelScope.launch {
        homeData.postValue(Resource.Loading())
        val response = homeRepository.getHomeData(file)
        homeData.postValue(handleHomeDataResponse(response))
    }

    private fun handleHomeDataResponse(response: Response<ApiResponse>): Resource<ApiResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                return if(it.code == 2000){
                    Resource.Success(it)
                }else{
                    Resource.Error(it.code,it.message)
                }

            }
        }
        return Resource.Error(500,response.message())
    }


    fun getGallerySize(context: Context): Int {
        val columns = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID) //get all columns of type images
        val orderBy = MediaStore.Images.Media.DATE_TAKEN //order data by date
        val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns,
                null,
                null,
                "$orderBy DESC"
        ) //get all data in Cursor by sorting in DESC order
        val rows = cursor!!.count
        cursor.close()
        return rows
    }

    private fun fetchGalleryImages(context: Context, rowsPerLoad: Int): List<PhotoModel> {
        val galleryImageUrls = LinkedList<PhotoModel>()
        val columns = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID) //get all columns of type images
        val orderBy = MediaStore.Images.Media.DATE_TAKEN //order data by date

        val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, "$orderBy DESC"
        ) //get all data in Cursor by sorting in DESC order

        Log.i("GalleryAllLoaded", "$allLoaded")

        if (cursor != null && !allLoaded) {
            val totalRows = cursor.count
            allLoaded = rowsToLoad == totalRows
            if (rowsToLoad < rowsPerLoad) {
                rowsToLoad = rowsPerLoad
            }

            for (i in startingRow until rowsToLoad) {
                cursor.moveToPosition(i)
                val dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA) //get column index
                galleryImageUrls.add(PhotoModel(cursor.getString(dataColumnIndex))) //get Image from column index

            }
            Log.i("TotalGallerySize", "$totalRows")
            Log.i("GalleryStart", "$startingRow")
            Log.i("GalleryEnd", "$rowsToLoad")
            startingRow = rowsToLoad

            if (rowsPerLoad > totalRows || rowsToLoad >= totalRows)
                rowsToLoad = totalRows
            else {
                if (totalRows - rowsToLoad <= rowsPerLoad)
                    rowsToLoad = totalRows
                else
                    rowsToLoad += rowsPerLoad
            }

            cursor.close()
            Log.i("PartialGallerySize", " ${galleryImageUrls.size}")
        }
        return galleryImageUrls
    }

    override fun onCleared() {
        compositeDisposable.clear()
    }
}