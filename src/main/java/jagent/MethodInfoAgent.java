package jagent;

import kagent.MethodInfoRewriter;

import java.lang.instrument.Instrumentation;

public class MethodInfoAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        final MethodInfoRewriter methodInfoRewriter = new MethodInfoRewriter();
        instrumentation.addTransformer(methodInfoRewriter);
    }
}
