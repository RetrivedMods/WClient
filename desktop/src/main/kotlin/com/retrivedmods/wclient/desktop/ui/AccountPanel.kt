package com.retrivedmods.wclient.desktop.ui

import com.retrivedmods.wclient.desktop.core.DesktopAccountManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import java.awt.*
import javax.swing.*

class AccountPanel : JPanel() {
    
    private val accountListModel = DefaultListModel<String>()
    private val accountList = JList(accountListModel)
    private val addButton = JButton("Add Account")
    private val removeButton = JButton("Remove Account")
    private val selectButton = JButton("Select Account")
    private val refreshButton = JButton("Refresh")
    
    init {
        layout = BorderLayout(10, 10)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        
        val listPanel = JPanel(BorderLayout())
        listPanel.add(JLabel("Accounts:"), BorderLayout.NORTH)
        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        listPanel.add(JScrollPane(accountList), BorderLayout.CENTER)
        
        val buttonPanel = JPanel(GridLayout(4, 1, 5, 5))
        buttonPanel.add(addButton)
        buttonPanel.add(selectButton)
        buttonPanel.add(removeButton)
        buttonPanel.add(refreshButton)
        
        add(listPanel, BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.EAST)
        
        val infoPanel = JPanel(BorderLayout())
        val infoLabel = JLabel("<html>Selected: None</html>")
        infoPanel.add(infoLabel, BorderLayout.NORTH)
        add(infoPanel, BorderLayout.SOUTH)
        
        addButton.addActionListener {
            showAuthDialog()
        }
        
        removeButton.addActionListener {
            val selected = accountList.selectedValue
            if (selected != null) {
                DesktopAccountManager.removeAccount(selected)
                refreshAccountList()
            }
        }
        
        selectButton.addActionListener {
            val selected = accountList.selectedValue
            if (selected != null) {
                DesktopAccountManager.selectAccount(selected)
                refreshAccountList()
                updateInfoLabel(infoLabel)
            }
        }
        
        refreshButton.addActionListener {
            refreshAccountList()
            updateInfoLabel(infoLabel)
        }
        
        accountList.addListSelectionListener {
            updateInfoLabel(infoLabel)
        }
        
        refreshAccountList()
        updateInfoLabel(infoLabel)
    }
    
    private fun refreshAccountList() {
        accountListModel.clear()
        val accounts = DesktopAccountManager.getAccounts()
        val selected = DesktopAccountManager.getSelectedAccount()
        
        accounts.forEach { account ->
            val displayName = account.mcChain.displayName
            val prefix = if (displayName == selected?.mcChain?.displayName) "[✓] " else ""
            accountListModel.addElement("$prefix$displayName")
        }
    }
    
    private fun updateInfoLabel(label: JLabel) {
        val selected = DesktopAccountManager.getSelectedAccount()
        if (selected != null) {
            label.text = "<html><b>Selected:</b> ${selected.mcChain.displayName}<br>" +
                    "<b>Xbox ID:</b> ${selected.mcChain.xblXsts.userHash}</html>"
        } else {
            label.text = "<html>Selected: None</html>"
        }
    }
    
    private fun showAuthDialog() {
        val dialog = JDialog(SwingUtilities.getWindowAncestor(this) as? JFrame, "Add Account", false)
        dialog.layout = BorderLayout(10, 10)
        dialog.setSize(500, 300)
        dialog.setLocationRelativeTo(this)
        
        val messageArea = JTextArea()
        messageArea.isEditable = false
        messageArea.lineWrap = true
        messageArea.wrapStyleWord = true
        messageArea.text = "Initializing authentication..."
        
        val scrollPane = JScrollPane(messageArea)
        dialog.add(scrollPane, BorderLayout.CENTER)
        
        val closeButton = JButton("Close")
        closeButton.isEnabled = false
        closeButton.addActionListener { dialog.dispose() }
        
        val buttonPanel = JPanel()
        buttonPanel.add(closeButton)
        dialog.add(buttonPanel, BorderLayout.SOUTH)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val session = DesktopAccountManager.authenticateNewAccount { url ->
                    SwingUtilities.invokeLater {
                        messageArea.text = "Please visit the following URL to authenticate:\n\n$url\n\n" +
                                "Waiting for authentication..."
                    }
                }
                
                SwingUtilities.invokeLater {
                    messageArea.text = "Successfully authenticated as: ${session.mcChain.displayName}\n\n" +
                            "Account has been added!"
                    closeButton.isEnabled = true
                    refreshAccountList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                SwingUtilities.invokeLater {
                    messageArea.text = "Authentication failed:\n${e.message}\n\n${e.stackTraceToString()}"
                    closeButton.isEnabled = true
                }
            }
        }
        
        dialog.isVisible = true
    }
}
