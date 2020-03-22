package fr.comif.mycomifclient.serverhandling

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import fr.comif.mycomifclient.UnsafeHTTPClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface HTTPServices {

    @GET("/api/users/info")
    @Headers("Content-Type:application/json")
    fun getUser(
        @Header("Authorization") bearerToken: String
    ): Call<JsonObject>

    @GET("api/transactions/info")
    @Headers("Content-Type:application/json")
    fun getTransactions(
        @Header("Authorization") bearerToken: String
    ): Call<JsonArray>

    @POST("api/users/login")
    @Headers("Content-Type:application/json")
    fun authenticate(
        @Body request: JsonObject
    ): Call<JsonObject>

    @POST("api/users/reset")
    @Headers("Content-Type:application/json")
    fun resetPassword(
        @Header("Authorization") bearerToken: String,
        @Body request: JsonObject
    ): Call<JsonObject>

    @POST("api/users/forgot")
    @Headers("Content-Type:application/json")
    fun forgotPassword(
        @Body request: JsonObject
    ): Call<JsonObject>

    @POST("api/users/logout")
    @Headers("Content-Type: text/html")
    fun logoutFromApi(
        @Body request: JsonObject
    ): Call<ResponseBody>

    /**
     * Create the HTTPService
     * @parameter a boolean that specifies whether the application communicates with the dev'
     * server or the prod' one
     * @return the HTTPService
     */
    companion object {

        fun create(isSafeConnexion: Boolean): HTTPServices {

            val retrofit: Retrofit = if (isSafeConnexion) {
                Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://comif.fr")
                    .build()
            } else {
                Retrofit.Builder()
                    .client(UnsafeHTTPClient.getUnsafeOkHttpClient().build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://dev.comif.fr")
                    .build()
            }
            return retrofit.create(HTTPServices::class.java)
        }
    }
}