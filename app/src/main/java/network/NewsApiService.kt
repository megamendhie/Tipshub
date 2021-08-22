package network

import com.sqube.tipshub.BuildConfig
import models.News
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://newsapi.org/"
private const val API_KEY = "apiKey=${BuildConfig.NEWS_API_KEY}"
private const val DOMAINS = "domains=goal.com"
private const val LANGUAGE = "language=en"
private const val PAGE_SIZE = "pageSize=29"
private const val SUFFIX = "v2/everything?"
private const val PARAMETERS = "$SUFFIX$DOMAINS&$LANGUAGE&$PAGE_SIZE&$API_KEY"

private val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()

interface NewsApiService{
    @GET(PARAMETERS)
    fun getNews():Call<News>
}

object NewsApi{
    val retrofitService: NewsApiService by lazy {
        retrofit.create(NewsApiService::class.java)
    }
}