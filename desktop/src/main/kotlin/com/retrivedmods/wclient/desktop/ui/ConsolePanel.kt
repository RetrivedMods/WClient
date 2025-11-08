package com.retrivedmods.wclient.desktop.ui

import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

class ConsolePanel : JPanel() {
    
    private val consoleArea = JTextArea()
    private val clearButton = JButton("Clear")
    
    init {
        layout = BorderLayout(10, 10)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        
        consoleArea.isEditable = false
        consoleArea.lineWrap = true
        consoleArea.wrapStyleWord = true
        
        add(JScrollPane(consoleArea), BorderLayout.CENTER)
        
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        buttonPanel.add(clearButton)
        add(buttonPanel, BorderLayout.SOUTH)
        
        clearButton.addActionListener {
            consoleArea.text = ""
        }
    }
    
    fun appendMessage(message: String) {
        consoleArea.append("$message\n")
        consoleArea.caretPosition = consoleArea.document.length
    }
}
