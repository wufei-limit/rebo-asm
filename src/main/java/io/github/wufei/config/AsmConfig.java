package io.github.wufei.config;

import java.util.ArrayList;
import java.util.List;

public class AsmConfig {
    private List<ReplaceMethodCodeConfig> replaceMethodCodeConfig = new ArrayList<>();
    private List<ReplaceMethodInvokeConfig> replaceMethodInvokeConfigs = new ArrayList<>();
    private List<String> skipClassName = new ArrayList<>();
    private List<TryCatchMethodConfig> tryCatchMethodConfigs = new ArrayList<>();
    private List<ReplaceClassInvokeConfig> replaceClassInvokeConfigs = new ArrayList<>();
    private List<ReplaceSuperClassConfig> replaceSuperClassConfigs = new ArrayList<>();

    public List<ReplaceClassInvokeConfig> getReplaceClassInvokeConfigs() {
        return replaceClassInvokeConfigs;
    }

    public void setReplaceClassInvokeConfigs(List<ReplaceClassInvokeConfig> replaceClassInvokeConfigs) {
        this.replaceClassInvokeConfigs = replaceClassInvokeConfigs;
    }

    public List<ReplaceSuperClassConfig> getReplaceSuperClassConfigs() {
        return replaceSuperClassConfigs;
    }

    public void setReplaceSuperClassConfigs(List<ReplaceSuperClassConfig> replaceSuperClassConfigs) {
        this.replaceSuperClassConfigs = replaceSuperClassConfigs;
    }

    public AsmConfig() {
    }

    public List<TryCatchMethodConfig> getTryCatchMethodConfigs() {
        return tryCatchMethodConfigs;
    }

    public void setTryCatchMethodConfigs(List<TryCatchMethodConfig> tryCatchMethodConfigs) {
        this.tryCatchMethodConfigs.addAll(tryCatchMethodConfigs);
    }

    public List<ReplaceMethodCodeConfig> getReplaceMethodCodeConfig() {
        return replaceMethodCodeConfig;
    }

    public void setReplaceMethodCodeConfig(List<ReplaceMethodCodeConfig> replaceMethodCodeConfig) {
        this.replaceMethodCodeConfig.addAll(replaceMethodCodeConfig);
    }

    public List<ReplaceMethodInvokeConfig> getReplaceMethodInvokeConfigs() {
        return replaceMethodInvokeConfigs;
    }

    public void setReplaceMethodInvokeConfigs(List<ReplaceMethodInvokeConfig> replaceMethodInvokeConfigs) {
        this.replaceMethodInvokeConfigs.addAll(replaceMethodInvokeConfigs);
    }

    public List<String> getSkipClassName() {
        return skipClassName;
    }

    public void setSkipClassName(List<String> skipClassName) {
        this.skipClassName.addAll(skipClassName);
    }

    public AsmConfig copy(AsmConfig src) {
        this.replaceMethodCodeConfig.addAll(src.getReplaceMethodCodeConfig());
        this.replaceMethodInvokeConfigs.addAll(src.getReplaceMethodInvokeConfigs());
        this.skipClassName.addAll(src.getSkipClassName());
        this.tryCatchMethodConfigs.addAll(src.getTryCatchMethodConfigs());
        this.replaceClassInvokeConfigs.addAll(src.getReplaceClassInvokeConfigs());
        this.replaceSuperClassConfigs.addAll(src.getReplaceSuperClassConfigs());
        return this;
    }
}
