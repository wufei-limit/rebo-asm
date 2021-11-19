package io.github.wufei.config;

import java.util.List;

public class ReplaceClassInvokeConfig {
    private final String className;
    private final String replaceClassName;
    private final List<String> skipClass;

    public ReplaceClassInvokeConfig(String className, String replaceClassName, List<String> skipClass) {
        this.className = className.replace(".class", "")
                .replace(".java", "")
                .replace(".", "/").trim();
        this.replaceClassName = replaceClassName.replace(".class", "")
                .replace(".java", "")
                .replace(".", "/").trim();
        this.skipClass = skipClass;
    }

    public String getClassName() {
        return className;
    }

    public String getReplaceClassName() {
        return replaceClassName;
    }

    public List<String> getSkipClass() {
        return skipClass;
    }

    public String getClassDesc() {
        return "L" + getClassName() + ";";
    }

    public String getReplaceClassDesc() {
        return "L" + getReplaceClassName() + ";";
    }

    @Override
    public String toString() {
        return "ReplaceClassInvokeConfig{" +
                "className='" + className + '\'' +
                ", replaceClassName='" + replaceClassName + '\'' +
                ", skipClass=" + skipClass +
                '}';
    }
}
