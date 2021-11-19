package io.github.wufei.rule;


import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.wufei.base.BaseTransformRule;
import io.github.wufei.base.PreClassReader;
import io.github.wufei.config.ReplaceMethodCodeConfig;
import io.github.wufei.parse.MethodDescriptionParser;

public class ReplaceMethodCodeRule extends BaseTransformRule {

    private final ReplaceMethodCodeConfig currReplaceMethodCode;


    public ReplaceMethodCodeRule(Logger logger, ReplaceMethodCodeConfig replaceMethodCodeConfig) {
        super(logger);
        this.currReplaceMethodCode = replaceMethodCodeConfig;
    }

    @Override
    protected ClassVisitor createClassVisitor(int asmVersion, ClassWriter classWriter) {
        return new ReplaceMethodCodeClassVisitor(asmVersion, classWriter);
    }


    @Override
    protected boolean processFilter(PreClassReader preClassReader) {
        //  logger.info("HookMethodTransformTask handleFilter, className=" + className);
        boolean result = preClassReader.equalClassName(currReplaceMethodCode.getClassName());
//        System.out.println("ReplaceMethodCodeRule, className=" + className + ", result=" + result);
        return result;
    }

    public class ReplaceMethodCodeClassVisitor extends ClassVisitor {
        public ReplaceMethodCodeClassVisitor(int api, ClassWriter classWriter) {
            super(api, classWriter);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor delegate;
            if (currReplaceMethodCode.getClassName().equals(className) &&
                    currReplaceMethodCode.getMethodName().equals(name) &&
                    currReplaceMethodCode.getMethodDescriptor().equals(descriptor)) {
                mIsChanged = true;
                logger.info("ReplaceMethodCodeRule  className=" + className + ", name=" + name + ", descriptor=" + descriptor);
                if (access == 0x2 || access == 0x0 || access == 0x4) { //private , def , protected
                    access = 0x1;
                }
                if (access == 0xA || access == 0x8 || access == 0xC) { //private static , def static , protected static
                    access = 0x9;
                }
                delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new ReplaceMethodCodeMethodVisitor(delegate, currReplaceMethodCode, access);
            }
            delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
            return delegate;
        }

    }

    private class ReplaceMethodCodeMethodVisitor extends MethodVisitor {
        private final ReplaceMethodCodeConfig replaceMethodCodeConfig;
        private final int access;

        public ReplaceMethodCodeMethodVisitor(MethodVisitor delegate, ReplaceMethodCodeConfig replaceMethodCodeConfig, int access) {
            super(Opcodes.ASM7, delegate);
            this.replaceMethodCodeConfig = replaceMethodCodeConfig;
            this.access = access;
        }

        @Override
        public void visitCode() {
            if (mv != null) {
                boolean isStatic = (access == 0x9 || access == 0xA0 || access == 0x8 || access == 0xC);
                if (!isStatic) { //private , def , protected
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                }
                int []paramsOps= MethodDescriptionParser.getParmaOp(replaceMethodCodeConfig.getMethodDescriptor());
                for (int i = 0; i < paramsOps.length; i++) {
                    if (isStatic) {
                        mv.visitVarInsn(paramsOps[i], i); //private static , def static , protected static
                    } else {
                        mv.visitVarInsn(paramsOps[i], i + 1);
                    }
                }
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, replaceMethodCodeConfig.getReplaceClassName(),
                        replaceMethodCodeConfig.getReplaceMethodName(),
                        replaceMethodCodeConfig.getReplaceMethodDescriptor(), false);
                int returnCmd = getReturnCmd(replaceMethodCodeConfig.getMethodDescriptor());
                if (replaceMethodCodeConfig.isInterrupt()) {
                    mv.visitInsn(returnCmd);
                } else if (!replaceMethodCodeConfig.returnTypeIsVoid()) {
                    mv.visitInsn(Opcodes.POP);
                }

            }
        }

    }

}
