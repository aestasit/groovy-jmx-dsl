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
