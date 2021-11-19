package io.github.wufei.base;

import java.io.*;
import java.util.HashSet;

/**
 * 预先读取class二进制文件, header数据部分信息
 */
public class PreClassReader {
    private byte[] srcData;
    private int header;
    private int maxStringLength;
    private int classFileOffset = 0;
    private String[] constantUtf8Values;
    private int[] cpInfoOffsets;
    private HashSet<String> stringSet = new HashSet();

    private String mClassName = "";


    /**
     * 获取当前类的名称
     */
    public String getClassName() {
        if (!mClassName.isEmpty()) {
            return mClassName;
        } else {
            mClassName = readClass(header + 2, new char[maxStringLength]);
        }
        return mClassName;
    }

    /**
     * 判读当前类名是否一致.
     */
    public boolean equalClassName(String targetClassName) {
        String dest = targetClassName.replace(".class", "")
                .replace(".java", "")
                .replace(".", "/");
        return dest.equals(getClassName());
    }

    /**
     * 获取父类名称
     */
    public String getSuperClassName() {
        return readClass(header + 4, new char[maxStringLength]);
    }

    /**
     * 判读当前父类名是否一致.
     */
    public boolean equalSuperClassName(String targetSuperClassName) {
        String dest = targetSuperClassName.replace(".class", "")
                .replace(".java", "")
                .replace(".", "/");
        return dest.equals(getSuperClassName());
    }

    /**
     * 是否含有指定的导包
     */
    public boolean headerStringContain(String imports) {
        String destImports = imports.replace(".class", "")
                .replace(".java", "")
                .replace(".", "/");
        return stringSet.contains(destImports);
    }

    /**
     * 获取所有的字符串,包含类名,方法名,引用类型,等等.
     */
    public HashSet<String> getStringSet() {
        return stringSet;
    }

    /**
     * 获取实现的接口名称
     */
    public String[] getInterfacesName() {
        // interfaces_count is after the access_flags, this_class and super_class fields (2 bytes each).
        int currentOffset = header + 6;
        int interfacesCount = readUnsignedShort(currentOffset);
        String[] interfaces = new String[interfacesCount];
        if (interfacesCount > 0) {
            char[] charBuffer = new char[maxStringLength];
            for (int i = 0; i < interfacesCount; ++i) {
                currentOffset += 2;
                interfaces[i] = readClass(currentOffset, charBuffer);
            }
        }
        return interfaces;
    }

