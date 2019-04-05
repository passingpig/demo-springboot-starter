我们以一个简单的例子来谈谈组件中的一个Bean，可以通过怎样的方式注册到业务方的Spring容器中；
首先我们来回顾下以前常用的配置方式，然后尝试着结合`@EnableConfiguration`达成全自动配置，顺带了解条件注解`@ConditionalOnXXX`，最后看看开启注解`@EnableXXX`的玩法。相信大家看完后，会对SpringBoot相关组件的自动化配置有个大概的了解。

# 1.需求
需求非常简单：我们提供一个组件（lib-user.jar），主要用于拉取用户信息。
里面有个Bean（接口为`UserService`），我们希望其他项目引入jar包后，可以使用到这个Bean（`@Autowire`）

# 2. 传统的配置方式
## 2.1 先建个项目lib-user
先看看pom.xml，只需要满足组件需求即可（本例依赖了SpringBoot）：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.4.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <groupId>demo</groupId>
    <artifactId>lib-user-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```
## 2.2 核心功能
（1）接口 UserService 
```java
public interface UserService {
    User getByToken(String token);
}
```
（2）默认实现 DefaultUserServiceImpl
```java
package demo.lib.user.service;

@Service
public class DefaultUserServiceImpl implements UserService {
    @Override
    public User getByToken(String token) {
        System.out.println("UserService----in---" + token);
        return new User();
    }
}
```
好了，打包、deploy完事。业务方的大佬们可以使用了。
## 2.3 业务方使用
假如我们有个支付组的**项目demo-pay**，是个springBoot项目，启动类为demo.pay.PayApplication。
接入时，我们需要在pom.xml加入依赖：
```xml
<dependency>
    <groupId>demo</groupId>
    <artifactId>lib-user</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
引入依赖后，尽管组件中`DefaultUserServiceImpl` 有`@Service`注解，但是我们支付项目默认情况下是扫描不到这个bean的（默认扫描的包为：demo.pay）。只能自己去配置了，以下配置方式只需选一种即可：
（1）xml
```xml
  <context:component-scan base-package="demo.lib.user.service" />
```
（2）@ComponentScan
```java
package demo.pay.config;

@Configuration
@ComponentScan("demo.lib.user.service")
public class UserConfig {
    
}
```
（3）@Bean
```java
package demo.pay.config;

@Configuration
public class UserConfig {
    @Bean
    public UserService userService() {
        return new DefaultUserServiceImpl();
    }
}
```
# 3. SpringBoot全自动配置
## 3.1 spring.factories 借助@EnableAutoConfiguration的魔法
我们**再建**一个项目：**lib-user-starter**，这个项目和一般的starter一样，只做两件事：

**（1）引入相关的依赖**
就如下面的pom.xml，我们这个starter需要引入lib-user依赖。
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.4.RELEASE</version>
    </parent>

    <groupId>demo</groupId>
    <artifactId>lib-user-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>demo</groupId>
            <artifactId>lib-user</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```
**（2）帮助自动化配置**
先把配置类写了：
```java
package demo.lib.user.starter.autoconfig;

@Configuration
public class UserAutoConfiguration {
    @Bean
    public UserService userService() {
        return new DefaultUserServiceImpl();
    }
}
```
重点来了，我们需要在resource目录下，**新建一个/META-INF/spring.factories**：![](https://upload-images.jianshu.io/upload_images/7779607-4ee41fb618ed4e10.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
把我们的配置类加上去，类似Properties的key=value写法，“\”用于为换行（实际上key还是上面那个的意思）
```

org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
demo.lib.user.starter.autoconfig.UserAutoConfiguration

```
多个可以使用“,”隔开，参考spring-boot-autoconfigure下的spring.factories：
```
# Auto Configure
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration,\
org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,\
org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration,\
....
```
好了，已经完事，lib-user-starter进行deploy一下，业务大佬们改用这个吧，我们都配置好了。
这时候业务方**只需加入starter依赖，不需要任何配置**，可以直接`@Autowire UserService `这个bean了。
```xml
<dependency>
    <groupId>demo</groupId>
    <artifactId>lib-user-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
> 拓展阅读：原理简单地讲，SpringBoot启动类上往往会有`@SpringBootApplication`注解，而这个注解包含了`@EnableAutoConfiguration`，项目启动后，当解析`@EnableAutoConfiguration`时，会通过`org.springframework.core.io.support.SpringFactoriesLoader`将META-INF/spring.factories中的key为EnableAutoConfiguration的所有value加载出来（一堆配置类），然后就可以进行常规的`@Configuration`配置类解析了。

