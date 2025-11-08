package com.retrivedmods.wclient.desktop.ui

import com.retrivedmods.wclient.desktop.core.DesktopAccountManager
import com.retrivedmods.wclient.desktop.core.DesktopModuleManager
import com.retrivedmods.wclient.desktop.core.RelayService
import java.awt.*
import javax.swing.*

class MainWindow : JFrame("NovaClient Desktop - v1.9.1") {
    
    private val accountPanel: AccountPanel
    private val relayPanel: RelayPanel
    private val modulePanel: ModulePanel
    private val consolePanel: ConsolePanel
    
    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(1000, 700)
        setLocationRelativeTo(null)
        
        layout = BorderLayout(10, 10)
        
        val tabbedPane = JTabbedPane()
        
        accountPanel = AccountPanel()
        relayPanel = RelayPanel()
        modulePanel = ModulePanel()
        consolePanel = ConsolePanel()
        
        tabbedPane.addTab("Account", accountPanel)
        tabbedPane.addTab("Relay", relayPanel)
        tabbedPane.addTab("Modules", modulePanel)
        tabbedPane.addTab("Console", consolePanel)
        
        add(tabbedPane, BorderLayout.CENTER)
        
        val statusBar = JPanel(FlowLayout(FlowLayout.LEFT))
        val statusLabel = JLabel("Ready")
        statusBar.add(statusLabel)
        add(statusBar, BorderLayout.SOUTH)
        
        RelayService.setStatusCallback { status ->
            SwingUtilities.invokeLater {
                statusLabel.text = status
            }
        }
        
        RelayService.setConsoleCallback { message ->
            SwingUtilities.invokeLater {
                consolePanel.appendMessage(message)
            }
        }
        
        isVisible = true
        
        DesktopModuleManager.loadConfig()
        DesktopAccountManager.loadAccounts()
        
        checkWindowsLoopback()
    }
    
    private fun checkWindowsLoopback() {
        if (!com.retrivedmods.wclient.desktop.util.WindowsLoopbackFixer.isWindows()) {
            return
        }
        
        val configDir = java.io.File(System.getProperty("user.home"), ".novaclient")
        val skipFile = java.io.File(configDir, ".skip-loopback-check")
        
        if (skipFile.exists()) {
            return
        }
        
        Thread {
            Thread.sleep(500)
            
            val isEnabled = com.retrivedmods.wclient.desktop.util.WindowsLoopbackFixer.isLoopbackEnabled()
            
            if (isEnabled) {
                configDir.mkdirs()
                skipFile.createNewFile()
                return@Thread
            }
            
            SwingUtilities.invokeLater {
                val isAdmin = com.retrivedmods.wclient.desktop.util.WindowsLoopbackFixer.isRunningAsAdmin()
                
                if (isAdmin) {
                    val result = JOptionPane.showConfirmDialog(
                        this,
                        "Windows Minecraft needs loopback permission.\n\n" +
                        "Fix now? (Running as Administrator)",
                        "Windows Setup Required",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    )
                    
                    if (result == JOptionPane.YES_OPTION) {
                        val fixResult = com.retrivedmods.wclient.desktop.util.WindowsLoopbackFixer.enableLoopback()
                        if (fixResult.isSuccess) {
                            configDir.mkdirs()
                            skipFile.createNewFile()
                            JOptionPane.showMessageDialog(
                                this,
                                "Setup complete!\n\nConnect Minecraft to localhost:19132",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                            )
                        } else {
                            JOptionPane.showMessageDialog(
                                this,
                                "Setup failed: ${fixResult.exceptionOrNull()?.message}",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            )
                        }
                    } else {
                        configDir.mkdirs()
                        skipFile.createNewFile()
                    }
                } else {
                    val command = "CheckNetIsolation LoopbackExempt -a -n=\"Microsoft.MinecraftUWP_8wekyb3d8bbwe\""
                    val result = JOptionPane.showConfirmDialog(
                        this,
                        "Windows Minecraft needs loopback permission.\n\n" +
                        "Please run this command as Administrator:\n" +
                        "$command\n\n" +
                        "Copy command to clipboard?",
                        "Windows Setup Required",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    )
                    
                    if (result == JOptionPane.YES_OPTION) {
                        val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
                        clipboard.setContents(java.awt.datatransfer.StringSelection(command), null)
                        JOptionPane.showMessageDialog(
                            this,
                            "Command copied to clipboard!\n\n" +
                            "Steps:\n" +
                            "1. Open Command Prompt as Administrator\n" +
                            "2. Paste and run the command (Ctrl+V, Enter)\n" +
                            "3. Restart Minecraft\n\n" +
                            "Alternative: Right-click NovaClient.jar → Run as Administrator",
                            "Next Steps",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    }
                    
                    configDir.mkdirs()
                    skipFile.createNewFile()
                }
            }
        }.start()
    }
}
