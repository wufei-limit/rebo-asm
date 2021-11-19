package io.github.wufei.base;

public interface ITransformRule {
    /**
     * 如果{@filterClass()}返回为true,
     * 将会进行ClassReader-ClassVisitor-ClassWriter处理原始的class文件二进制数据
     *
     * @param srcData 原始的class文件二进制数据
     * @return 处理后的class文件的二进制数据
     */
    byte[] processClassFile(byte[] srcData);

    /**
     * 通过PreClassReader 进行预读,过滤掉不处理的类.
     * 比如根据类名,导包,父类,是否实现接口进行过滤.
     *
     * @return 如果需要处理 返回为true, 如果不处理返回为false.
     */
    boolean filterClass(PreClassReader preClassReader);

    boolean isChanged();

}
