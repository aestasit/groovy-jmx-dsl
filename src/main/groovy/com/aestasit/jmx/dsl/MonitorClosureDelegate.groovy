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

import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL as JmxUrl
import javax.naming.Context

class MonitorClosureDelegate {
  
  def String initialContextFactory
  def String protocolProvider
  def String username
  def String password
  def String title
  def String urlPattern = 'service:jmx:rmi://$ip:$port/jndi/rmi://$ip:$port/jmxrmi'
  def Long cols
  def Long rows
  
  private def env
  private def context
  
  public MonitorClosureDelegate() {
    
    println "Creating monitor."
    
    println "Creating environment properties."
    env = [:]
    env["java.naming.factory.initial"] = this.initialContextFactory
    env[JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES] = this.protocolProvider
    env[Context.SECURITY_PRINCIPAL] = this.username
    env[Context.SECURITY_CREDENTIALS] = this.password
  } 
  
  public MonitoringContext getContext() {
    if (context == null) {
      println "Creating window context."
      context = new MonitoringContext(title, cols, rows)
    }
    return context
  }
  
  void server(String serverUrl, Closure cl) {
    server([serverUrl], cl)
  }
  
  void server(List<String> serverIps, List<String> serverPorts, Closure cl) {
    def serverUrls = []
    serverIps.each { ip -> 
      serverPorts.each { port ->
        serverUrls.add(urlPattern.replace('$port', port.toString()).replace('$ip', ip))
      }
    }    
    server(serverUrls, cl)
  }
  
  void server(List<String> serverUrls, Closure cl) {
    def servers = []
    serverUrls.each { serverUrl ->
      println "Connecting to " + serverUrl 
      servers.add(JMXConnectorFactory.connect(new JmxUrl(serverUrl), env).MBeanServerConnection)
    } 
    cl.delegate = new ServerClosureDelegate(servers, getContext())
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }
}
