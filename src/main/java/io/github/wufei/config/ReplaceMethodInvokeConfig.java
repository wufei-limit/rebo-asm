package io.github.wufei.config;

import java.util.List;

public class ReplaceMethodInvokeConfig {

    private final String className;
    private final String methodName;
    private final String methodDescriptor;

    private final List<String> skipClass;
    private final List<String> excludeOwner;

    private final String replaceClassName;
    private final String replaceMethodName;
    private final String replaceMethodDescriptor;

    public ReplaceMethodInvokeConfig(String className, String methodName, String methodDescriptor,
                                     List<String> skipClass, List<String> excludeOwner,
                                     String replaceClassName,
                                     String replaceMethodName, String replaceMethodDescriptor) {
        this.className = className.replace(".class", "")
                .replace(".java", "")
                .replace(".", "/").trim();
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
        this.skipClass = skipClass;
        this.excludeOwner = excludeOwner;
        this.replaceClassName = replaceClassName.replace(".class", "")
                .replace(".java", "")
                .replace(".", "/").trim();
        this.replaceMethodName = replaceMethodName;
        this.replaceMethodDescriptor = replaceMethodDescriptor;
    }

    public String getClassName() {
        return className.trim();
    }

    public String getMethodName() {
        return methodName.trim();
    }

    public String getMethodDescriptor() {
        return methodDescriptor.trim();
    }


    public List<String> getSkipClass() {
        return skipClass;
    }

    public List<String> getExcludeOwner() {
        return excludeOwner;
    }

    public String getReplaceClassName() {
        return replaceClassName.trim();
    }

    public String getReplaceMethodName() {
        return replaceMethodName.trim();
    }

    public String getReplaceMethodDescriptor() {
        return replaceMethodDescriptor.trim();
    }

    @Override
    public String toString() {
        return "ReplaceMethodInvokeConfig{" +
                "className=" + className +
                ", methodName='" + methodName + '\'' +
                ", methodDescriptor='" + methodDescriptor + '\'' +
                ", replaceClassName='" + replaceClassName + '\'' +
                ", replaceMethodName='" + replaceMethodName + '\'' +
                ", replaceMethodDescriptor='" + replaceMethodDescriptor + '\'' +
                ", skipClass=" + skipClass + '\'' +
                ", excludeOwner=" + excludeOwner + '\'' +
                '}';
    }
}
