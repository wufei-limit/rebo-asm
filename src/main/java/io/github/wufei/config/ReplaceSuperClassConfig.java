package io.github.wufei.config;

import java.util.List;

public class ReplaceSuperClassConfig {
    private final String className;
    private final String replaceClassName;
    private final List<String> skipClass;

    public ReplaceSuperClassConfig(String className, String replaceClassName, List<String> skipClass) {
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

    @Override
    public String toString() {
        return "ReplaceSuperClassConfig{" +
                "className='" + className + '\'' +
                ", replaceClassName='" + replaceClassName + '\'' +
                ", skipClass=" + skipClass +
                '}';
    }
}
