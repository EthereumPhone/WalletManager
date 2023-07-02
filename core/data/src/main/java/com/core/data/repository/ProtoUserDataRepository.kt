package com.core.data.repository

import com.core.datastore.WmPreferencesDataSource
import com.core.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProtoUserDataRepository @Inject constructor(
    private val wmPreferencesDataSource: WmPreferencesDataSource
): UserDataRepository {

    override val userData: Flow<UserData> =
        wmPreferencesDataSource.userData

    override suspend fun setWalletAddress(address: String) =
        wmPreferencesDataSource.setWalletAddress(address)

}