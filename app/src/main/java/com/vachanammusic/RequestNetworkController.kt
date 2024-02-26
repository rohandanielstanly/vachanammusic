package com.vachanammusic
import android.util.Log
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class RequestNetworkController private constructor() {
    private var client: OkHttpClient = createClient()

    private fun createClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .connectTimeout(SOCKET_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .hostnameVerifier { _, _ -> true } // Warning: This bypasses hostname verification
            .build()
    }

    fun execute(
        requestNetwork: RequestNetwork,
        method: String,
        url: String,
        tag: String?,
        requestListener: RequestNetwork.RequestListener
    ) {
        val reqBuilder = Request.Builder()
        val headerBuilder = Headers.Builder()

        requestNetwork.headers.forEach { (key, value) -> headerBuilder.add(key, value.toString()) }

        try {
            val request = when (requestNetwork.requestType) {
                REQUEST_PARAM -> buildParamRequest(requestNetwork.params, method, url, headerBuilder)
                else -> buildJsonRequest(requestNetwork.params, method, url, headerBuilder)
            }

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    requestNetwork.activity.runOnUiThread {
                        requestListener.onErrorResponse(tag, e.message)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()?.trim { it <= ' ' } ?: ""
                    requestNetwork.activity.runOnUiThread {
                        val map = response.headers.toMultimap().mapValues { it.value.joinToString(", ") }
                        requestListener.onResponse(tag, responseBody, map as HashMap<String, Any?>)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("RequestNetworkController", "Request Execution Error: ${e.message}")
            requestListener.onErrorResponse(tag, e.message)
        }
    }

    private fun buildParamRequest(
        params: Map<String, Any>,
        method: String,
        url: String,
        headerBuilder: Headers.Builder
    ): Request {
        val reqBuilder = Request.Builder() // Define reqBuilder here

        val httpBuilder = url.toHttpUrlOrNull()?.newBuilder() ?: throw NullPointerException("Unexpected url: $url")
        params.forEach { (key, value) -> httpBuilder.addQueryParameter(key, value.toString()) }

        return if (method == GET) {
            reqBuilder.url(httpBuilder.build()).headers(headerBuilder.build()).get().build()
        } else {
            val reqBody = FormBody.Builder()
            params.forEach { (key, value) -> reqBody.add(key, value.toString()) }
            reqBuilder.url(url).headers(headerBuilder.build()).method(method, reqBody.build()).build()
        }
    }

    private fun buildJsonRequest(
        params: Map<String, Any>,
        method: String,
        url: String,
        headerBuilder: Headers.Builder
    ): Request {
        val reqBuilder = Request.Builder() // Define reqBuilder here

        val reqBody = Gson().toJson(params).toRequestBody("application/json".toMediaTypeOrNull())

        return if (method == GET) {
            reqBuilder.url(url).headers(headerBuilder.build()).get().build()
        } else {
            reqBuilder.url(url).headers(headerBuilder.build()).method(method, reqBody).build()
        }
    }

    companion object {
        const val GET = "GET"
        const val POST = "POST"
        const val PUT = "PUT"
        const val DELETE = "DELETE"
        const val REQUEST_PARAM = 0
        const val REQUEST_BODY = 1
        private const val SOCKET_TIMEOUT = 15000
        private const val READ_TIMEOUT = 25000

        private var mInstance: RequestNetworkController? = null

        @Synchronized
        fun getInstance(): RequestNetworkController {
            if (mInstance == null) {
                mInstance = RequestNetworkController()
            }
            return mInstance!!
        }
    }
}
