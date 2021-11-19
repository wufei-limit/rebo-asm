package io.github.wufei.parse;

import org.objectweb.asm.Opcodes;

import java.util.ArrayList;

public class MethodDescriptionParser {

    public static int[] getParmaOp(String methodDescriptor) {
        ArrayList<String> params = processMethodParamNum(methodDescriptor);
        int[] result = new int[params.size()];
        for (int i = 0; i < params.size(); i++) {
            result[i] = paramOp(params.get(i));
        }
        return result;


    }

    private static int paramOp(String param) {

        if (param.contains(";") || param.contains("[")) {
            return Opcodes.ALOAD;
        }
        if (param.equals("I") ||
                param.equals("B") ||
                param.equals("C") ||
                param.equals("Z") ||
                param.equals("S")) {
            return Opcodes.ILOAD;
        }
        if (param.equals("J")) {
            return Opcodes.LLOAD;
        }
        if (param.equals("D")) {
            return Opcodes.DLOAD;
        }
        if (param.equals("F")) {
            return Opcodes.FLOAD;
        }
        return Opcodes.ALOAD;
    }

    private static ArrayList<String> processMethodParamNum(String methodDescription) {
        ArrayList<String> r = new ArrayList<>();
        int indexL = methodDescription.indexOf("(");
        int indexR = methodDescription.indexOf(")");
        if (indexR - indexL <= 1) {
            return r;
        }
        r = readOneByOne(methodDescription.substring(indexL + 1, indexR), r);
        return r;
    }

    private static ArrayList<String> readOneByOne(String s, ArrayList<String> r) {
        if (s.isEmpty() || s.equals(")")) {
            return r;
        }
        int index = 0;
        char firstChar = s.toCharArray()[index];
        while (firstChar == '[') {
            index = index + 1;
            firstChar = s.toCharArray()[index];
        }
        if (isObject(firstChar)) {
            int indexEnd = s.indexOf(";");
            String params = s.substring(0, indexEnd + 1);
            r.add(params);
            String sub = s.substring(indexEnd + 1);
            return readOneByOne(sub, r);
        }

        if (isBasicType(firstChar)) {
            String params = s.substring(0, 1 + index);
            r.add(params);
            String sub = s.substring(1 + index);
            return readOneByOne(sub, r);
        }
        return r;
    }

    private static boolean isBasicType(char c) {

        return (c == 'I' ||
                c == 'B' ||
                c == 'C' ||
                c == 'S' ||
                c == 'J' ||
                c == 'D' ||
                c == 'Z' ||
                c == 'F');

    }

    private static boolean isObject(char c) {
        if (c == 'L') {
            return true;
        }
        return false;
    }
}
