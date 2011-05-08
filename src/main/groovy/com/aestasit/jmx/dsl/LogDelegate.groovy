package com.aestasit.jmx.dsl

import java.text.SimpleDateFormat 


class LogDelegate extends MonitoringDelegate {
  
  def attributes         = []
  def String fileName    = null 
  def fileAppend         = false 
  
  private def File file  = null
  private def SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
  
  LogDelegate(modules, context) {
    super(modules, context)
    println "Creating log."
  }
  
  void poll() {
    createFile()
    modules.each { GroovyMBean m ->
      def values = getValues(m, attributes)
      def line = new StringBuilder()
      line.append df.format(new Date())
      line.append ";"
      if (!isSingleServerContext()) {
        line.append getServerName(m) 
        line.append ";"
      }
      line.append values.join(";")
      line.append "\n"
      file.append line
    }
  }
  
  private def createFile() {
    if (!file) {
      file = new File(fileName)
      if (!fileAppend) {
        if (!isSingleServerContext()) {
          file.text = "Time;Server;" + attributes.join(";") + "\n"
        } else {
          file.text = "Time;" + attributes.join(";") + "\n"
        }
      }
    }
  }
}
