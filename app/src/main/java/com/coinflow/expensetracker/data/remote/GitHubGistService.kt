package com.coinflow.expensetracker.data.remote

import com.coinflow.expensetracker.data.model.GistCreateRequest
import com.coinflow.expensetracker.data.model.GistPatchRequest
import com.coinflow.expensetracker.data.model.GistResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface GitHubGistService {

    @Headers("Accept: application/vnd.github+json")
    @GET("gists/{gist_id}")
    suspend fun getGist(
        @Path("gist_id") gistId: String,
        @Header("Authorization") tokenHeader: String
    ): Response<GistResponse>

    @Headers("Accept: application/vnd.github+json")
    @PATCH("gists/{gist_id}")
    suspend fun updateGist(
        @Path("gist_id") gistId: String,
        @Header("Authorization") tokenHeader: String,
        @Body body: GistPatchRequest
    ): Response<GistResponse>

    @Headers("Accept: application/vnd.github+json")
    @POST("gists")
    suspend fun createGist(
        @Header("Authorization") tokenHeader: String,
        @Body body: GistCreateRequest
    ): Response<GistResponse>
}
