package io.github.wufei.config;

public class TryCatchMethodConfig {
    private final String className;
    private final String methodName;
    private final String methodDescriptor;
    private final String throwClassName;
    private final String throwMethodName;
    private final String throwMethodDescriptor;

    public TryCatchMethodConfig(String className, String methodName, String methodDescriptor, String throwClassName, String throwMethodName, String throwMethodDescriptor) {
        this.className = className.replace(".class", "")
                .replace(".java", "")
                .replace(".", "/").trim();
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
        this.throwClassName = throwClassName.replace(".class", "")
                .replace(".java", "")
                .replace(".", "/").trim();
        this.throwMethodName = throwMethodName;
        this.throwMethodDescriptor = throwMethodDescriptor;
    }

    @Override
    public String toString() {

        return "TryCatchMethodConfig{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodDescriptor='" + methodDescriptor + '\'' +
                ",  throwClassName='" + throwClassName + '\'' +
                ",  throwMethodName='" + throwMethodName + '\'' +
                ",  throwMethodDescriptor='" + throwMethodDescriptor + '\'' +
                '}';
    }


    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDescriptor() {
        return methodDescriptor;
    }

    public String getThrowClassName() {
        return throwClassName;
    }

    public String getThrowMethodName() {
        return throwMethodName;
    }

    public String getThrowMethodDescriptor() {
        return throwMethodDescriptor;
    }
}
