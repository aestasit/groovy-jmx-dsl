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
