package com.example.biologytester.ota

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url
import retrofit2.Response

data class GithubRelease(
    val tag_name: String,
    val name: String,
    val body: String,
    val assets: List<GithubAsset>
)

data class GithubAsset(
    val name: String,
    val browser_download_url: String
)

interface GithubApiService {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GithubRelease
}
