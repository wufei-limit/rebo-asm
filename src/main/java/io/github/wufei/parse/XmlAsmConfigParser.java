package io.github.wufei.parse;


import org.gradle.api.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.github.wufei.config.AsmConfig;
import io.github.wufei.config.ReplaceClassInvokeConfig;
import io.github.wufei.config.ReplaceMethodCodeConfig;
import io.github.wufei.config.ReplaceMethodInvokeConfig;
import io.github.wufei.config.ReplaceSuperClassConfig;
import io.github.wufei.config.TryCatchMethodConfig;

public class XmlAsmConfigParser implements AsmConfigParser {
    private final Logger logger;

    public XmlAsmConfigParser(Logger logger) {
        this.logger = logger;
    }

    @Override
    @NotNull
    public AsmConfig parseAsmConfig(File asmConfigFile) throws IOException {
        AsmConfig result = new AsmConfig();
        if (asmConfigFile.exists() && asmConfigFile.isFile()) {
            try {
                result.setReplaceMethodCodeConfig(parseReplaceMethodCode(asmConfigFile));
                result.setReplaceMethodInvokeConfigs(parseReplaceMethodInvoke(asmConfigFile));
                result.setSkipClassName(parseSkipClassName(asmConfigFile));
                result.setTryCatchMethodConfigs(parseTryCatchMethod(asmConfigFile));

                result.setReplaceClassInvokeConfigs(parseReplaceClassInvoke(asmConfigFile));
                result.setReplaceSuperClassConfigs(parseReplaceSuperClass(asmConfigFile));
            } catch (IOException | JDOMException e) {
                throw new IOException("RoboAsmConfig.xml parse error", e);
            }
        } else {
            logger.info("RoboAsmConfig.xml no exists, path=" + asmConfigFile.getAbsolutePath());
        }
        return result;
    }

    private List<ReplaceSuperClassConfig> parseReplaceSuperClass(File file) throws IOException, JDOMException {
        List<ReplaceSuperClassConfig> replaceSuperClassConfigs = new ArrayList<>();
        SAXBuilder builder = new SAXBuilder();
        InputStream inputStream = removeBOM(new FileInputStream(file));
        Document document = builder.build(inputStream, "UTF-8");
        Element root = document.getRootElement();

        for (Iterator<Element> it = root.getChildren("ReplaceSuperClass").listIterator(); it.hasNext(); ) {
            Element hook = it.next();

            String className = hook.getChildText("superClassName");
            String replaceClassName = hook.getChildText("replaceSuperClassName");

            List<String> skipClasses = new ArrayList<>();
            skipClasses.add(replaceClassName);

            String skipClass = hook.getChildText("skipClassName");
            if (skipClass != null && !"".equals(skipClass)) {
                if (skipClass.contains("#")) {
                    skipClasses = Arrays.asList(skipClass.trim().split("#"));
                } else {
                    skipClasses.add(skipClass);
                }
            }

            ReplaceSuperClassConfig r = new ReplaceSuperClassConfig(className, replaceClassName, skipClasses);
            replaceSuperClassConfigs.add(r);
        }
        return replaceSuperClassConfigs;
    }

    private List<ReplaceClassInvokeConfig> parseReplaceClassInvoke(File file) throws IOException, JDOMException {
        List<ReplaceClassInvokeConfig> replaceClassInvokeConfigs = new ArrayList<>();
        SAXBuilder builder = new SAXBuilder();
        InputStream inputStream = removeBOM(new FileInputStream(file));
        Document document = builder.build(inputStream, "UTF-8");
        Element root = document.getRootElement();

        for (Iterator<Element> it = root.getChildren("ReplaceClassInvoke").listIterator(); it.hasNext(); ) {
            Element hook = it.next();

            String className = hook.getChildText("className");
            String replaceClassName = hook.getChildText("replaceClassName");

            List<String> skipClasses = new ArrayList<>();
            skipClasses.add(className);
            skipClasses.add(replaceClassName);

            String skipClass = hook.getChildText("skipClassName");
            if (skipClass != null && !"".equals(skipClass)) {
                if (skipClass.contains("#")) {
                    skipClasses = Arrays.asList(skipClass.trim().split("#"));
                } else {
                    skipClasses.add(skipClass);
                }
            }

            ReplaceClassInvokeConfig r = new ReplaceClassInvokeConfig(className, replaceClassName, skipClasses);
            replaceClassInvokeConfigs.add(r);
        }
        return replaceClassInvokeConfigs;
    }

    private List<TryCatchMethodConfig> parseTryCatchMethod(File file) throws IOException, JDOMException {
        List<TryCatchMethodConfig> tryCatchMethodConfigs = new ArrayList<>();
        SAXBuilder builder = new SAXBuilder();
        InputStream inputStream = removeBOM(new FileInputStream(file));
        Document document = builder.build(inputStream, "UTF-8");
        Element root = document.getRootElement();

        for (Iterator<Element> it = root.getChildren("TryCatchMethod").listIterator(); it.hasNext(); ) {
            Element hook = it.next();
            String className = hook.getChildText("className");
            String methodName = hook.getChildText("methodName");
            String methodDescriptor = hook.getChildText("methodDescriptor");

            String throwClassName = hook.getChildText("throwClassName");
            String throwMethodName = hook.getChildText("throwMethodName");
            if (throwMethodName == null || throwMethodName.equals("null")) {
                throwMethodName = methodName;
            }
            String throwMethodDescriptor = hook.getChildText("throwMethodDescriptor");
            if (throwMethodDescriptor == null || throwMethodDescriptor.equals("null")) {
                throwMethodDescriptor = methodDescriptor;
            }

            TryCatchMethodConfig tryCatchMethodConfig = new TryCatchMethodConfig(
                    className, methodName, methodDescriptor,
                    throwClassName, throwMethodName,
                    throwMethodDescriptor);
            tryCatchMethodConfigs.add(tryCatchMethodConfig);
        }

        return tryCatchMethodConfigs;
    }


