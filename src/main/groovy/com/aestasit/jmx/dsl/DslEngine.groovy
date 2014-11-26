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

class DslEngine {
  
  public static void main(String[] args) {
    if (args.length != 1) {
      println("Usage: DslEngine <ScriptFileName>")
    }
    runEngine(new File(args[0]))
  }
  
  private static void runEngine(File dsl) {
    
    Script dslScript = new GroovyShell().parse(dsl.text)
    
    dslScript.metaClass = createExpandoMetaClass(dslScript.class, { ExpandoMetaClass emc ->
      emc.monitor = { Closure cl ->
        cl.delegate = new MonitorClosureDelegate()
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        cl.delegate.context.start()
      }
    })
    
    dslScript.run()
  }
  
  private static ExpandoMetaClass createExpandoMetaClass(Class clazz, Closure cl) {
    ExpandoMetaClass emc = new ExpandoMetaClass(clazz, false)
    cl(emc)
    emc.initialize()
    return emc
  }
}
