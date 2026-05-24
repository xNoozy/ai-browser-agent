package com.qalab.launcher.data.remote

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTestLabClient @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun runTest(
        projectId: String,
        apiKey: String,
        apkPath: String,
        deviceModel: String,
        apiLevel: Int,
        locale: String = "en_US",
        orientation: String = "portrait"
    ): Result<TestLabResult> = withContext(Dispatchers.IO) {
        try {
            val requestBody = TestLabRequest(
                environmentMatrix = EnvironmentMatrix(
                    androidDeviceList = AndroidDeviceList(
                        androidDevices = listOf(
                            AndroidDevice(
                                androidModelId = deviceModel,
                                androidVersionId = apiLevel.toString(),
                                locale = locale,
                                orientation = orientation
                            )
                        )
                    )
                ),
                testSpecification = TestSpecification(
                    androidRoboTest = AndroidRoboTest(
                        appApk = FileReference(gcsPath = apkPath)
                    )
                )
            )

            val json = gson.toJson(requestBody)
            val request = Request.Builder()
                .url("https://testing.googleapis.com/v1/projects/$projectId/testMatrices?key=$apiKey")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val result = gson.fromJson(
                    response.body?.string(),
                    TestLabResult::class.java
                )
                Result.success(result)
            } else {
                Result.failure(Exception("Firebase Test Lab error: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTestStatus(
        projectId: String,
        apiKey: String,
        testMatrixId: String
    ): Result<TestLabResult> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://testing.googleapis.com/v1/projects/$projectId/testMatrices/$testMatrixId?key=$apiKey")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val result = gson.fromJson(
                    response.body?.string(),
                    TestLabResult::class.java
                )
                Result.success(result)
            } else {
                Result.failure(Exception("Error: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Firebase Test Lab API models
data class TestLabRequest(
    val environmentMatrix: EnvironmentMatrix,
    val testSpecification: TestSpecification
)

data class EnvironmentMatrix(
    val androidDeviceList: AndroidDeviceList
)

data class AndroidDeviceList(
    val androidDevices: List<AndroidDevice>
)

data class AndroidDevice(
    val androidModelId: String,
    val androidVersionId: String,
    val locale: String,
    val orientation: String
)

data class TestSpecification(
    val androidRoboTest: AndroidRoboTest? = null,
    val androidInstrumentationTest: AndroidInstrumentationTest? = null
)

data class AndroidRoboTest(
    val appApk: FileReference
)

data class AndroidInstrumentationTest(
    val appApk: FileReference,
    val testApk: FileReference
)

data class FileReference(
    val gcsPath: String
)

data class TestLabResult(
    @SerializedName("testMatrixId") val testMatrixId: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("resultStorage") val resultStorage: ResultStorage? = null
)

data class ResultStorage(
    @SerializedName("googleCloudStorage") val googleCloudStorage: GoogleCloudStorage? = null
)

data class GoogleCloudStorage(
    @SerializedName("gcsPath") val gcsPath: String? = null
)
