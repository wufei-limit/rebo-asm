package io.github.wufei.rule;


import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import io.github.wufei.base.BaseTransformRule;
import io.github.wufei.base.PreClassReader;
import io.github.wufei.config.ReplaceSuperClassConfig;

public class ReplaceSuperClassRule extends BaseTransformRule {
    final ReplaceSuperClassConfig replaceSuperClassConfig;

    public ReplaceSuperClassRule(Logger logger, ReplaceSuperClassConfig replaceSuperClassConfig) {
        super(logger);
        this.replaceSuperClassConfig = replaceSuperClassConfig;
    }

    @Override
    protected boolean processFilter(PreClassReader preClassReader) {
        return preClassReader.equalSuperClassName(replaceSuperClassConfig.getClassName()) &&
                !isSkipClass(replaceSuperClassConfig.getSkipClass());
    }

    @Override
    protected ClassVisitor createClassVisitor(int asmVersion, ClassWriter classWriter) {
        return new ClassVisitor(asmVersion, classWriter) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                //System.out.println("ReplaceSuperClassRule, createClassVisitor, superName=" + superName);
                if (superName.equals(replaceSuperClassConfig.getClassName())) {
                    String superNameBack = superName;
                    superName = replaceSuperClassConfig.getReplaceClassName();
                    mIsChanged = true;
                    logger.info("ReplaceSuperClassRule, className=" + className + ", superName=" + superNameBack +
                            ", replaceSuperName=" + superName);
                }
                super.visit(version, access, name, signature, superName, interfaces);
            }
        };
    }


}
