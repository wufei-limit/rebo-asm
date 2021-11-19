package io.github.wufei.rule;


import org.gradle.api.logging.Logger;
import org.objectweb.asm.*;

import io.github.wufei.base.BaseTransformRule;
import io.github.wufei.base.PreClassReader;
import io.github.wufei.config.ReplaceClassInvokeConfig;

public class ReplaceClassInvokeRule extends BaseTransformRule {
    private final ReplaceClassInvokeConfig replaceClassInvokeConfig;

    public ReplaceClassInvokeRule(Logger logger, ReplaceClassInvokeConfig replaceClassInvokeConfig) {
        super(logger);
        this.replaceClassInvokeConfig = replaceClassInvokeConfig;
    }

    @Override
    protected boolean processFilter(PreClassReader preClassReader) {
        return preClassReader.headerStringContain(replaceClassInvokeConfig.getClassName()) &&
                !isSkipClass(replaceClassInvokeConfig.getSkipClass());
    }

    @Override
    protected ClassVisitor createClassVisitor(int asmVersion, ClassWriter classWriter) {
        return new ReplaceClassInvokeClassVisitor(asmVersion, classWriter);
    }

    public class ReplaceClassInvokeClassVisitor extends ClassVisitor {
        public ReplaceClassInvokeClassVisitor(int api, ClassWriter classWriter) {
            super(api, classWriter);
        }


        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            //System.out.println("ReplaceClassInvokeRule, visitField, name=" + name + ", name=" + name + ", descriptor=" + descriptor);
//            if (descriptor.contains(replaceClassInvokeConfig.getClassDesc())) {
//                String descriptorBack = descriptor;
//                descriptor = descriptor.replace(replaceClassInvokeConfig.getClassDesc(), replaceClassInvokeConfig.getReplaceClassDesc());
//                mIsChanged = true;
//                logger.info("ReplaceClassInvokeRule, visitField, className=" + className + ", descriptor=" + descriptorBack + ", replaceDescriptor=" + descriptor);
//            }
            return super.visitField(access, name, descriptor, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            // System.out.println("ReplaceClassInvokeRule, visitMethod, name=" + name + ", descriptor=" + descriptor);
//            if (descriptor.contains(replaceClassInvokeConfig.getClassDesc())) {
//                String descriptorBack = descriptor;
//                descriptor = descriptor.replace(replaceClassInvokeConfig.getClassDesc(), replaceClassInvokeConfig.getReplaceClassDesc());
//                mIsChanged = true;
//                logger.info("ReplaceClassInvokeRule, visitMethod, className=" + className + ", descriptor=" + descriptorBack + ", replaceDescriptor=" + descriptor);
//            }
            MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new ReplaceClassInvokeMethodVisitor(delegate);
        }


    }


    private class ReplaceClassInvokeMethodVisitor extends MethodVisitor {
        public ReplaceClassInvokeMethodVisitor(MethodVisitor delegate) {
            super(Opcodes.ASM7, delegate);

        }

        @Override
        public void visitParameter(final String name, final int access) {
            //System.out.println("ReplaceClassInvokeRule, visitParameter, name=" + name + ", access=" + access);
            super.visitParameter(name, access);
        }

        public void visitInvokeDynamicInsn(
                final String name,
                final String descriptor,
                final Handle bootstrapMethodHandle,
                final Object... bootstrapMethodArguments) {
            //System.out.println("ReplaceClassInvokeRule, visitInvokeDynamicInsn, name=" + name + ", descriptor=" + descriptor);
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }

        @Override
        public void visitLdcInsn(Object value) {
            super.visitLdcInsn(value);
            //System.out.println("ReplaceClassInvokeRule, visitInvokeDynamicInsn, value=" + value.toString());
        }


        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (opcode == Opcodes.NEW && type.equals(replaceClassInvokeConfig.getClassName())) {
                String typeBack = type;
                type = replaceClassInvokeConfig.getReplaceClassName();
                mIsChanged = true;
                logger.info("ReplaceClassInvokeRule, visitTypeInsn, className=" + className + ", type=" + typeBack + ", replaceType=" + type);
            }
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitFieldInsn(
                final int opcode, String owner, String name, String descriptor) {
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>") && owner.equals(replaceClassInvokeConfig.getClassName())) {
                owner = replaceClassInvokeConfig.getReplaceClassName();
                mIsChanged = true;
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }
}
