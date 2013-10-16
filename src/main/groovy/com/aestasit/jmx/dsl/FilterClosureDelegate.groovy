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

class FilterClosureDelegate {
  
  private def MonitoringContext context
  private def modules
  
  FilterClosureDelegate(modules, context) {
    println "Creating filter."
    this.modules = modules
    this.context = context
  }  
  
  void chart(Closure cl) {
    def delegate = new ChartDelegate(modules, context)
    context.addChart(delegate) 
    cl.delegate = delegate 
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }
  
  void log(Closure cl) {
    def delegate = new LogDelegate(modules, context)
    context.addLog(delegate)
    cl.delegate = delegate
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }
}
