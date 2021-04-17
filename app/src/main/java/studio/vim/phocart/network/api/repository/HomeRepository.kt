package studio.vim.phocart.network.api.repository

import okhttp3.MultipartBody
import studio.vim.phocart.network.api.RetrofitInstance

class HomeRepository {
    suspend fun getHomeData(file: MultipartBody.Part) = RetrofitInstance.api.postPicture("0", file)
    suspend fun getHomeDataNoBackground(file: MultipartBody.Part) = RetrofitInstance.api.postPicture("1", file)
}