package com.retrivedmods.wclient.desktop

import com.retrivedmods.wclient.desktop.ui.MainWindow
import javax.swing.SwingUtilities
import javax.swing.UIManager

fun main() {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    SwingUtilities.invokeLater {
        MainWindow()
    }
}
