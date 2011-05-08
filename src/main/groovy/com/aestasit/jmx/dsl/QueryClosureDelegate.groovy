package com.aestasit.jmx.dsl

import groovy.lang.Closure;

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
