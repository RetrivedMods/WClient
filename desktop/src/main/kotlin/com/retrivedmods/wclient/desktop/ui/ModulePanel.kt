package com.retrivedmods.wclient.desktop.ui

import com.retrivedmods.wclient.desktop.core.DesktopModuleManager
import java.awt.*
import javax.swing.*
import javax.swing.table.DefaultTableModel

class ModulePanel : JPanel() {
    
    private val tableModel = DefaultTableModel(arrayOf("Module", "Category", "Enabled"), 0)
    private val table = JTable(tableModel)
    private val toggleButton = JButton("Toggle")
    private val refreshButton = JButton("Refresh")
    private val saveButton = JButton("Save Config")
    private val loadButton = JButton("Load Config")
    
    init {
        layout = BorderLayout(10, 10)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.setDefaultEditor(Object::class.java, null)
        
        add(JScrollPane(table), BorderLayout.CENTER)
        
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 10))
        buttonPanel.add(toggleButton)
        buttonPanel.add(refreshButton)
        buttonPanel.add(saveButton)
        buttonPanel.add(loadButton)
        
        add(buttonPanel, BorderLayout.SOUTH)
        
        val infoPanel = JPanel(BorderLayout())
        val infoLabel = JLabel("<html>Use commands in-game: .help, .&lt;module&gt; to toggle<br>" +
                "Example: .killaura, .fly, .speed</html>")
        infoPanel.add(infoLabel, BorderLayout.NORTH)
        add(infoPanel, BorderLayout.NORTH)
        
        toggleButton.addActionListener {
            val selectedRow = table.selectedRow
            if (selectedRow >= 0) {
                val moduleName = tableModel.getValueAt(selectedRow, 0) as String
                DesktopModuleManager.toggleModule(moduleName)
                refreshModuleList()
            }
        }
        
        refreshButton.addActionListener {
            refreshModuleList()
        }
        
        saveButton.addActionListener {
            DesktopModuleManager.saveConfig()
            JOptionPane.showMessageDialog(this, "Configuration saved", "Success", JOptionPane.INFORMATION_MESSAGE)
        }
        
        loadButton.addActionListener {
            DesktopModuleManager.loadConfig()
            refreshModuleList()
            JOptionPane.showMessageDialog(this, "Configuration loaded", "Success", JOptionPane.INFORMATION_MESSAGE)
        }
        
        refreshModuleList()
    }
    
    private fun refreshModuleList() {
        tableModel.rowCount = 0
        
        val modules = DesktopModuleManager.getModules()
        modules.forEach { module ->
            tableModel.addRow(arrayOf(
                module.name,
                module.category,
                if (module.enabled) "✓" else "✗"
            ))
        }
    }
}
