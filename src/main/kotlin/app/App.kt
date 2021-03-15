package app

import kagent.MethodInfo

// import kagent.MethodInfoClassVisitor
// import kagent.Timer
// import org.objectweb.asm.ClassReader

class SampleClass() {
  @MethodInfo
  fun instrumentedMethod(i: Int, s: String) {
    println("instrumentedMethod: $i, $s")
  }

  fun nonInstrumentedMethod(i: Int, s: Boolean) {
    println("nonInstrumentedMethod, $i, $s")
  }
}

fun main() {
  val sample = SampleClass()
  sample.instrumentedMethod(2, "s")
  sample.nonInstrumentedMethod(2, false)
}

// fun main() {
//  val t = Timer("foo")
//  t.elapsedTime()
//  ClassReader(SampleClass::class.java.canonicalName)
//    .accept(MethodInfoClassVisitor(), 0)
// }
