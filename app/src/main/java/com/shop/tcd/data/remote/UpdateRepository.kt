package com.shop.tcd.data.remote

import com.shop.tcd.core.extension.handleApi
import com.shop.tcd.data.dto.ato.UpdateRequest
import javax.inject.Inject

class UpdateRepository @Inject constructor(
    private val updateApi: UpdateApi,
) {
    suspend fun checkUpdatePost(updateRequest: UpdateRequest) =
        handleApi { updateApi.checkUpdatePost(updateRequest) }
}
