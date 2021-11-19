package io.github.wufei.parse;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import io.github.wufei.config.AsmConfig;

public interface AsmConfigParser {
    @NotNull
    AsmConfig parseAsmConfig(File asmConfigFile) throws IOException;
}
