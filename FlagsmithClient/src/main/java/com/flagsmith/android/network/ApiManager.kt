package com.flagsmith.android.network


import com.flagsmith.interfaces.INetworkListener
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit


class ApiManager {
    var url: String
    var passBody: HashMap<String, String>? = null
    var postJsonRaw: String? = null
    var iFinish: INetworkListener?

    //data
    var requestBuilder: Request.Builder? = null
    var request: Request? = null
    var client: OkHttpClient? = null
    var res: String? = null

    //status of request
    var responseStatusSuccessful: Boolean = false
    var requestFailedException: Exception? = null
    var isCompleteBefore //to called interface finish, to avoid call two times
            = false
    var headers: HashMap<String, String>?

    constructor(
        url: String,
        headers: HashMap<String, String>?,
        iFinish: INetworkListener?
    ) {
        this.url = url
        this.headers = headers
        this.iFinish = iFinish
        startApi()
    }


    constructor(
        url: String, headers: HashMap<String, String>?,
        postJsonRaw: String, iFinish: INetworkListener?
    ) {
        this.url = url
        this.headers = headers
        this.postJsonRaw = postJsonRaw
        this.iFinish = iFinish
        startApi()
    }

    private fun startApi() {
        Thread { functionApi() }.start()
    }

    private fun functionApi() {
        setClientAndSetRequest()
        downloadString()
        chooseTypeOfRequestStatus()
    }

    private fun chooseTypeOfRequestStatus() {
        val isValidResponse = res != null
        if (responseStatusSuccessful && isValidResponse) {
            finishSuccess()
            return
        } else {
            finishFailed()
            return
        }
    }

    private fun finishSuccess() {
        //check called before
        if (isCompleteBefore) return

        if (iFinish != null) {
            iFinish?.success(res)
        }
        isCompleteBefore = true
    }

    private fun finishFailed() {
        //check called before
        if (isCompleteBefore) {
            return
        }
        if (iFinish != null) {
            iFinish?.failed(requestFailedException ?: IllegalStateException("Request failed for unknown reason"))
        }
        isCompleteBefore = true
    }

    private fun downloadString() {
        try {
            val response = client!!.newCall(request!!).execute()
            res = response.body!!.string()
            responseStatusSuccessful = response.isSuccessful
        } catch (es: SecurityException) {
            responseStatusSuccessful = false
            requestFailedException = es
            return
        } catch (e: Exception) {
            requestFailedException = e
            responseStatusSuccessful = false
            return
        }
    }

    private fun setClientAndSetRequest() {
        val isTypePostJsonRaw = postJsonRaw != null && postJsonRaw!!.isNotEmpty()
        if (isTypePostJsonRaw) {
            methodTypePOSTJsonRaw()
        } else if (passBody == null || passBody!!.size == 0) {
            methodTypeGet()

        } else {
            methodTypePOST()
        }
    }

    private fun methodTypePOSTJsonRaw() {

        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

        val requestBody = postJsonRaw!!.toRequestBody(JSON)

        /// client
        client = clientWithRetry()

        //add url and post body
        requestBuilder = Request.Builder()
            .url(url)
            .post(requestBody)

        //add header
        addHeaders()

        //build now
        request = requestBuilder!!.build()

    }

    private fun methodTypePOST() {
        // create builder to carry parameter post body
        val mMultiBuilder = MultipartBody.Builder()

        mMultiBuilder.setType(MultipartBody.FORM)

        for (entry in passBody!!.entries.iterator()) {
            print("${entry.key} : ${entry.value}")
        }

        passBody!!.forEach {
            mMultiBuilder.addFormDataPart(it.key, it.value)
        }

        //create object type request with body
        val requestBody: RequestBody = mMultiBuilder.build()
        // client
        client = clientWithRetry()

        //add url and post body
        requestBuilder = Request.Builder()
            .url(url)
            .post(requestBody)
        //add header
        addHeaders()
        //build now
        request = requestBuilder!!.build()
    }

    private fun methodTypeGet() {
        println("methodTypeGet()")

        /// client
        client = clientWithRetry()
        requestBuilder = Request.Builder()
        requestBuilder!!.url(url)
        addHeaders()

        //build now
        request = requestBuilder!!.build()
    }

    private fun addHeaders() {
        requestBuilder!!.header("Content-Type", "application/json")

        if (headers == null) return

        headers!!.forEach {
            requestBuilder!!.header(it.key, it.value)
        }
    }

    private fun clientWithRetry(): OkHttpClient {
        val client = OkHttpClient.Builder()
        client.readTimeout(60, TimeUnit.SECONDS)
        client.retryOnConnectionFailure(true)
        return client.build()
    }
}
