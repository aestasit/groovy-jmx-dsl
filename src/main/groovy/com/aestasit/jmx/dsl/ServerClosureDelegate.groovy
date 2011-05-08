package com.aestasit.jmx.dsl

import javax.management.ObjectName

class ServerClosureDelegate {
  
  private def servers
  private def MonitoringContext context
  
  ServerClosureDelegate(servers, context) {
    println "Creating server."
    this.servers = servers
    this.context = context
  }
  
  void query(String objectName, Closure cl) {
    def query = new ObjectName(objectName)
    cl.delegate = new QueryClosureDelegate(makeQuery(servers, query, null), context)
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }
  
  private def makeQuery(servers, nameQuery, filter) {
    def resultCollection = []
    servers.each { server ->
      resultCollection.add(new QueryResult(server, server.queryNames(nameQuery, filter)))
    }
    def totalSize = 0
    resultCollection.each { totalSize += it.result.length  }
    println "Query: '" + nameQuery.canonicalName + "'" + 
        (filter ? " (" + filter + ")" : "") + 
        " returned " + totalSize + " result(s)"
    resultCollection.each {
      it.result.each { println "Name: " + it  }
    }
    return resultCollection
  }
}
