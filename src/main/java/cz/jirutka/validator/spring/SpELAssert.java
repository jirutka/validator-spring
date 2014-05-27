/*
 * The MIT License
 *
 * Copyright 2013 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cz.jirutka.validator.spring;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * JSR-303 constraint for validation with Spring Expression Language (SpEL).
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, TYPE})
@Constraint(validatedBy = SpELAssertValidator.class)
public @interface SpELAssert {

    String message() default "{cz.jirutka.validator.spring.SpELAssert.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    /**
     * Validation expression in SpEL.
     *
     * @see <a href="http://static.springsource.org/spring/docs/3.0.x/reference/expressions.html">
     *     documentation of Spring Expression Language</a>
     */
    String value();

    /**
     * Perform validation only if this SpEL expression evaluates to <tt>true</tt>.
     *
     * @see <a href="http://static.springsource.org/spring/docs/3.0.x/reference/expressions.html">
     *     documentation of Spring Expression Language</a>
     */
    String applyIf() default "";

    /**
     * Classes with static methods to register as helper functions which you
     * can call from expression as <tt>#methodName(arg1, arg2, ...)</tt>.
     * This does not support overloading, only last registered method of each
     * name will be used!
     */
    Class<?>[] helpers() default { };
}
