package com.example.mvvmtask.di

import android.app.Application
import androidx.room.Room
import com.example.mvvmtask.data.database.WallPaperDatabase
import com.example.mvvmtask.data.database.dao.SavedPhotosDao
import com.example.mvvmtask.data.network.ApiService
import com.example.mvvmtask.data.repositoryImp.SavedPhotosRepository
import com.example.mvvmtask.data.repositoryImp.WallPaperRepository
import com.example.mvvmtask.ui.main.gallery.GalleryImagesViewModel
import com.example.mvvmtask.ui.viewmodel.SavedPhotosViewModel
import com.example.mvvmtask.ui.viewmodel.WallPaperViewModel
import com.example.mvvmtask.utils.API_KEY
import com.example.mvvmtask.utils.BASE_URL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetworkModule {
    companion object {

        class AuthInterceptor(private val apiKey: String) : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                    .newBuilder()
                    .addHeader("Authorization", apiKey)
                    .build()
                return chain.proceed(request)
            }
        }

        private fun provideHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
            return OkHttpClient
                .Builder()
                .addInterceptor(authInterceptor)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build()
        }


        private fun provideConverterFactory(): GsonConverterFactory =
            GsonConverterFactory.create()


        private fun provideRetrofit(
            okHttpClient: OkHttpClient,
            gsonConverterFactory: GsonConverterFactory
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(gsonConverterFactory)
                .build()
        }

        private fun provideService(retrofit: Retrofit): ApiService =
            retrofit.create(ApiService::class.java)


        val wallPaperViewModel= module {
            viewModel {
                WallPaperViewModel(get())
            }
            viewModel {
                GalleryImagesViewModel(get())
            }

            viewModel {
                SavedPhotosViewModel(get())
            }
        }
        val networkModule = module {
            single { AuthInterceptor(API_KEY) }
            single { provideHttpClient(get()) }
            single { provideConverterFactory() }
            single { provideRetrofit(get(), get()) }
            single { provideService(get()) }
            factory { WallPaperRepository(get()) }
            factory { SavedPhotosRepository(get()) }
        }

        val databaseModule = module {
            fun provideDatabase(application: Application): WallPaperDatabase {
                return Room.databaseBuilder(
                    application,
                    WallPaperDatabase::class.java,
                    "wallPaperDatabase"
                ).build()
            }


            fun provideAppsDao(database: WallPaperDatabase): SavedPhotosDao {
                return database.savedPhotosDao()
            }
            single { provideDatabase(androidApplication()) }
            single { provideAppsDao(get()) }
        }
    }
}