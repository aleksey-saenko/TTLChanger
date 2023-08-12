package com.mrsep.ttlchanger.data

sealed class TtlOperationResult {

    data class Success(
        val ipv4: String,
        val ipv6: String
    ) : TtlOperationResult()

    data object InvalidValue : TtlOperationResult()

    data object NoRootAccess : TtlOperationResult()

    data class ErrorReturnCode(val code: Int) : TtlOperationResult()

    data class UnhandledError(
        val message: String? = null,
        val t: Throwable? = null
    ) : TtlOperationResult()

}