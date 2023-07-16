package com.mrsep.ttlchanger.data

interface TtlManager {

    suspend fun writeValue(value: Int): TtlOperationResult

    suspend fun readValue(): TtlOperationResult

}