package io.github.wufei;

import com.android.annotations.NonNull;
import com.android.build.api.transform.*;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.google.common.io.Files;
import io.github.wufei.base.ITransformRule;
import io.github.wufei.base.PreClassReader;
import io.github.wufei.config.*;
import io.github.wufei.parse.AsmConfigParser;
import io.github.wufei.parse.XmlAsmConfigParser;
import io.github.wufei.rule.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;

public class RoboAsmTransform extends Transform {
    private final Logger logger;
    private AsmConfig asmConfig;
    private final Project project;
    private final String configFileNameGen = "RoboAsmConfigGen.xml";
    private final String configFileName = "RoboAsmConfig.xml";
    private final String cacheConfigFileName = "AllRoboAsmConfig.txt";
    private final ArrayList<ITransformRule> transformRules = new ArrayList<>();

    public RoboAsmTransform(Logger logger, Project project) {
        this.logger = logger;
        this.project = project;
    }

    @Override
    public String getName() {
        return "RoboAsmTransform";
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        List<QualifiedContent.Scope> scopes = Arrays.asList(QualifiedContent.Scope.PROJECT,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES);
        return new HashSet<>(scopes);
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS; //all classes
    }

    public void register() {
        asmConfig = parseRoboAsmConfig();
        for (ReplaceMethodCodeConfig codeConfig : asmConfig.getReplaceMethodCodeConfig()) {
            transformRules.add(new ReplaceMethodCodeRule(logger, codeConfig));
        }
        for (ReplaceMethodInvokeConfig invokeConfig : asmConfig.getReplaceMethodInvokeConfigs()) {
            transformRules.add(new ReplaceMethodInvokeRule(logger, invokeConfig));
        }
        for (TryCatchMethodConfig invokeConfig : asmConfig.getTryCatchMethodConfigs()) {
            transformRules.add(new TryCatchMethodRule(logger, invokeConfig));
        }
        for (ReplaceSuperClassConfig superClassConfig : asmConfig.getReplaceSuperClassConfigs()) {
            transformRules.add(new ReplaceSuperClassRule(logger, superClassConfig));
        }
        for (ReplaceClassInvokeConfig classInvokeConfig : asmConfig.getReplaceClassInvokeConfigs()) {
            transformRules.add(new ReplaceClassInvokeRule(logger, classInvokeConfig));
        }
    }

    private AsmConfig parseRoboAsmConfig() {
        final AsmConfigParser parser = new XmlAsmConfigParser(logger);
        final File generatedAsmConfigPath = project.file(configFileNameGen);
        final File asmConfigPath = project.file(configFileName);
        AsmConfig asmConfig = null;
        try {
            asmConfig = parser.parseAsmConfig(asmConfigPath);
            asmConfig.copy(parser.parseAsmConfig(generatedAsmConfigPath));
        } catch (IOException e) {
            logger.error("parse RoboAsmConfig.xml is error");
        }
        return asmConfig;
    }

    public void transform(InputStream inputStream, OutputStream outputStream, String classPath) throws IOException {
        byte src[] = PreClassReader.readFile(inputStream);
        try {
            PreClassReader preClassReader = new PreClassReader(src);
            for (ITransformRule task : transformRules) {
                if (task.filterClass(preClassReader)) {
                    byte[] processedSrc = task.processClassFile(preClassReader.getSrcByteData());
                    if (task.isChanged()) {
                        preClassReader = new PreClassReader(processedSrc);
                    }
                }
            }
            outputStream.write(preClassReader.getSrcByteData());
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.warn("RoboAsm handle transform e=" + e + ", classPath=" + classPath);
            outputStream.write(src);
        }
    }

