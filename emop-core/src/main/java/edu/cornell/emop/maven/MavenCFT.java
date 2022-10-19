package edu.cornell.emop.maven;

import edu.cornell.emop.maven.asm.ClassReader;
import edu.cornell.emop.maven.asm.ClassVisitor;
import edu.cornell.emop.maven.asm.ClassWriter;
import edu.cornell.emop.maven.asm.MethodVisitor;
import edu.cornell.emop.maven.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/** This class is from STARTS. **/

public final class MavenCFT implements ClassFileTransformer {

    private static class MavenMethodVisitor extends MethodVisitor {
        private final String mavenMethodName;
        private final String mavenMethodDesc;

        private final String mavenInterceptorName;

        public MavenMethodVisitor(String interceptorName, String methodName, String methodDesc, MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
            this.mavenInterceptorName = interceptorName;
            this.mavenMethodName = methodName;
            this.mavenMethodDesc = methodDesc;
        }

        // we will be rewriting the bytecode for SurefirePlugin's execute() method
        public void visitCode() {
            // load the "this" object (Aload 0 gets us the first argument)
            // since execute() is not a static method, the first argument is the this object
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            // mavenInterceptorName = "edu/illinois/starts/maven/SurefireMojoInterceptor"
            // mavenMethodName = "execute"
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, mavenInterceptorName, mavenMethodName,
                    mavenMethodDesc.replace("(", "(Ljava/lang/Object;"), false);
            // Adding in SurefireMojoInterceptor.execute(Object o) right before the SurefirePlugin.execute()
            mv.visitCode();
        }
    }

    private static class MavenClassVisitor extends ClassVisitor {
        private final String mavenClassName;
        private final String mavenInterceptorName;

        public MavenClassVisitor(String className, String interceptorName, ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
            this.mavenClassName = className;
            this.mavenInterceptorName = interceptorName;
        }

        // processing byte code in the method
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            // otherwise, use their visitor (does nothing - returns bytecode back)
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            if (name.equals("execute") && desc.equals("()V")) {
                // if we care about this method, we want to use a different visitor
                mv = new MavenMethodVisitor(mavenInterceptorName, name, desc, mv);
            }
            return mv;
        }
    }

    /**
     * Method starting bytecode transformation, gets called by instrumentation.retransform().
     */
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
                            throws IllegalClassFormatException {
        String abstractSurefireMojo = "org/apache/maven/plugin/surefire/AbstractSurefireMojo";
        String surefirePlugin = "org/apache/maven/plugin/surefire/SurefirePlugin";
        String surefireMojoInterceptor = "edu/cornell/emop/maven/SurefireMojoInterceptor";
        if (className.equals(abstractSurefireMojo) || className.equals(surefirePlugin)) {
            return addInterceptor(className, classfileBuffer, surefireMojoInterceptor);
        } else {
            return null;
        }
    }

    private byte[] addInterceptor(String className, byte[] classfileBuffer, String interceptorName) {
        ClassReader classReader = new ClassReader(classfileBuffer); // buffer used to instantiate classReader in ASM
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        // creating visitor for surefire
        ClassVisitor visitor = new MavenClassVisitor(className, interceptorName, classWriter);
        classReader.accept(visitor, 0);
        return classWriter.toByteArray();
    }
}

