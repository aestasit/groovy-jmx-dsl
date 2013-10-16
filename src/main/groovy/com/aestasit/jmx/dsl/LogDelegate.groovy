/*
 * Copyright (C) 2011-2013 Aestas/IT
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
