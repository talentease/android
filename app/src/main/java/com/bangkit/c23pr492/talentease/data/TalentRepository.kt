package com.bangkit.c23pr492.talentease.data

import android.util.Log
import com.bangkit.c23pr492.talentease.data.database.ApplicationEntity
import com.bangkit.c23pr492.talentease.data.database.TalentEaseDao
import com.bangkit.c23pr492.talentease.data.database.TalentEntity
import com.bangkit.c23pr492.talentease.data.datastore.AuthDataStore
import com.bangkit.c23pr492.talentease.data.model.position.PositionItemModel
import com.bangkit.c23pr492.talentease.data.network.ApiService
import com.bangkit.c23pr492.talentease.utils.Const.tagRepository
import com.bangkit.c23pr492.talentease.utils.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class TalentRepository(
    private val apiService: ApiService,
    private val mTalentEaseDao: TalentEaseDao,
    private val authDataStore: AuthDataStore
) {
    suspend fun addTalent(talent: TalentEntity) {
        mTalentEaseDao.upsertTalent(talent)
    }

    suspend fun removeTalent(talent: TalentEntity) {
        mTalentEaseDao.upsertTalent(talent)
    }

    suspend fun applyApplication() {
        mTalentEaseDao.upsertApplication(ApplicationEntity.application1)
    }

    fun getTalentId(): Flow<String?> = authDataStore.getTalentId()

    suspend fun saveTalentId(talentId: String) {
        authDataStore.saveTalentId(talentId)
    }

    private suspend fun clearTalentId() {
        authDataStore.clearTalentId()
    }

    fun getAllPosition(token: String) = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getAllPositions(generateBearerToken(token))
            Log.d(tagRepository, response.toString())
            emit(Resource.Success(response))
        } catch (e: Exception) {
            Log.e(tagRepository, Log.getStackTraceString(e))
            emit(Resource.Error(UiText.DynamicString(e.message ?: "Unknown Error")))
        }
    }.flowOn(Dispatchers.IO)

    fun searchPositionsFromName(
        token: String,
        query: String
    ): Flow<Resource<List<PositionItemModel>?>> =
        flow {
            emit(Resource.Loading)
            try {
                val response = apiService.getAllPositions(generateBearerToken(token)).data?.toList()
                    ?.filter {
                        it.title?.contains(query, ignoreCase = true) ?: false
                    }
                Log.d(tagRepository, response.toString())
                emit(Resource.Success(response))
            } catch (e: Exception) {
                Log.e(tagRepository, Log.getStackTraceString(e))
                emit(Resource.Error(UiText.DynamicString(e.message ?: "Unknown Error")))
            }
        }.flowOn(Dispatchers.IO)

    fun getPositionByPositionId(token: String, positionId: String) = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getPositionByPositionId(
                generateBearerToken(token),
                positionId
            )
            Log.d(tagRepository, response.toString())
            emit(Resource.Success(response.data))
        } catch (e: Exception) {
            Log.e(tagRepository, Log.getStackTraceString(e))
            emit(Resource.Error(UiText.DynamicString(e.message ?: "Unknown Error")))
        }
    }

    fun applyPosition(token: String, positionId: String, file: MultipartBody.Part?) = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.applyPositions(
                generateBearerToken(token),
                positionId.toRequestBody("text/plain".toMediaType()),
                file
            )
            Log.d(tagRepository, response.toString())
            emit(Resource.Success(response))
        } catch (e: Exception) {
            Log.e(tagRepository, Log.getStackTraceString(e))
            emit(Resource.Error(UiText.DynamicString(e.message ?: "Unknown Error")))
        }
    }

    //    fun getPositionWithPositionId(positionId: String) = flow {
//        emit(Resource.Loading)
//        try {
//            val response = mTalentEaseDao.getPositionWithPositionId(positionId)
//            Log.d(tagRepository, response.toString())
//            emit(Resource.Success(response))
//        } catch (e: Exception) {
//            Log.e(tagRepository, Log.getStackTraceString(e))
//            emit(Resource.Error(UiText.DynamicString(e.message ?: "Unknown Error")))
//        }
//    }.flowOn(Dispatchers.IO)
//
    fun getAllTalentApplicationWithTalentId(talentId: String) = flow {
        emit(Resource.Loading)
        try {
            val response = mTalentEaseDao.getAllTalentApplicationWithTalentId(talentId)
            Log.d(tagRepository, response.toString())
            emit(Resource.Success(response))
        } catch (e: Exception) {
            Log.e(tagRepository, Log.getStackTraceString(e))
            emit(Resource.Error(UiText.DynamicString(e.message ?: "Unknown Error")))
        }
    }.flowOn(Dispatchers.IO)

    private fun generateBearerToken(token: String): String {
        return if (token.contains("bearer", true)) {
            token
        } else {
            "Bearer $token"
        }
    }

    companion object {
        @Volatile
        private var instance: TalentRepository? = null
        fun getInstance(
            apiService: ApiService,
            mTalentEaseDao: TalentEaseDao,
            authDataStore: AuthDataStore
        ): TalentRepository = instance ?: synchronized(this) {
            instance ?: TalentRepository(apiService, mTalentEaseDao, authDataStore)
        }.also { instance = it }
    }
}