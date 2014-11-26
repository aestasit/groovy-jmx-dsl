/*
 * Copyright (C) 2011-2014 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aestasit.jmx.dsl

import groovy.swing.SwingBuilder
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
