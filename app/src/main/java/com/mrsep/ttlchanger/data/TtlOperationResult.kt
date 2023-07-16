package com.mrsep.ttlchanger.data

sealed class TtlOperationResult {

    data class Success(val value: Int) : TtlOperationResult()

    object InvalidValue : TtlOperationResult()

    object NoRootAccess : TtlOperationResult()

    data class ErrorReturnCode(val code: Int) : TtlOperationResult()

    data class UnhandledError(val t: Throwable?) : TtlOperationResult()

}