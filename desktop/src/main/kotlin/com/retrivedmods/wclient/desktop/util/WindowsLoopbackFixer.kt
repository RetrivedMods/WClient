package com.retrivedmods.wclient.desktop.util

import java.io.File

object WindowsLoopbackFixer {
    
    fun isWindows(): Boolean {
        return System.getProperty("os.name").lowercase().contains("windows")
    }
    
    fun isRunningAsAdmin(): Boolean {
        if (!isWindows()) return false
        
        try {
            val process = ProcessBuilder("net", "session")
                .redirectErrorStream(true)
                .start()
            
            process.waitFor()
            return process.exitValue() == 0
        } catch (e: Exception) {
            return false
        }
    }
    
    fun isLoopbackEnabled(): Boolean {
        if (!isWindows()) return true
        
        try {
            val process = ProcessBuilder("CheckNetIsolation", "LoopbackExempt", "-s")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            
            return output.contains("Microsoft.MinecraftUWP") || 
                   output.contains("Microsoft.MinecraftWindowsBeta")
        } catch (e: Exception) {
            return false
        }
    }
    
    fun enableLoopback(): Result<String> {
        if (!isWindows()) {
            return Result.success("Not Windows, loopback not needed")
        }
        
        if (!isRunningAsAdmin()) {
            return Result.failure(Exception("Not running as Administrator"))
        }
        
        return try {
            val commands = listOf(
                listOf("CheckNetIsolation", "LoopbackExempt", "-a", "-n=Microsoft.MinecraftUWP_8wekyb3d8bbwe"),
                listOf("CheckNetIsolation", "LoopbackExempt", "-a", "-n=Microsoft.MinecraftWindowsBeta_8wekyb3d8bbwe")
            )
            
            var lastError = ""
            var success = false
            
            for (command in commands) {
                try {
                    val process = ProcessBuilder(command)
                        .redirectOutput(ProcessBuilder.Redirect.PIPE)
                        .redirectError(ProcessBuilder.Redirect.PIPE)
                        .start()
                    
                    val output = process.inputStream.bufferedReader().readText()
                    val error = process.errorStream.bufferedReader().readText()
                    val exitCode = process.waitFor()
                    
                    if (exitCode == 0 || output.contains("OK")) {
                        success = true
                    } else {
                        lastError = error.ifEmpty { output }
                    }
                } catch (e: Exception) {
                    lastError = e.message ?: "Unknown error"
                }
            }
            
            if (success) {
                Result.success("Loopback enabled successfully")
            } else {
                Result.failure(Exception("Failed to enable loopback.\n$lastError"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun createElevatedScript(): File? {
        if (!isWindows()) return null
        
        try {
            val tempDir = System.getProperty("java.io.tmpdir")
            val scriptFile = File(tempDir, "novaclient-fix.bat")
            
            val script = """
                @echo off
                CheckNetIsolation LoopbackExempt -a -n="Microsoft.MinecraftUWP_8wekyb3d8bbwe"
                CheckNetIsolation LoopbackExempt -a -n="Microsoft.MinecraftWindowsBeta_8wekyb3d8bbwe"
                exit
            """.trimIndent()
            
            scriptFile.writeText(script)
            return scriptFile
        } catch (e: Exception) {
            return null
        }
    }
    
    fun runAsAdmin(): Boolean {
        if (!isWindows()) return false
        
        try {
            val batFile = createBatchFile()
            if (batFile == null) return false
            
            val vbsFile = createVBSLauncher(batFile.absolutePath)
            if (vbsFile == null) {
                batFile.delete()
                return false
            }
            
            val process = ProcessBuilder("wscript", vbsFile.absolutePath)
                .start()
            process.waitFor()
            
            Thread.sleep(2000)
            
            vbsFile.delete()
            batFile.delete()
            
            return isLoopbackEnabled()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    private fun createBatchFile(): File? {
        try {
            val tempDir = System.getProperty("java.io.tmpdir")
            val batFile = File(tempDir, "novaclient-fix.bat")
            
            val script = """
                @echo off
                CheckNetIsolation LoopbackExempt -a -n="Microsoft.MinecraftUWP_8wekyb3d8bbwe"
                CheckNetIsolation LoopbackExempt -a -n="Microsoft.MinecraftWindowsBeta_8wekyb3d8bbwe"
                exit
            """.trimIndent()
            
            batFile.writeText(script)
            return batFile
        } catch (e: Exception) {
            return null
        }
    }
    
    private fun createVBSLauncher(batPath: String): File? {
        try {
            val tempDir = System.getProperty("java.io.tmpdir")
            val vbsFile = File(tempDir, "novaclient-elevate.vbs")
            
            val vbsScript = """
                Set UAC = CreateObject("Shell.Application")
                UAC.ShellExecute "$batPath", "", "", "runas", 0
            """.trimIndent()
            
            vbsFile.writeText(vbsScript)
            return vbsFile
        } catch (e: Exception) {
            return null
        }
    }
}
