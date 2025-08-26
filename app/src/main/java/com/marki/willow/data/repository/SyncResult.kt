package com.marki.willow.data.repository

sealed class SyncResult {
    data class Success(val synced: Int, val skipped: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
}