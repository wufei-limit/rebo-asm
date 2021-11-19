package io.github.wufei;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;


public class AsmFeature implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        AppExtension extension = (AppExtension) project.getProperties().get("android");
        if (extension == null){
            return;
        }
        extension.registerTransform(new RoboAsmTransform(project.getLogger(), project));
    }

}
