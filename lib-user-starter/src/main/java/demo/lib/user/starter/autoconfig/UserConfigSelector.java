package demo.lib.user.starter.autoconfig;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class UserConfigSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{"demo.lib.user.starter.autoconfig.UserAutoConfiguration"};
    }
}
