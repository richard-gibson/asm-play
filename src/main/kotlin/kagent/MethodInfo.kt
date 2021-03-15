package kagent

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.InstructionAdapter
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain
import java.util.ArrayList

class MethodInfoClassVisitor(writer: ClassWriter) : ClassVisitor(Opcodes.ASM8, writer) {
  override fun visitMethod(
    access: Int,
    name: String,
    desc: String?,
    signature: String?,
    exceptions: Array<String?>?
  ): MethodVisitor? {
    val baseMethodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
    println("visitMethod: access=$access name=$name desc=$desc signature=$signature exceptions=$exceptions")

    return MethodInfoMethodVistor(baseMethodVisitor, access, desc, name)
  }
}

class MethodInfoMethodVistor(
  baseMethodVisitor: MethodVisitor,
  access: Int,
  desc: String?,
  private val methodName: String
) : GeneratorAdapter(Opcodes.ASM8, baseMethodVisitor, access, methodName, desc) {
  private var annotationMatch: Boolean = false
  private var parameterNames: MutableList<String> = ArrayList()

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
    println("desc $desc")
    annotationMatch = (desc == ANNOTATION_DESC)
    return null
  }

  override fun visitParameter(name: String?, access: Int) {
    name?.let(parameterNames::add)
  }

  override fun visitCode() {
    super.visitCode()
    if (annotationMatch) {
      println("instrumenting method $methodName")
      InstructionAdapter(this).onEntry(methodName, parameterNames)
    }
  }

  override fun visitInsn(opcode: Int) {
    if (annotationMatch) {
      when (opcode) {
        Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN,
        Opcodes.DRETURN, Opcodes.ARETURN, Opcodes.RETURN ->
          InstructionAdapter(this).onExit()
      }
    }
    super.visitInsn(opcode)
  }

  companion object {
    private val ANNOTATION_DESC = "Lkagent/MethodInfo;"
    private val TIMER_TYPE = Type.getType("Lkagent/Timer")
  }

  private fun InstructionAdapter.onEntry(methodName: String, parameterNames: List<String>) {
    println("method $methodName, annotatated with $ANNOTATION_DESC params ${parameterNames.joinToString()}")
    anew(TIMER_TYPE)
    dup()
    visitLdcInsn(methodName)
    invokespecial(TIMER_TYPE.internalName, "<init>", "(Ljava/lang/String;)V", false)
    astore(TIMER_TYPE)
  }

  private fun InstructionAdapter.onExit() {
    aload(TIMER_TYPE)
    invokevirtual(TIMER_TYPE.internalName, "elapsedTime", "()V", false)
    pop2()
  }
}

class MethodInfoRewriter : ClassFileTransformer {
  override fun transform(
    loader: ClassLoader,
    className: String,
    redef: Class<*>?,
    pd: ProtectionDomain,
    bytes: ByteArray
  ): ByteArray {
    val reader = ClassReader(bytes)
    val writer = ClassWriter(reader, ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
    val methodInfoInstrumentor = MethodInfoClassVisitor(writer)
    reader.accept(methodInfoInstrumentor, ClassReader.EXPAND_FRAMES)
    return writer.toByteArray()
  }
}

object MethodInfoAgent {
  fun premain(args: String?, instrumentation: Instrumentation) {
    val transformer = MethodInfoRewriter()
    instrumentation.addTransformer(transformer)
  }
}

class Timer(val methodName: String) {
  private val start = System.currentTimeMillis()
  fun elapsedTime() = println("method $methodName took ${System.currentTimeMillis() - start} to execute")
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MethodInfo
