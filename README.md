# cas35-addon-rest
Modular SpringMVC @Controller based implementation of CAS REST endpoints for CAS 3.5.x series

> This project was developed as part of Unicon's [Open Source Support program](https://unicon.net/opensource).
Professional Support / Integration Assistance for this module is available. For more information [visit](https://unicon.net/opensource/cas).

## Motivation

Current Restlet-based REST module for CAS 3.5.x series has completely broken some overlay builds with runtime dependencies conflicts hell in Restlet and other Spring modules like `cglib`, and `asm`. Rather then "fight" these to squeeze this heavyweight Restlet library into existing overlays, there is a better, lighter-weight implementetion based on SpringMVC `@Controller` programming model, which supports a semi-auto configuration (no need to define any Spring beans, just include the jar on the apps's classpath).

This implementation is borrowed from the REST module in CAS which will be available in CAS `4.1`. By having a seprate micro addon with the same code, CAS `3.5` based deploymnent could enjoy the same enhaced REST implementation that CAS `4.1` builds would enjoy.

### Current version
`1.0.0-GA`

### Supported CAS version
The minimum supported CAS server version is `3.5.1+`

## Usage

* Define a dependency in your CAS war overlay:

  > Maven:

  ```xml
  <dependency>
      <groupId>net.unicon.cas</groupId>
      <artifactId>cas35-addon-rest</artifactId>
      <version>1.0.0-GA</version>
      <scope>runtime</scope>
  </dependency>
  ```

  > Gradle:

  ```Groovy
  dependencies {
        ...
        runtime 'net.unicon.cas:cas35-addon-rest:1.0.0-GA'
        ...
  }
  ```

* Add `classpath*:/META-INF/spring/*.xml` entry to `contextConfigLocation`

  > in `WEB-INF/web.xml`

  ```xml
  <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>
            /WEB-INF/spring-configuration/*.xml
            /WEB-INF/deployerConfigContext.xml
            classpath*:/META-INF/spring/*.xml
        </param-value>
  </context-param>
  ```
* Add `/v1/*` servlet mapping to `cas` servlet

  > in `WEB-INF/web.xml`

  ```xml
  <servlet-mapping>
      <servlet-name>cas</servlet-name>
      <url-pattern>/v1/*</url-pattern>
  </servlet-mapping>
  ```