    @Override
    public Set<? super QualifiedContent.Scope> getReferencedScopes() {

        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @NonNull
    @SuppressWarnings("UnstableApiUsage")
    public static Stream<File> getAllFiles(@NonNull File dir) {
        Iterable<File> files = Files.fileTraverser().depthFirstPreOrder(dir);
        return StreamSupport.stream(files.spliterator(), false).filter(Files.isFile());
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public void transform(TransformInvocation invocation) throws TransformException, InterruptedException, IOException {
        boolean incremental = invocation.isIncremental();
        System.out.println("RoboAsmTransform start, isIncremental=" + incremental);
        long startTime = System.currentTimeMillis();
        final TransformOutputProvider outputProvider = invocation.getOutputProvider();
        register();
        if (incremental && isNoChangeConfigFile(asmConfig)) {
            System.out.println("RoboAsmConfig Is No Change, Skip");
            return;
        }

        if (outputProvider != null) {
            outputProvider.deleteAll();
        }

        for (TransformInput ti : invocation.getInputs()) {

            for (JarInput jarInput : ti.getJarInputs()) {
                transformJar(jarInput, outputProvider);
            }

            for (DirectoryInput di : ti.getDirectoryInputs()) {
                transformFile(di, outputProvider);
            }
        }

        long cost = (System.currentTimeMillis() - startTime) / 1000;
        writeCache(asmConfig);
        System.out.println("RoboAsmTransform cost=" + cost + "s");
    }

    private void writeCache(AsmConfig asmConfig) {
        File cacheFile = new File(project.getBuildDir(), "generated/source/developer/" + cacheConfigFileName);
        try {
            FileUtils.write(cacheFile, asmConfig.toString(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("writeCache, e=" + e.toString());
        }
    }

    private boolean isNoChangeConfigFile(AsmConfig asmConfig) {
        File cacheFile = getCacheFile();
        if (cacheFile.exists()) {
            try {
                String cache = FileUtils.readFileToString(cacheFile, "UTF-8");
                if (cache.equals(asmConfig.toString())) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private File getCacheFile() {
        return new File(project.getBuildDir(), "generated/source/developer/" + cacheConfigFileName);
    }

    private void transformFile(DirectoryInput directoryInput, TransformOutputProvider outputProvider)
            throws IOException {
        File outputDir =
                outputProvider.getContentLocation(
                        directoryInput.getName(),
                        directoryInput.getContentTypes(),
                        directoryInput.getScopes(),
                        Format.DIRECTORY);
        if (directoryInput.getFile().isDirectory()) {
            List<File> allFiles = new ArrayList<>();
            listAllFiles(directoryInput.getFile(), allFiles);
            allFiles.forEach(file -> {
                if (checkClassFile(file.getPath())) {
                    File tmpFile = new File(file.getParent() + File.separator + "class_temp");
                    if (tmpFile.exists()) {
                        tmpFile.delete();
                    }
                    try {
                        tmpFile.createNewFile();
                        FileOutputStream out = new FileOutputStream(tmpFile);
                        FileInputStream in = new FileInputStream(file);
                        transform(in, out, file.getPath());
                        in.close();
                        out.close();
                        FileUtils.copyFile(tmpFile, file);
                        tmpFile.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        FileUtils.copyDirectory(directoryInput.getFile(), outputDir);
    }

    private void transformJar(JarInput jarInput, TransformOutputProvider outputProvider)
            throws IOException {
        if (jarInput.getFile().getAbsolutePath().endsWith(".jar")) {

            String jarName = jarInput.getName();
            String md5Name = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4);
            }
            JarFile jarFile = new JarFile(jarInput.getFile());
            Enumeration<JarEntry> enumeration = jarFile.entries();
            File tmpFile = new File(jarInput.getFile().getParent() + File.separator + "classes_temp.jar");

            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile));

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement();
                String entryName = jarEntry.getName();
                ZipEntry zipEntry = new ZipEntry(entryName);

                InputStream inputStream = jarFile.getInputStream(jarEntry);

                if (checkClassFile(entryName)) {
                    jarOutputStream.putNextEntry(zipEntry);
                    transform(inputStream, jarOutputStream, entryName);
                } else {
                    jarOutputStream.putNextEntry(zipEntry);
                    jarOutputStream.write(IOUtils.toByteArray(inputStream));
                }
                jarOutputStream.closeEntry();
            }

            jarOutputStream.close();
            jarFile.close();
            File dest = outputProvider.getContentLocation(jarName + md5Name,
                    jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
            FileUtils.copyFile(tmpFile, dest);
            tmpFile.delete();
        }
    }

    boolean checkClassFile(String name) {

        boolean result = (name.endsWith(".class")
                && !name.contains("R/$")
                && !name.contains("androidx") //只处理需要的class文件,androidx不可以通过asm修复(androidx会校验签名,强行修复会抛出 Exception: Failure to verify dex file).
                && !name.contains("android/support/v4")
                && !name.contains("android/support/v7")
                && !name.contains("kotlinx/")
                && !name.contains("kotlin/")
                && !name.contains("META-INF/")
                && !name.contains("/R$")
                && !name.contains("/R.class")
                && !"BuildConfig.class".equals(name))
                && !skipClassName(name);
        return result;
    }

    boolean skipClassName(String name) {
        for (String s : asmConfig.getSkipClassName()) {
            if (name.contains(s)) {
                return true;
            }
        }
        return false;
    }

    private static void listAllFiles(File fileDir, List<File> fileList) {
        File[] files = fileDir.listFiles(); //获取目录下的所有文件或文件夹
        if (files == null) { //如果目录为空，直接退出
            return;
        }
        //遍历，目录下的所有文件
        for (File f : files) {
            if (f.isFile()) {
                fileList.add(f);
            } else if (f.isDirectory()) {
                listAllFiles(f, fileList);
            }
        }
    }
}
