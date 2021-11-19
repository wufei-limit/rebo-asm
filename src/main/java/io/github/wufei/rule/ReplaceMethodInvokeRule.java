package io.github.wufei.rule;

import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

import io.github.wufei.base.BaseTransformRule;
import io.github.wufei.base.PreClassReader;
import io.github.wufei.config.ReplaceMethodInvokeConfig;

public class ReplaceMethodInvokeRule extends BaseTransformRule {
    private final ReplaceMethodInvokeConfig currReplaceMethodInvoke;

    public ReplaceMethodInvokeRule(Logger logger, ReplaceMethodInvokeConfig invokeConfig) {
        super(logger);
        this.currReplaceMethodInvoke = invokeConfig;
    }

    @Override
    protected boolean processFilter(PreClassReader preClassReader) {
        if (!preClassReader.headerStringContain(currReplaceMethodInvoke.getMethodName()) ||
                !preClassReader.headerStringContain(currReplaceMethodInvoke.getMethodDescriptor())) {
            return false;
        }
        if (isSkipClass(currReplaceMethodInvoke.getSkipClass())) {
            return false;
        }
        if (!currReplaceMethodInvoke.getClassName().isEmpty()) {
            return preClassReader.headerStringContain(currReplaceMethodInvoke.getClassName());
        } else {
            return true;
        }
    }

    @Override
    protected ClassVisitor createClassVisitor(int asmVersion, ClassWriter classWriter) {
        return new ReplaceMethodInvokeClassVisitor(asmVersion, classWriter);
    }

    public class ReplaceMethodInvokeClassVisitor extends ClassVisitor {

        public ReplaceMethodInvokeClassVisitor(int api, ClassWriter classWriter) {
            super(api, classWriter);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

            MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);

            return new ReplaceMethodInvokeMethodVisitor(delegate, name);
        }
    }

    private class ReplaceMethodInvokeMethodVisitor extends MethodVisitor {
        String currMethodName = "";

        public ReplaceMethodInvokeMethodVisitor(MethodVisitor delegate, String name) {
            super(Opcodes.ASM7, delegate);
            currMethodName = name;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            ReplaceMethodInvokeConfig config = currReplaceMethodInvoke;
            boolean isEqualsOwner = config.getClassName().isEmpty() || config.getClassName().equals(owner);
            boolean isEqualsMethodName = config.getMethodName().equals(name);
            boolean isEqualsMethodDescriptor = config.getMethodDescriptor().equals(descriptor);
            if (isEqualsOwner && isEqualsMethodName && isEqualsMethodDescriptor && !isExcludeOwner(config.getExcludeOwner(), owner)) {
                mIsChanged = true;
                logger.info("ReplaceMethodInvokeRule, className=" + className + ", owner=" + owner + ", name=" + name + ", descriptor=" + descriptor);
                opcode = Opcodes.INVOKESTATIC;
                owner = config.getReplaceClassName();
                name = config.getReplaceMethodName();
                descriptor = config.getReplaceMethodDescriptor();
            }

            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }

    private boolean isExcludeOwner(List<String> excludeOwner, String owner) {
        if (!currReplaceMethodInvoke.getClassName().isEmpty()) {
            return false;
        }
        for (String s : excludeOwner) {
            String className = s.replace(".class", "")
                    .replace(".java", "")
                    .replace(".", "/").trim();
            if (owner.contains(className)) {
                return true;
            }
        }
        return false;
    }
}
