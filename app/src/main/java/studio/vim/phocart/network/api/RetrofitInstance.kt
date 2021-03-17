package studio.vim.phocart.network.api

import android.os.Build
import android.provider.SyncStateContract
import androidx.annotation.RequiresApi

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import studio.vim.phocart.utils.Constants
import java.time.Duration
import java.util.concurrent.TimeUnit


/**
 * Created by Gilang Arinata on 10/01/21.
 * https://github.com/gilangarinata/
 */

class RetrofitInstance {
    companion object {
        private val retrofit by lazy {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

            Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
        }

        val api by lazy {
            retrofit.create(PhocartAPI::class.java)
        }
    }
}