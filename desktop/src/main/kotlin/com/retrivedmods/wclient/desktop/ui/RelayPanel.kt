package com.retrivedmods.wclient.desktop.ui

import com.retrivedmods.wclient.desktop.core.DesktopAccountManager
import com.retrivedmods.wclient.desktop.core.RelayService
import java.awt.*
import javax.swing.*

class RelayPanel : JPanel() {
    
    private val serverField = JTextField("play.lbsg.net")
    private val portField = JTextField("19132")
    private val localPortField = JTextField("19132")
    private val startButton = JButton("Start Relay")
    private val stopButton = JButton("Stop Relay")
    private val statusLabel = JLabel("Status: Stopped")
    private val connectionInfoLabel = JLabel("")
    
    init {
        layout = BorderLayout(10, 10)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        
        val configPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.insets = Insets(5, 5, 5, 5)
        
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        configPanel.add(JLabel("Server:"), gbc)
        
        gbc.gridx = 1
        gbc.weightx = 1.0
        configPanel.add(serverField, gbc)
        
        gbc.gridx = 0
        gbc.gridy = 1
        gbc.weightx = 0.0
        configPanel.add(JLabel("Port:"), gbc)
        
        gbc.gridx = 1
        gbc.weightx = 1.0
        configPanel.add(portField, gbc)
        
        gbc.gridx = 0
        gbc.gridy = 2
        gbc.weightx = 0.0
        configPanel.add(JLabel("Local Port:"), gbc)
        
        gbc.gridx = 1
        gbc.weightx = 1.0
        configPanel.add(localPortField, gbc)
        
        add(configPanel, BorderLayout.NORTH)
        
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 10))
        buttonPanel.add(startButton)
        buttonPanel.add(stopButton)
        
        val centerPanel = JPanel(BorderLayout())
        centerPanel.add(buttonPanel, BorderLayout.NORTH)
        
        val statusPanel = JPanel()
        statusPanel.layout = BoxLayout(statusPanel, BoxLayout.Y_AXIS)
        statusPanel.add(statusLabel)
        statusPanel.add(Box.createVerticalStrut(10))
        statusPanel.add(connectionInfoLabel)
        centerPanel.add(statusPanel, BorderLayout.CENTER)
        
        add(centerPanel, BorderLayout.CENTER)
        
        val infoPanel = JPanel()
        infoPanel.layout = BoxLayout(infoPanel, BoxLayout.Y_AXIS)
        infoPanel.border = BorderFactory.createTitledBorder("How to Connect")
        
        infoPanel.add(JLabel("Windows: Servers → Add Server → localhost:19132"))
        infoPanel.add(JLabel("Mobile: Friends → LAN Games"))
        
        add(infoPanel, BorderLayout.SOUTH)
        
        stopButton.isEnabled = false
        
        startButton.addActionListener {
            val account = DesktopAccountManager.getSelectedAccount()
            if (account == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "Please select an account first",
                    "No Account Selected",
                    JOptionPane.WARNING_MESSAGE
                )
                return@addActionListener
            }
            
            try {
                val server = serverField.text
                val port = portField.text.toInt()
                val localPort = localPortField.text.toInt()
                
                RelayService.startRelay(server, port, localPort, account)
                
                startButton.isEnabled = false
                stopButton.isEnabled = true
                serverField.isEnabled = false
                portField.isEnabled = false
                localPortField.isEnabled = false
                statusLabel.text = "Status: Running"
                
                val localIP = try {
                    java.net.InetAddress.getLocalHost().hostAddress
                } catch (e: Exception) {
                    "your-local-ip"
                }
                
                connectionInfoLabel.text = "<html>" +
                    "<b>Relay Active</b><br>" +
                    "Windows: localhost:$localPort<br>" +
                    "Mobile: LAN discovery<br>" +
                    "Network: $localIP:$localPort" +
                    "</html>"
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to start relay: ${e.message}",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
        
        stopButton.addActionListener {
            RelayService.stopRelay()
            
            startButton.isEnabled = true
            stopButton.isEnabled = false
            serverField.isEnabled = true
            portField.isEnabled = true
            localPortField.isEnabled = true
            statusLabel.text = "Status: Stopped"
            connectionInfoLabel.text = ""
        }
    }
}
