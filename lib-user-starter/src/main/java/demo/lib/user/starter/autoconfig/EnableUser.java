package demo.lib.user.starter.autoconfig;

import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.AsyncConfigurationSelector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({UserConfigSelector.class})
public @interface EnableUser {

}