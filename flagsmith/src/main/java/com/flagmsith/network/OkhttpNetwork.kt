package com.flag.android.network

//import android.content.Context
//import android.os.Handler
//import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit


class OkhttpNetwork {


//    var mContext: Context
    var url: String
    var passBody: HashMap<String, String>? = null
    var postJsonRaw: String? = null;
//    var readTimeout_thenStopRetryUploading =  5 * 60 //number of second   :  default 5 * 60  menas 5 minets
    var iFinish: INetworkListener?

    //data
    var requestBuilder: Request.Builder? = null
    var request: Request? = null
    var client: OkHttpClient? = null
    var res: String? = null

    //status of request
    var request_status = false
    var request_failed_msg: String? = null
    var isCompleteBefore //to called interface finish, to avoid call two times
            = false
    var headers: HashMap<String, String>?

    //----------------------------------------------------------------------------- constructor


    /**
     * case get url
     */
    constructor(
//        mContext: Context,
        url: String,
        headers: HashMap<String, String>?,
        iFinish: INetworkListener?
    ) {

//        this.mContext = mContext
        this.url = url
        this.headers = headers
        this.iFinish = iFinish
        println(  "OkhttpNetwork - start url: $url")
        println(  "OkhttpNetwork - param:  $passBody")
        //start
        startApi()
    }

    /**
     * case Post "map<string,string>"
     */
    constructor(
//        mContext: Context,
        url: String, headers: HashMap<String, String>?,
        passBody: HashMap<String, String>, iFinish: INetworkListener?
    ) {

//        this.mContext = mContext
        this.url = url
        this.headers = headers
        this.passBody = passBody
        this.iFinish = iFinish

        //log
        println("OkhttpNetwork - start url: $url")
        println(  "OkhttpNetwork - param:  $passBody")

        //start
        startApi()
    }


    /**
     * case Post "map<string,string>"
     */
    constructor(
//        mContext: Context,
        url: String, headers: HashMap<String, String>?,
        postJsonRaw: String, iFinish: INetworkListener?
    ) {

//        this.mContext = mContext
        this.url = url
        this.headers = headers
        this.postJsonRaw = postJsonRaw
        this.iFinish = iFinish

        //log
        println(  "OkhttpNetwork - start url: $url")
        println(  "OkhttpNetwork - postJsonRaw:  $postJsonRaw")

        //start
        startApi()
    }

    //---------------------------------------------------------------------- private code
    private fun startApi() {
        Thread { functionApi() }.start() //end thread
       // functionApi()
    }

    private fun functionApi() {
        setClientAndSetRequest()
        downloadString()

        //  try { Thread.sleep(1000); } catch (InterruptedException e) { }
        chooseTypeOfRequestStatus()
    }

    private fun chooseTypeOfRequestStatus() {
        val isValiedResponse = res != null && res!!.length > 0
        if (request_status && isValiedResponse) {
            finish_success()
            return
        } else {
            finish_failed()
            return
        }
    }

    //---------------------------------------------------------------------------------- finish mode
    private fun finish_success() {
        //println(  "finish_success() - isCompleteBefore: " + isCompleteBefore  );

        //check called before
        if (isCompleteBefore) return


        //run UI inside thread
        /**
        val h = Handler(mContext.mainLooper)
        h.post {
        println(  "finish_success() - success  $res")

        // printToUser
        if (iFinish != null) {
        iFinish!!.success(res)
        }

        //called
        isCompleteBefore = true
        }
         */


        // printToUser
        if (iFinish != null) {
            iFinish!!.success(res)
        }

        //called
        isCompleteBefore = true

    }

    private fun finish_failed() {
        // println(  "finish_failed() - isCompleteBefore: " + isCompleteBefore  );
        //check called before
        if (isCompleteBefore) {
            return
        }
        // printToUser
        if (iFinish != null) {
            iFinish!!.failed(request_failed_msg)
        }


        //called
        isCompleteBefore = true


        /** --- //run UI inside thread
        val h = Handler(mContext.mainLooper)
        h.post {
        println(  "finish_failed() - request_failed_msg: $request_failed_msg" )

        // printToUser
        if (iFinish != null) {
        iFinish!!.failed(request_failed_msg)
        }


        //called
        isCompleteBefore = true
        }
         */


    }

    //-------------------------------------------------------------------------------- download
    private fun downloadString() {
        try {
            val response = client!!.newCall(request!!).execute()
            res = response.body!!.string()
            request_status = true
        } catch (es: SecurityException) {
            request_status = false
            // println(  "SecurityException: " + es);
            request_failed_msg = es.toString()
            return
        } catch (e: Exception) {
            // Log.i( "abdo","exe: " +  e );
            request_failed_msg = e.toString()
            request_status = false
            return
        }
    }

    private fun setClientAndSetRequest() {

        /////////////////////// check type "post raw json"
        val isTypePostJsonRaw = postJsonRaw != null &&  postJsonRaw!!.length > 0;
        if ( isTypePostJsonRaw ) {
            methodTypePOSTJsonRaw()
        }

        ////////////////////// check type get
        else if (passBody == null || passBody!!.size == 0) {  //   have no post body json
            methodTypeGet()

            ///////////////////// check type Post
        } else {
            methodTypePOST()
        }
    }

    private fun methodTypePOSTJsonRaw() {

        var JSON : MediaType = "application/json; charset=utf-8".toMediaType();

        var requestBody = RequestBody.Companion.create(JSON, postJsonRaw!! )

        /// client
        client = client_with_retry()

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
        // create builder to carry paramater post body
        val mMultiBuilder = MultipartBody.Builder();

        mMultiBuilder.setType( MultipartBody.FORM)

        for (entry in passBody!!.entries.iterator()) {
            print("${entry.key} : ${entry.value}")
        }

       passBody!!.forEach(   {
           mMultiBuilder.addFormDataPart( it.key, it.value)
        })

        // println(  "line 122"  );
        //creaste object type request with body
        val requestBody: RequestBody = mMultiBuilder.build()
        /// client
        client = client_with_retry()

        //add url and post body
        requestBuilder = Request.Builder()
            .url(url)
            .post(requestBody)

        //add header
        addHeaders()

        //build now
        request = requestBuilder!!.build()
        // println(  "line 131"  );
    }

    private fun methodTypeGet() {
        println(  "methodTypeGet()")

        /// client
        client = client_with_retry()
        requestBuilder = Request.Builder()
        requestBuilder!!.url(url)
        addHeaders()

        //build now
        request = requestBuilder!!.build()
    }


    private fun addHeaders() {

        requestBuilder!!.header("Content-Type", "application/json")

        if (headers == null) return

        headers!!.forEach( {
            requestBuilder!!.header( it.key, it.value)
        })
    }


    fun client_with_retry(): OkHttpClient {
            val client = OkHttpClient.Builder()
            client.readTimeout(60, TimeUnit.SECONDS)
            client.retryOnConnectionFailure(true)
            return client.build()
        }


}
