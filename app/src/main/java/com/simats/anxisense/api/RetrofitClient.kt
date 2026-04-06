package com.simats.anxisense.api

import okhttp3.OkHttpClient // [NEW IMPORT]
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // [NEW IMPORT]

object  RetrofitClient {

    private const val BASE_URL = "http://10.153.120.8:5000/api/"

    val instance: DoctorApi by lazy {
        
        // [NEW] Configure custom timeouts
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // wait up to 60s for connection
            .readTimeout(120, TimeUnit.SECONDS)    // wait up to 120s for analysis result (important!)
            .writeTimeout(120, TimeUnit.SECONDS)   // wait up to 120s for image upload
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // [NEW] Attach the custom client here
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(DoctorApi::class.java)
    }
}