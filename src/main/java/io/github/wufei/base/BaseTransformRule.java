package io.github.wufei.base;

import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.util.List;

public abstract class BaseTransformRule implements ITransformRule {
    public Logger logger;
    public String className;
    public PreClassReader mPreClassReader;
    public boolean mIsChanged = false;
    public BaseTransformRule(Logger logger) {
        this.logger = logger;
    }

    @Override
    public byte[] processClassFile(byte[] srcData) {
        ClassReader classReader = createClassReader(srcData);
        ClassWriter classWriter = createClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = createClassVisitor(Opcodes.ASM7, classWriter);
        classReader.accept(cv, classReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    @Override
    public boolean filterClass(PreClassReader preClassReader) {
        mPreClassReader = preClassReader;
        className = mPreClassReader.getClassName();
        return processFilter(mPreClassReader);
    }

    protected abstract boolean processFilter(PreClassReader preClassReader);

    protected ClassReader createClassReader(byte[] srcData) {
        return new ClassReader(srcData);
    }

    protected ClassWriter createClassWriter(ClassReader classReader, int COMPUTE_TYPE) {
        return new ClassWriter(classReader, COMPUTE_TYPE);
    }

    protected abstract ClassVisitor createClassVisitor(int asmVersion, ClassWriter classWriter);
    public int getReturnCmd(String c) {
        int opcodes = Opcodes.ARETURN; //引用地址

        if (c.endsWith(")S") ||  //short
                c.endsWith(")I") || //int
                c.endsWith(")Z") || //boolean
                c.endsWith(")B") || //byte
                c.endsWith(")C")  //char
        ) {
            opcodes = Opcodes.IRETURN;
        }
        if (c.endsWith(")F")) {  //float
            opcodes = Opcodes.FRETURN;
        }
        if (c.endsWith(")D")) {  //double
            opcodes = Opcodes.DRETURN;
        }
        if (c.endsWith(")J")) { //long
            opcodes = Opcodes.LRETURN;
        }
        if (c.endsWith(")V")) { //void
            opcodes = Opcodes.RETURN;
        }
        return opcodes;
    }

    @Override
    public boolean isChanged() {
        return mIsChanged;
    }

    public boolean isSkipClass(List<String> excludeClass) {
        for (String s : excludeClass) {
            String trimClass = s.replace(".class", "")
                    .replace(".java", "")
                    .replace(".", "/").trim();
            if (trimClass.equals(className)) {
                return true;
            }
        }
        return false;
    }
}
