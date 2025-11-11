package com.feature.home.fakes

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class FakeTokenBalanceContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.feature.home.test.tokenbalance.fake"
        private const val COLUMN_CONTRACT_ADDRESS = "contract_address"
        private const val COLUMN_CHAIN_ID = "chain_id"
        private const val COLUMN_TOKEN_BALANCE = "token_balance"
        private val COLUMNS = arrayOf(
            COLUMN_CONTRACT_ADDRESS,
            COLUMN_CHAIN_ID,
            COLUMN_TOKEN_BALANCE
        )
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        // Return empty cursor with expected columns
        return MatrixCursor(projection ?: COLUMNS)
    }

    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/vnd.$AUTHORITY.balances"

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // Accept inserts as no-op and return a fake inserted URI
        return uri.buildUpon().appendPath("ok").build()
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}


