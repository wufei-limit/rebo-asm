package io.github.wufei.config;

public class ReplaceMethodCodeConfig {
    private final String className;
    private final String methodName;
    private final String methodDescriptor;
    private final boolean isInterrupt;
    private final String replaceClassName;
    private final String replaceMethodName;
    private final String replaceMethodDescriptor;


    public ReplaceMethodCodeConfig(String className, String methodName, String methodDescriptor, boolean isInterrupt,
                                   String replaceClassName, String replaceMethodName, String replaceMethodDescriptor) {
        this.className = className.replace(".class", "")
                .replace(".java", "")
                .replace(".", "/").trim();
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
        this.isInterrupt = isInterrupt;
        this.replaceClassName = replaceClassName.replace(".class", "")
                .replace(".java", "")
                .replace(".", "/").trim();
        this.replaceMethodName = replaceMethodName;
        this.replaceMethodDescriptor = replaceMethodDescriptor;
    }

    @Override
    public String toString() {
        return "ReplaceMethodCodeConfig{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodDescriptor='" + methodDescriptor + '\'' +
                ", isInterrupt=" + isInterrupt +
                ", replaceClassName='" + replaceClassName + '\'' +
                ", replaceMethodName='" + replaceMethodName + '\'' +
                ", replaceMethodDescriptor='" + replaceMethodDescriptor + '\'' +
                '}';
    }

    public boolean isInterrupt() {
        return isInterrupt;
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

    public String getReplaceClassName() {
        return replaceClassName.trim();
    }

    public String getReplaceMethodName() {
        return replaceMethodName.trim();
    }

    public String getReplaceMethodDescriptor() {
        return replaceMethodDescriptor.trim();
    }

    public boolean returnTypeIsVoid() {
        return methodDescriptor.endsWith("V");
    }
}
