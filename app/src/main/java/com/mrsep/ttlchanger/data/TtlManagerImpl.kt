package com.mrsep.ttlchanger.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.Exception

class TtlManagerImpl : TtlManager {

    private val mutex = Mutex()

    private val ipv4CmdWrite = "sysctl -w net.ipv4.ip_default_ttl=%d\n"
    private val ipv4CmdRead = "sysctl net.ipv4.ip_default_ttl\n"

    private val ipv6Loop = "for INTERFACE in \$(sysctl -a 2>/dev/null | " +
            "grep -Eo \"^net\\.ipv6\\.conf\\.[^\\.]+\\.hop_limit\"); "

    private val ipv6CmdWrite = ipv6Loop + "do sysctl -w \$INTERFACE=%d; done \n"
    private val ipv6CmdRead = ipv6Loop + "do sysctl \$INTERFACE; done \n"

    override suspend fun writeValue(value: Int, applyIPv6: Boolean): TtlOperationResult {
        if (value !in 1..255) return TtlOperationResult.InvalidValue
        return withContext(Dispatchers.IO) {
            mutex.lock()
            var process: Process? = null
            try {
                process = Runtime.getRuntime().exec("su")
                process.outputStream.bufferedWriter().use { writer ->
                    writer.write(String.format(ipv4CmdWrite, value))
                    if (applyIPv6) writer.write(String.format(ipv6CmdWrite, value))
                    writer.write("exit\n")
                    writer.flush()
                }
                process.waitFor()
                val exitCode = process.exitValue()

                val stderrMessage = process.getErrorsOrNull()
                val unexpectedResponsesMessage = process.getUnexpectedResponsesOrNull(value)?.let {
                    "Unexpected responses:\n$it"
                }
                val error = stderrMessage ?: unexpectedResponsesMessage

                when {
                    exitCode == 0 && error == null -> TtlOperationResult.Success(
                        ipv4 = value.toString(),
                        ipv6 = if (applyIPv6) value.toString() else ""
                    )
                    error != null -> TtlOperationResult.UnhandledError(message = error, t = null)
                    else -> TtlOperationResult.ErrorReturnCode(exitCode)
                }
            } catch (e: IOException) {
                TtlOperationResult.NoRootAccess
            } catch (e: Exception) {
                TtlOperationResult.UnhandledError(t = e)
            } finally {
                process?.destroy()
                mutex.unlock()
            }
        }
    }

    override suspend fun readValue(readIPv6: Boolean): TtlOperationResult {
        return withContext(Dispatchers.IO) {
            mutex.lock()
            var process: Process? = null
            try {
                process = Runtime.getRuntime().exec("su")
                process.outputStream.bufferedWriter().use { writer ->
                    writer.write(ipv4CmdRead)
                    if (readIPv6) writer.write(ipv6CmdRead)
                    writer.write("exit\n")
                    writer.flush()
                }
                process.waitFor()
                val exitCode = process.exitValue()
                val error = process.getErrorsOrNull()
                when {
                    exitCode == 0 && error == null -> process.parseStdoutResponsesToResult()
                    error != null -> TtlOperationResult.UnhandledError(message = error, t = null)
                    else -> TtlOperationResult.ErrorReturnCode(exitCode)
                }
            } catch (e: IOException) {
                TtlOperationResult.NoRootAccess
            } catch (e: Exception) {
                TtlOperationResult.UnhandledError(t = e)
            } finally {
                process?.destroy()
                mutex.unlock()
            }
        }
    }

    private fun Process.getErrorsOrNull(): String? {
        return errorStream.bufferedReader().use { reader ->
            reader.readText().trim().takeIf { it.isNotBlank() }
        }
    }

    private fun Process.getUnexpectedResponsesOrNull(expectedValue: Int): String? {
        return inputStream.bufferedReader().use { reader ->
            reader.lineSequence().mapNotNull { cmdResponse ->
                cmdResponse.split("=", limit = 2).run {
                    cmdResponse.takeIf {
                        size != 2 || get(1).trim().toIntOrNull() != expectedValue
                    }
                }
            }.joinToString("\n").trim().takeIf { it.isNotBlank() }
        }
    }

    private fun Process.parseStdoutResponsesToResult(): TtlOperationResult.Success {
        var ipv4Message = ""
        var ipv6Message = ""
        inputStream.bufferedReader().use { reader ->
            val ipv6Responses = mutableListOf<String>()
            val ipv6value = mutableSetOf<Int>()
            reader.lineSequence().forEach { cmdResponse ->
                cmdResponse.split("=", limit = 2).run {
                    if (size != 2) return@run
                    val parameter = get(0).trim()
                    val value = get(1).trim().toIntOrNull()

                    if (parameter == "net.ipv4.ip_default_ttl") {
                        ipv4Message = value?.toString() ?: cmdResponse
                    } else if (parameter.startsWith("net.ipv6.conf")) {
                        value?.let { ipv6value.add(value) }
                        ipv6Responses.add(cmdResponse)
                    }
                }
            }
            ipv6Message = if (ipv6value.size == 1) {
                ipv6value.first().toString()
            } else {
                ipv6Responses.joinToString("\n")
            }
        }
        return TtlOperationResult.Success(
            ipv4 = ipv4Message,
            ipv6 = ipv6Message
        )
    }

}