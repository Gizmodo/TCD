package com.shop.tcd.data.remote

import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.extension.handleApi
import com.shop.tcd.data.dto.ato.UpdateRequest
import javax.inject.Inject

class UpdateRepository @Inject constructor(
    private val updateApi: UpdateApi,
) {
    suspend fun pingUpdateServer(): NetworkResult<String> =
        handleApi { updateApi.sendPing() }

    suspend fun checkUpdatePost(updateRequest: UpdateRequest) =
        handleApi { updateApi.checkUpdatePost(updateRequest) }
}
