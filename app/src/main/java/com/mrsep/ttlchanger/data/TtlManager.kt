package com.mrsep.ttlchanger.data

interface TtlManager {

    suspend fun writeValue(value: Int, applyIPv6: Boolean): TtlOperationResult

    suspend fun readValue(readIPv6: Boolean): TtlOperationResult

}