    public List<String> parseSkipClassName(File asmConfigFile) throws IOException, JDOMException {
        List<String> result = new ArrayList<>();

        SAXBuilder builder = new SAXBuilder();
        InputStream inputStream = removeBOM(new FileInputStream(asmConfigFile));
        Document document = builder.build(inputStream, "UTF-8");
        Element root = document.getRootElement();

        for (Iterator<Element> it = root.getChildren("SkipClass").listIterator(); it.hasNext(); ) {
            Element hook = it.next();
            String className = hook.getText().replace(".class", "")
                    .replace(".java", "")
                    .replace(".", "/").trim();
            result.add(className);
        }

        return result;
    }


    private List<ReplaceMethodInvokeConfig> parseReplaceMethodInvoke(File file) throws IOException, JDOMException {
        List<ReplaceMethodInvokeConfig> replaceMethodInvokeConfigs = new ArrayList<>();
        SAXBuilder builder = new SAXBuilder();
        InputStream inputStream = removeBOM(new FileInputStream(file));
        Document document = builder.build(inputStream, "UTF-8");
        Element root = document.getRootElement();

        for (Iterator<Element> it = root.getChildren("ReplaceMethodInvoke").listIterator(); it.hasNext(); ) {
            Element hook = it.next();

            String className = hook.getChildText("className");
            if (className == null || className.equals("null")) {
                className = "";
            }
            String methodName = hook.getChildText("methodName");
            String methodDescriptor = hook.getChildText("methodDescriptor");


            String replaceClassName = hook.getChildText("replaceClassName");

            String replaceMethodName = hook.getChildText("replaceMethodName");
            if (replaceMethodName == null || replaceMethodName.equals("null")) {
                replaceMethodName = methodName;
            }
            String replaceMethodDescriptor = hook.getChildText("replaceMethodDescriptor");
            if (replaceMethodDescriptor == null) {
                replaceMethodDescriptor = methodDescriptor;
            }
            List<String> skipClasses = new ArrayList<>();
            skipClasses.add(replaceClassName);

            String skipClass = hook.getChildText("skipClassName");
            if (skipClass != null && !"".equals(skipClass)) {
                if (skipClass.contains("#")) {
                    skipClasses = Arrays.asList(skipClass.trim().split("#"));
                } else {
                    skipClasses.add(skipClass);
                }
            }

            List<String> excludeOwners = new ArrayList<>();
            if (className.isEmpty()) {
                String excludeOwner = hook.getChildText("excludeClassName");
                if (excludeOwner != null && !"".equals(excludeOwner)) {
                    if (excludeOwner.contains("#")) {
                        excludeOwners = Arrays.asList(excludeOwner.trim().split("#"));
                    } else {
                        excludeOwners.add(excludeOwner);
                    }
                }
            }

            ReplaceMethodInvokeConfig r = new ReplaceMethodInvokeConfig(className, methodName, methodDescriptor,
                    skipClasses, excludeOwners, replaceClassName, replaceMethodName, replaceMethodDescriptor);
            replaceMethodInvokeConfigs.add(r);
        }
        return replaceMethodInvokeConfigs;
    }


    private List<ReplaceMethodCodeConfig> parseReplaceMethodCode(File file) throws IOException, JDOMException {
        List<ReplaceMethodCodeConfig> replaceMethodCodeConfigs = new ArrayList<>();
        SAXBuilder builder = new SAXBuilder();
        InputStream inputStream = removeBOM(new FileInputStream(file));
        Document document = builder.build(inputStream, "UTF-8");
        Element root = document.getRootElement();

        for (Iterator<Element> it = root.getChildren("ReplaceMethodCode").listIterator(); it.hasNext(); ) {
            Element hook = it.next();
            String className = hook.getChildText("className");
            String methodName = hook.getChildText("methodName");
            String methodDescriptor = hook.getChildText("methodDescriptor");

            String isInterrupt = hook.getChildText("interrupt");
            boolean interrupt = isInterrupt == null || isInterrupt.equals("null") || isInterrupt.equals("true");
            String replaceClassName = hook.getChildText("replaceClassName");

            String replaceMethodMethodName = hook.getChildText("replaceMethodName");
            if (replaceMethodMethodName == null || replaceMethodMethodName.equals("null")) {
                replaceMethodMethodName = methodName;
            }

            String replaceMethodDescriptor = hook.getChildText("replaceMethodDescriptor");
            if (replaceMethodDescriptor == null || replaceMethodDescriptor.equals("null")) {
                replaceMethodDescriptor = methodDescriptor;
            }

            ReplaceMethodCodeConfig replaceMethodCodeConfig = new ReplaceMethodCodeConfig(
                    className, methodName, methodDescriptor, interrupt,

                    replaceClassName, replaceMethodMethodName, replaceMethodDescriptor);
            replaceMethodCodeConfigs.add(replaceMethodCodeConfig);
        }

        return replaceMethodCodeConfigs;
    }


    private InputStream removeBOM(InputStream in) throws IOException {
        PushbackInputStream testin = new PushbackInputStream(in);
        int ch = testin.read();
        if (ch != 0xEF) {
            testin.unread(ch);
        } else if ((ch = testin.read()) != 0xBB) {
            testin.unread(ch);
            testin.unread(0xef);
        } else if ((ch = testin.read()) != 0xBF) {
            throw new IOException("错误的UTF-8格式文件");
        }
        return testin;
    }
}
