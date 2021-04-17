package studio.vim.phocart.network.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Created by Gilang Arinata on 10/01/21.
 * https://github.com/gilangarinata/
 */

interface PhocartAPI {
    @Multipart
    @POST("process/{isRemove}")
    suspend fun postPicture(@Path("isRemove") isRemove: String, @Part file: MultipartBody.Part): Response<ApiResponse>
}