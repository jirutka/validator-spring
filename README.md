Bean Validator utilizing SpEL
=============================
[![Build Status](https://travis-ci.org/jirutka/validator-spring.svg?branch=master)](https://travis-ci.org/jirutka/validator-spring)
[![Coverage Status](http://img.shields.io/coveralls/jirutka/validator-spring.svg?style=flat)](https://coveralls.io/r/jirutka/validator-spring)
[![Codacy code quality](https://api.codacy.com/project/badge/grade/3e4ab872dba9426ca74b49faccd8ad38)](https://www.codacy.com/app/jirutka/validator-spring)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.validator/validator-spring/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.validator/validator-spring)

This library provides [Bean Validation] \(JSR 303/349) constraint that allows to use powerful
**[Spring Expression Language]** \(SpEL) for non-trivial validations. It’s especially very useful for _cross-field
validations_ that are very complicated with a plain Bean Validation.


Usage examples
--------------

### Cross-field validation

```java
@SpELAssert(value = "hasRedirectUris()", applyIf = "grantTypes.contains('auth_code')",
            message = "{validator.missing_redirect_uri}")
public class ClientDTO {

	private Collection<String> grantTypes;
	private Collection<String> redirectUris;

    public boolean hasRedirectUris() {
        return !redirectUris.isEmpty();
    }
}
```

```java
@SpELAssert(value = "password.equals(passwordVerify)",
            applyIf = "password || passwordVerify",
            message = "{validator.passwords_not_same}")
public class User {

    private String password;
    private String passwordVerify;
}
```

### Using helper functions

```java
@SpELAssert(value = "#isEven(count) && count > 42", applyIf = "enabled",
            helpers = Helpers.class)
public class Sample {

    private int count;
    private boolean enabled;
}
```

```java
public class Sample {

    @SpELAssert(value = "#isEven(#this) && #this > 42",
                helpers = Helpers.class)
    private int count;
}
```

```java
public final class Helpers {

    public static boolean isEven(int value) {
        return value % 2 == 0;
    }
    public static boolean isOdd(int value) {
        return value % 2 != 0;
    }
}
```

### Using Spring beans

```java
public class Sample {

    @SpELAssert("@myService.calculate(#this) > 42")
    private int value;
}
```

```java
// Configuration is needed to allow autowiring of dependencies in custom validators.
@Configuration
public class ValidatorConfig {

    @Bean
    public LocalValidatorFactoryBean validatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }
}
```

Maven
-----

Released versions are available in The Central Repository. Just add this artifact to your project:

```xml
<dependency>
    <groupId>cz.jirutka.validator</groupId>
    <artifactId>validator-spring</artifactId>
    <version>1.1.0</version>
</dependency>
```

However if you want to use the last snapshot version, you have to add the Sonatype OSS repository:

```xml
<repository>
    <id>sonatype-snapshots</id>
    <name>Sonatype repository for deploying snapshots</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```


License
-------

This project is licensed under [MIT license](http://opensource.org/licenses/MIT).


[Bean Validation]: http://beanvalidation.org/1.1/spec/
[Spring Expression Language]: http://static.springsource.org/spring/docs/current/spring-framework-reference/html/expressions.html
