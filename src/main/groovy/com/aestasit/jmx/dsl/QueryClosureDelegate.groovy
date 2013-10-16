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

class QueryClosureDelegate {
  
  private def queryResult
  private def MonitoringContext context
  
  QueryClosureDelegate(queryResult, context) {
    println "Creating query."
    this.queryResult = queryResult
    this.context = context
  }
  
  void chart(Closure cl) {
    def modules = getAllModules()
    def delegate = new ChartDelegate(modules, context)
    context.addChart(delegate)
    cl.delegate = delegate
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }
  
  void log(Closure cl) {
    def modules = getAllModules()
    def delegate = new LogDelegate(modules, context)
    context.addLog(delegate)
    cl.delegate = delegate
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }  
  
  void nameContains(String filter, Closure cl) {
    def modules = getAllModules().findAll { it.name().canonicalName.contains(filter) }
    logFilterResult(filter, modules)
    callFilterClosure(cl, modules)
  }  
  
  void nameMatches(String filter, Closure cl) {
    def modules = getAllModules().findAll { it.name().canonicalName.matches(filter) }
    logFilterResult(filter, modules)
    callFilterClosure(cl, modules)
  }
  
  private def getAllModules() {
    def modules = []
    queryResult.each { QueryResult entry ->
      modules.addAll(entry.result.collect { new GroovyMBean(entry.server, it) })
    }
    return modules.sort{ it.name().canonicalName }
  }
  
  private void logFilterResult(String filter, modules) {
    println "Filter: '" + filter + "' matched " + modules.size() + " result(s)"
    modules.each {
      println "Name: " + it.name().canonicalName
    }
  }
  
  private callFilterClosure(Closure cl, List modules) {
    cl.delegate = new FilterClosureDelegate(modules, context)
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }
}
