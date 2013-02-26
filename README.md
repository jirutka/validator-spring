JSR-303 Validator using Spring Expression Language (SpEL)
=========================================================

This library provides JSR-303 validation constraint with which you can use
powerful [Spring Expression Language](http://static.springsource.org/spring/docs/current/spring-framework-reference/html/expressions.html)
(SpEL) for non-trivial validations. It’s especially very useful for
_cross-field validations_ which are very complicated with plain JSR-303.


Usage examples
--------------

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

```java
@SpELAssert(value = "#isEven(count) && count > 42", applyIf = "enabled",
            helpers = Helpers.class)
public class Sample {

    private int count;
    private boolean enabled;
}

public final class Helpers {

    public static boolean isEven(int value) {
        return value % 2 == 0;
    }
    public static boolean isOdd(int value) {
        return value % 2 != 0;
    }
}
```


Maven
-----

```xml
<dependency>
    <groupId>cz.jirutka.validator</groupId>
    <artifactId>validator-spring</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

<repository>
    <id>cvut-local-repos</id>
    <name>CVUT Repository Local</name>
    <url>http://repository.fit.cvut.cz/maven/local-repos/</url>
</repository>
```


License
-------

This project is licensed under [MIT license](http://opensource.org/licenses/MIT).