## 3.2 条件注解@ConditionalOnClass、@ConditionalOnMissingBean
在我们的@Configuration的配置类中，我们可以根据当前项目的环境做出一些特定配置、或者不做配置。
举个例子，我们的UserConfig，我们希望我们的项目里面有一个`demo.base.UserInfo`的类才进行配置，否则当spring你就忽视我这个配置类吧：
```java
@Configuration
@ConditionalOnClass(UserInfo.class)
public class UserBeanConfig {
    @Bean
    public UserService userService() {
        return new DefaultUserServiceImpl();
    }
}
```
还有一种情况，有时候业务方不希望用我们的`DefaultUserServiceImpl`，而是用自己整的UserService实现，这时候我们那个DefaultUserServiceImpl就不应该注册成bean了，否则可能会报冲突，`@ConditionalOnMissingBean`可以帮助我们避免这样情况：
```java
@Configuration
public class UserBeanConfig {

    // 没发现UserService的bean才注册DefaultUserServiceImpl
    @Bean
    @ConditionalOnMissingBean
    public UserService userService() {
        return new DefaultUserServiceImpl();
    }
}
```
除此之外，还有其他许多类似的注解，比如只有某个存在Bean才进行配置的`@ConditionalOnBean`、根据不同环境（比如测试和生成环境）进行不同配置的`@Profile` 等等。

## 3.3 属性配置与提示
假如我们的用户中心需要分配一个appId和appSecret才能访问，这时候我们需要提供出对应的配置了。
新增UserProperties：
```java
@ConfigurationProperties(prefix = "user")
public class UserProperties {

    private String appId = "123456"; // 默认值
    private String appSecret;

    // getter、setter
}
```
UserConfiguration修改为：
```java
@Configuration
@EnableConfigurationProperties(UserProperties.class)
public class UserConfiguration {

    @Bean
    public UserService userService(UserProperties userProperties) {
        DefaultUserServiceImpl userService = new DefaultUserServiceImpl();
        // DefaultUserServiceImpl 新增两个属性AppId和AppSecret用于组装数据
        userService.setAppId(userProperties.getAppId());
        userService.setAppSecret(userProperties.getAppSecret());
        return userService;
    }
}
```
新增提示文件：**META-INF/spring-configuration-metadata.json**，用于ide提示用户（注：仅仅是起提示作用，没有也不会影响组件正常运作）：
![](https://upload-images.jianshu.io/upload_images/7779607-f18309a043207687.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
```json
{
  "properties": [
    {
      "name": "user.appId",
      "type": "java.lang.String",
      "description": "用户中心分配的appId",
      "defaultValue": "123456"
    },
    {
      "name": "user.appSecret",
      "type": "java.lang.String",
      "description": "用户中心分配的appSecret."
    }
  ]
}
```
这时候，当我们在demo-pay的项目输入配置时：
![springboot配置提示](https://upload-images.jianshu.io/upload_images/7779607-7d2ce6ee1ec25fe0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

> 拓展阅读：除了`@ConfigurationProperties`外，`@Value`同样可以达到加载配置的作用。

## 3.4 @EnableXXX，开启类配置
在spring.factories中指定EnableAutoConfiguration=配置类，**一旦引入lib包，就会自动启动该配置**。这也是我们SpringBoot AutoConfig的常规操作，但是有时候我们希望把这个开关交给具体的项目来管理。这也是为什么```@EnableXXX```这类注解的存在（如：`@EnableAsync`）。
首先我们先把spring.factories删掉，再新建一个```@EnableUser```注解以及对应的ImportSelector ：
```java
// EnableUser.java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({AsyncConfigurationSelector.class})
public @interface EnableUser {
}

// UserConfigSelector.java
public class UserConfigSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        // 返回配置类的全类名
        return new String[]{"demo.lib.user.starter.autoconfig.UserConfiguration"};
    }
}
```
好了，这时候给业务大佬用了。我们来到demo-pay项目启动类，如果我们需要这个配置：
```java
@SpringBootApplication
@EnableUser
public class DemoPayApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoPayApplication.class, args);
    }
}
```
> 拓展阅读：
关于`@Import`注解，其value可以指定为：
> 1. @Configuration配置类；
> 2. 实现ImportSelector的类；其接口方法声明：`String[] selectImports(AnnotationMetadata importingClassMetadata);`，入参是`@EnableXXX`中相关属性，返回是一个字符串数组（@Configuration配置类全类名），这些配置类会被加载解析；
> 3. 实现ImportBeanDefinitionRegistrar的类，该接口方法声明：void `registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry);`入参第一个为`@EnableXXX`中相关属性，第二个则为SpringIOC容器中用于注册Bean（配置）的接口`BeanDefinitionRegistry`，可以手动地注册一些Bean（配置）。比如：`@EnableAspectJAutoProxy`中的`AspectJAutoProxyRegistrar`，Mybatis中`@MapperScan`中的`MapperScannerRegistrar`等。相对而言这个有更加强的控制权（为所欲为）。