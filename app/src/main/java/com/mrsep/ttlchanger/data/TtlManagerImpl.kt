package com.mrsep.ttlchanger.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.Exception

class TtlManagerImpl : TtlManager {

    private val mutex = Mutex()

    override suspend fun writeValue(value: Int): TtlOperationResult {
        if (value !in 1..255) return TtlOperationResult.InvalidValue
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                var process: Process? = null
                try {
                    process = Runtime.getRuntime().exec("su")
                    process.outputStream.bufferedWriter().use { writer ->
                        writer.write("sysctl -w net.ipv4.ip_default_ttl=$value\n")
                        writer.write("exit\n")
                        writer.flush()
                    }
                    process.waitFor()

                    val exitCode = process.exitValue()
                    if (exitCode == 0) {
                        TtlOperationResult.Success(value)
                    } else {
                        TtlOperationResult.ErrorReturnCode(exitCode)
                    }
                } catch (e: IOException) {
                    TtlOperationResult.NoRootAccess
                } catch (e: Exception) {
                    TtlOperationResult.UnhandledError(e)
                } finally {
                    process?.destroy()
                }
            }
        }
    }

    override suspend fun readValue(): TtlOperationResult {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                var process: Process? = null
                try {
                    process = Runtime.getRuntime().exec("su")
                    process.outputStream.bufferedWriter().use { writer ->
                        writer.write("sysctl net.ipv4.ip_default_ttl\n")
                        writer.write("exit\n")
                        writer.flush()
                    }
                    process.waitFor()

                    val exitCode = process.exitValue()
                    if (exitCode == 0) {
                        process.inputStream.bufferedReader().use { reader ->
                            reader.lineSequence().firstOrNull { line ->
                                line.startsWith("net.ipv4.ip_default_ttl")
                            }?.substringAfter("=")?.trim()?.toIntOrNull()?.let { ttl ->
                                TtlOperationResult.Success(ttl)
                            } ?: TtlOperationResult.InvalidValue
                        }
                    } else {
                        TtlOperationResult.ErrorReturnCode(exitCode)
                    }

                } catch (e: IOException) {
                    TtlOperationResult.NoRootAccess
                } catch (e: Exception) {
                    TtlOperationResult.UnhandledError(e)
                } finally {
                    process?.destroy()
                }
            }
        }
    }

}