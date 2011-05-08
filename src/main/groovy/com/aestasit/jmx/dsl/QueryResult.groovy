package com.aestasit.jmx.dsl

import javax.management.MBeanServerConnection 

class QueryResult {
  
  def MBeanServerConnection server
  def String[] result
  
  QueryResult(server, result) {
    this.server = server
    this.result = result
  }
}