    /**
     * 是否含有指定的借口
     */
    public boolean containInterfaces(String interfaces) {
        String destImports = interfaces.replace(".class", "")
                .replace(".java", "")
                .replace(".", "/");
        String[] interfaceNames = getInterfacesName();
        for (String src : interfaceNames) {
            if (destImports.equals(src)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件的byte[]
     */
    public byte[] getSrcByteData() {
        return srcData;
    }
    //========================================================================

    public PreClassReader(File srcClassFile) {
        try {
            byte[] data = readFile(new FileInputStream(srcClassFile));
            this.srcData = data;
            readHeader();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public PreClassReader(byte[] srcData) {
        this.srcData = srcData;
        readHeader();
    }


    public void readHeader() {
        int constantPoolCount = readUnsignedShort(classFileOffset + 8);
        cpInfoOffsets = new int[constantPoolCount];
        constantUtf8Values = new String[constantPoolCount];
        int currentCpInfoIndex = 1;
        int currentCpInfoOffset = classFileOffset + 10;
        int currentMaxStringLength = 0;
        boolean hasConstantDynamic = false;
        boolean hasConstantInvokeDynamic = false;
        while (currentCpInfoIndex < constantPoolCount) {
            cpInfoOffsets[currentCpInfoIndex++] = currentCpInfoOffset + 1;
            int cpInfoSize;
            switch (srcData[currentCpInfoOffset]) {
                case Symbol.CONSTANT_FIELDREF_TAG:
                case Symbol.CONSTANT_METHODREF_TAG:
                case Symbol.CONSTANT_INTERFACE_METHODREF_TAG:
                case Symbol.CONSTANT_INTEGER_TAG:
                case Symbol.CONSTANT_FLOAT_TAG:
                case Symbol.CONSTANT_NAME_AND_TYPE_TAG:
                    cpInfoSize = 5;
                    break;
                case Symbol.CONSTANT_DYNAMIC_TAG:
                    cpInfoSize = 5;
                    hasConstantDynamic = true;
                    break;
                case Symbol.CONSTANT_INVOKE_DYNAMIC_TAG:
                    cpInfoSize = 5;
                    hasConstantInvokeDynamic = true;
                    break;
                case Symbol.CONSTANT_LONG_TAG:
                case Symbol.CONSTANT_DOUBLE_TAG:
                    cpInfoSize = 9;
                    currentCpInfoIndex++;
                    break;
                case Symbol.CONSTANT_UTF8_TAG:
                    int srcLen = readUnsignedShort(currentCpInfoOffset + 1);
                    cpInfoSize = 3 + srcLen;
                    if (cpInfoSize > currentMaxStringLength) {
                        // The size in bytes of this CONSTANT_Utf8 structure provides a conservative estimate
                        // of the length in characters of the corresponding string, and is much cheaper to
                        // compute than this exact length.
                        currentMaxStringLength = cpInfoSize;
                    }
                    String src = readUtf(currentCpInfoOffset + 3, srcLen, new char[currentMaxStringLength]);
                    //System.out.println("src" + src);
                    stringSet.add(src);
                    break;
                case Symbol.CONSTANT_METHOD_HANDLE_TAG:
                    cpInfoSize = 4;
                    break;
                case Symbol.CONSTANT_CLASS_TAG:
                case Symbol.CONSTANT_STRING_TAG:
                case Symbol.CONSTANT_METHOD_TYPE_TAG:
                case Symbol.CONSTANT_PACKAGE_TAG:
                case Symbol.CONSTANT_MODULE_TAG:
                    cpInfoSize = 3;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            currentCpInfoOffset += cpInfoSize;
        }
        maxStringLength = currentMaxStringLength;
        // The Classfile's access_flags field is just after the last constant pool entry.
        header = currentCpInfoOffset;

        // Allocate the cache of ConstantDynamic values, if there is at least one.
        // constantDynamicValues = hasConstantDynamic ? new ConstantDynamic[constantPoolCount] : null;

        // Read the BootstrapMethods attribute, if any (only get the offset of each method).
        //bootstrapMethodOffsets =
        // (hasConstantDynamic | hasConstantInvokeDynamic)
        //? readBootstrapMethodsAttribute(currentMaxStringLength)
        //: null;
    }

    private String readClass(final int offset, final char[] charBuffer) {
        return readStringish(offset, charBuffer);
    }

    private String readStringish(final int offset, final char[] charBuffer) {
        // Get the start offset of the cp_info structure (plus one), and read the CONSTANT_Utf8 entry
        // designated by the first two bytes of this cp_info.
        return readUTF8(cpInfoOffsets[readUnsignedShort(offset)], charBuffer);
    }

    private String readUTF8(final int offset, final char[] charBuffer) {
        int constantPoolEntryIndex = readUnsignedShort(offset);
        if (offset == 0 || constantPoolEntryIndex == 0) {
            return null;
        }
        return readUtf(constantPoolEntryIndex, charBuffer);
    }

    private int readUnsignedShort(final int offset) {
        byte[] classFileBuffer = srcData;
        return ((classFileBuffer[offset] & 0xFF) << 8) | (classFileBuffer[offset + 1] & 0xFF);
    }

    private String readUtf(final int constantPoolEntryIndex, final char[] charBuffer) {
        String value = constantUtf8Values[constantPoolEntryIndex];
        if (value != null) {
            return value;
        }
        int cpInfoOffset = cpInfoOffsets[constantPoolEntryIndex];
        return constantUtf8Values[constantPoolEntryIndex] =
                readUtf(cpInfoOffset + 2, readUnsignedShort(cpInfoOffset), charBuffer);
    }

    private String readUtf(final int utfOffset, final int utfLength, final char[] charBuffer) {
        int currentOffset = utfOffset;
        int endOffset = currentOffset + utfLength;
        int strLength = 0;
        byte[] classFileBuffer = srcData;
        while (currentOffset < endOffset) {
            int currentByte = classFileBuffer[currentOffset++];
            if ((currentByte & 0x80) == 0) {
                charBuffer[strLength++] = (char) (currentByte & 0x7F);
            } else if ((currentByte & 0xE0) == 0xC0) {
                charBuffer[strLength++] =
                        (char) (((currentByte & 0x1F) << 6) + (classFileBuffer[currentOffset++] & 0x3F));
            } else {
                charBuffer[strLength++] =
                        (char)
                                (((currentByte & 0xF) << 12)
                                        + ((classFileBuffer[currentOffset++] & 0x3F) << 6)
                                        + (classFileBuffer[currentOffset++] & 0x3F));
            }
        }
        return new String(charBuffer, 0, strLength);
    }

    public static byte[] readFile(InputStream inputStream) {
        byte[] data = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 4];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);

            }
            outputStream.flush();
            data = outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private static class Symbol {
        /**
         * The tag value of CONSTANT_Class_info JVMS structures.
         */
        static final int CONSTANT_CLASS_TAG = 7;

        /**
         * The tag value of CONSTANT_Fieldref_info JVMS structures.
         */
        static final int CONSTANT_FIELDREF_TAG = 9;

        /**
         * The tag value of CONSTANT_Methodref_info JVMS structures.
         */
        static final int CONSTANT_METHODREF_TAG = 10;

        /**
         * The tag value of CONSTANT_InterfaceMethodref_info JVMS structures.
         */
        static final int CONSTANT_INTERFACE_METHODREF_TAG = 11;

        /**
         * The tag value of CONSTANT_String_info JVMS structures.
         */
        static final int CONSTANT_STRING_TAG = 8;

        /**
         * The tag value of CONSTANT_Integer_info JVMS structures.
         */
        static final int CONSTANT_INTEGER_TAG = 3;

        /**
         * The tag value of CONSTANT_Float_info JVMS structures.
         */
        static final int CONSTANT_FLOAT_TAG = 4;

        /**
         * The tag value of CONSTANT_Long_info JVMS structures.
         */
        static final int CONSTANT_LONG_TAG = 5;

        /**
         * The tag value of CONSTANT_Double_info JVMS structures.
         */
        static final int CONSTANT_DOUBLE_TAG = 6;

        /**
         * The tag value of CONSTANT_NameAndType_info JVMS structures.
         */
        static final int CONSTANT_NAME_AND_TYPE_TAG = 12;

        /**
         * The tag value of CONSTANT_Utf8_info JVMS structures.
         */
        static final int CONSTANT_UTF8_TAG = 1;

        /**
         * The tag value of CONSTANT_MethodHandle_info JVMS structures.
         */
        static final int CONSTANT_METHOD_HANDLE_TAG = 15;

        /**
         * The tag value of CONSTANT_MethodType_info JVMS structures.
         */
        static final int CONSTANT_METHOD_TYPE_TAG = 16;

        /**
         * The tag value of CONSTANT_Dynamic_info JVMS structures.
         */
        static final int CONSTANT_DYNAMIC_TAG = 17;

        /**
         * The tag value of CONSTANT_InvokeDynamic_info JVMS structures.
         */
        static final int CONSTANT_INVOKE_DYNAMIC_TAG = 18;

        /**
         * The tag value of CONSTANT_Module_info JVMS structures.
         */
        static final int CONSTANT_MODULE_TAG = 19;

        /**
         * The tag value of CONSTANT_Package_info JVMS structures.
         */
        static final int CONSTANT_PACKAGE_TAG = 20;

        // Tag values for the BootstrapMethods attribute entries (ASM specific tag).

        /**
         * The tag value of the BootstrapMethods attribute entries.
         */
        static final int BOOTSTRAP_METHOD_TAG = 64;

        // Tag values for the type table entries (ASM specific tags).

        /**
         * The tag value of a normal type entry in the (ASM specific) type table of a class.
         */
        static final int TYPE_TAG = 128;

        /**
         * The tag value of an {Frame#ITEM_UNINITIALIZED} type entry in the type table of a class.
         */
        static final int UNINITIALIZED_TYPE_TAG = 129;

        /**
         * The tag value of a merged type entry in the (ASM specific) type table of a class.
         */
        static final int MERGED_TYPE_TAG = 130;
    }

//    public static void main(String[] args) {
//        String classFilePath = "C:\\Users\\qinguanghui\\Desktop\\java编译\\com\\base\\User.class";
//        PreClassReader preClassReader = new PreClassReader(new File(classFilePath));
//        preClassReader.readHeader();
//        String classname = preClassReader.getClassName();
//        String superclassname = preClassReader.getSuperClassName();
//        String[] interfaces = preClassReader.getInterfacesName();
//        HashSet<String> strings = preClassReader.getStringSet();
//        boolean containImport = preClassReader.containImport("java.util.ArrayList");
//        System.out.println("classname=" + classname + ",superclassname=" + superclassname + ",interfaces=" + Arrays.toString(interfaces));
//        System.out.println("containImport=" + containImport);
//        System.out.println("strings=" + Arrays.toString(strings.toArray()));
//
//    }
}
