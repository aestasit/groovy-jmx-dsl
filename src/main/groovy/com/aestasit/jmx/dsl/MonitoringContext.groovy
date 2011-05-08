package com.aestasit.jmx.dsl

import groovy.swing.SwingBuilder;
import java.awt.Color 

import javax.swing.JFrame;
import javax.swing.WindowConstants as WC

class MonitoringContext {
  
  def SwingBuilder swing
  def JFrame frame
  
  private def String title  = "JMX monitoring"
  private def Long   cols   = 2
  private def Long   rows   = 2
  
  private def chartDelegates = [] as List
  private def logDelegates = [] as List
  private def threads = [] as List
  
  public MonitoringContext(String title, Long cols, Long rows) {
    if (title) {
      this.title = title
    }
    if (cols) {
      this.cols = cols
    }
    if (rows) {
      this.rows = rows
    }
    println "Creating window context: rows=" + rows + ", cols=" + cols + ", title='" + title + "'"
  }
  
  private def createWindow() {
    println "Creating window with grid layout: " + rows + "x" + cols
    this.swing = new SwingBuilder()
    this.frame = swing.frame(title: title, defaultCloseOperation: WC.EXIT_ON_CLOSE) {
      menuBar() {
        menu(text: "File", mnemonic: 'F') {
          menuItem(text: "Exit", mnemonic: 'X', actionPerformed: { stopThreads() })
        }
      }
      panel(id: 'canvas') {
        gridLayout(cols: cols, rows: rows)
        lineBorder(color:Color.WHITE, thickness:1, parent:true)
      }
    }
  }
  
  def addChart(ChartDelegate delegate) {
    chartDelegates.add(delegate)
  }
  
  def addLog(LogDelegate delegate) {
    logDelegates.add(delegate)
  }
  
  def start() {
    if (chartDelegates.size() > 0) {
      createWindow()
      chartDelegates.each { ChartDelegate delegate ->
        delegate.onStart()
      }
      frame.pack()
      frame.show()
      chartDelegates.each { ChartDelegate delegate ->
        def thread = new Thread(delegate)
        threads.add(thread)
        thread.start()
      }
    }
    if (logDelegates.size() > 0) {
      logDelegates.each { LogDelegate delegate ->
        def thread = new Thread(delegate)
        threads.add(thread)
        thread.start()
      }
    }
  }
  
  private def stopThreads() {
    threads.each { it.stop() }
    swing.dispose()
  }
}
