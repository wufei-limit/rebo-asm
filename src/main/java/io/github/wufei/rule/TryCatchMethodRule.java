package io.github.wufei.rule;

import io.github.wufei.base.BaseTransformRule;
import io.github.wufei.base.PreClassReader;
import io.github.wufei.parse.MethodDescriptionParser;
import io.github.wufei.config.TryCatchMethodConfig;
import org.gradle.api.logging.Logger;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

public class TryCatchMethodRule extends BaseTransformRule {
    private final TryCatchMethodConfig currTryCatchConfig;

    public TryCatchMethodRule(Logger logger, TryCatchMethodConfig tryCatchConfig) {
        super(logger);
        this.currTryCatchConfig = tryCatchConfig;
    }

    @Override
    protected boolean processFilter(PreClassReader preClassReader) {
        return preClassReader.equalClassName(currTryCatchConfig.getClassName());
    }

    @Override
    protected ClassVisitor createClassVisitor(int asmVersion, ClassWriter classWriter) {
        return new TryCatchMethodClassVisitor(asmVersion, classWriter);
    }

    public class TryCatchMethodClassVisitor extends ClassVisitor {
        public TryCatchMethodClassVisitor(int api, ClassWriter classWriter) {
            super(api, classWriter);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (currTryCatchConfig.getClassName().equals(className) &&
                    currTryCatchConfig.getMethodName().equals(name) &&
                    currTryCatchConfig.getMethodDescriptor().equals(descriptor)) {
                logger.info("TryCatchMethodRule, className=" + className + ", name=" + name + ", descriptor=" + descriptor);
                mIsChanged = true;
                return new TryCatchMethodMethodVisitor(api, delegate, access, name, descriptor);
            }
            return delegate;
        }

    }

    private class TryCatchMethodMethodVisitor extends AdviceAdapter {
        private int access;
        private Label labelStart = new Label();
        private Label labelEnd = new Label();
        private Label labelTarget = new Label();

        protected TryCatchMethodMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String desc) {
            super(api, methodVisitor, access, name, desc);
            this.access = access;
        }

        protected void onMethodEnter() {
            super.onMethodEnter();
            mv.visitLabel(this.labelStart);
            mv.visitTryCatchBlock(this.labelStart, this.labelEnd, this.labelTarget, "java/lang/Exception");
        }

        public void visitMaxs(int maxStack, int maxLocals) {
            mv.visitLabel(this.labelEnd);
            mv.visitLabel(this.labelTarget);
            //-----------
            int local1 = this.newLocal(Type.getType("Ljava/lang/Exception;"));
            mv.visitVarInsn(Opcodes.ASTORE, local1);
            boolean isStatic = (access == 0x9 || access == 0xA0 || access == 0x8 || access == 0xC);
            if (!isStatic) { //private , def , protected
                mv.visitVarInsn(Opcodes.ALOAD, 0);
            }
            int[] paramsOps = MethodDescriptionParser.getParmaOp(currTryCatchConfig.getMethodDescriptor());
            for (int i = 0; i < paramsOps.length; i++) {
                if (isStatic) {
                    mv.visitVarInsn(paramsOps[i], i); //private static , def static , protected static
                } else {
                    mv.visitVarInsn(paramsOps[i], i + 1);
                }
            }
            mv.visitVarInsn(Opcodes.ALOAD, local1);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, currTryCatchConfig.getThrowClassName(),
                    currTryCatchConfig.getThrowMethodName(),
                    currTryCatchConfig.getThrowMethodDescriptor(), false);
            int returnCmd = getReturnCmd(currTryCatchConfig.getThrowMethodDescriptor());
            mv.visitInsn(returnCmd);
            super.visitMaxs(maxStack, maxLocals);
        }
    }
}
