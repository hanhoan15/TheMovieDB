import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbService {

    @GET(AppConfig.PATH_MOVIE_NOW_PLAYING)
    suspend fun getNowPlaying(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MoviesResponse

    @GET(AppConfig.PATH_MOVIE_TOP_RATED)
    suspend fun getTopRated(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MoviesResponse

    @GET(AppConfig.PATH_MOVIE_POPULAR)
    suspend fun getPopular(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MoviesResponse

    @GET(AppConfig.PATH_MOVIE_UPCOMING)
    suspend fun getUpcoming(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MoviesResponse
}
