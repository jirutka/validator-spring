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

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.testng.Assert.fail;
import static org.testng.util.Strings.isNullOrEmpty;

/**
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class SpELAssertValidatorTest {

    private Class<?>[] helpers = { Helpers.class };


    @DataProvider
    public Object[][] validEntities() {
        return new Object[][] {
               // value                     applyIf             helpers     object
               //------------------------------------------------------------------------------------
                { "first == second",        "",                 null,       new Mock(42, 42)        },
                { "first > second",         "first == 42",      null,       new Mock(42, 24)        },
                { "first > second",         "first == 42",      null,       new Mock(0, 66)         },
                { "first.equals('foo')",    "second",           null,       new Mock("bar", false)  },
                { "first.equals('foo')",    "second",           null,       new Mock("foo", 1)      },
                { "first.equals(second)",   "",                 null,       new Mock("foo", "foo")  },
                { "!third",                 "",                 null,       new Mock(emptyList())   },
                { "third",                  "",                 null,       new Mock(asList("foo")) },
                { "third.contains('foo')",  "",                 null,       new Mock(asList("foo")) },
                { "sayHello(first).equals('hello, John!')", "", null,       new Mock("John")        },
                { "#isEven(first)",         "",                 helpers,    new Mock(2)             },
                { "#countChars(first) == 4","",                 helpers,    new Mock("cool")        }
        };
    }

    @DataProvider
    public Object[][] invalidEntities() {
        return new Object[][] {
               // value                     applyIf             helpers     object
               //------------------------------------------------------------------------------------
                { "first == second",        "",                 null,       new Mock(0, 66)         },
                { "first == second",        "first == 42",      null,       new Mock(42, 66)        },
                { "first == 66",            "second",           null,       new Mock(0, true)       },
                { "first",                  "",                 null,       new Mock(emptyList())   },
                { "sayHello(first).equals('hello, foo!')", "",  null,       new Mock("John")        },
                { "#isEven(first)",         "",                 helpers,    new Mock(1)             }
        };
    }


    @Test(dataProvider = "validEntities")
    public void test_valid_entities(String expression, String applyIf, Class<?>[] helpers, Object entity) {
        Validator validator = createValidator(entity.getClass(),
                new SpELAssertDef()
                        .value(expression)
                        .applyIf(applyIf)
                        .helpers(helpers != null ? helpers : new Class[]{}));

        String message = isNullOrEmpty(applyIf)
                ? String.format("if '%s', then entity [%s] should be valid", expression, entity)
                : String.format("if ('%1$s' and '%2$s') or not('%1$s'), then entity [%3$s] should be valid",
                                applyIf, expression, entity);

        if (! validator.validate(entity).isEmpty()) {
            fail(message);
        }
    }

    @Test(dataProvider = "invalidEntities")
    public void test_invalid_entities(String expression, String applyIf, Class<?>[] helpers, Object entity) {
        Validator validator = createValidator(entity.getClass(),
                new SpELAssertDef()
                        .value(expression)
                        .applyIf(applyIf)
                        .helpers(helpers != null ? helpers : new Class[]{}));

        String message = isNullOrEmpty(applyIf)
                ? String.format("if '%s', then entity [%s] should *not* be valid", expression, entity)
                : String.format("if ('%1$s' and '%2$s') or not('%1$s'), then entity [%3$s] should *not* be valid",
                                applyIf, expression, entity);

        if (validator.validate(entity).isEmpty()) {
            fail(message);
        }
    }


    private Validator createValidator(Class<?> type, ConstraintDef<?, ?> constraint) {
        HibernateValidatorConfiguration cfg = Validation.byProvider(HibernateValidator.class).configure();
        ConstraintMapping mapping = cfg.createConstraintMapping();
        mapping.type(type).constraint(constraint);
        cfg.addMapping(mapping);

        return cfg.buildValidatorFactory().getValidator();
    }



    ////////// Mocks //////////

    static class Mock {
        private Object first;
        private Object second;
        private Collection third;

        Mock(Object first, Object second) {
            this.first = first;
            this.second = second;
        }
        Mock(Object first) {
            this.first = first;
        }
        Mock(Collection third) {
            this.third = third;
        }

        public Object getFirst() { return first; }
        public Object getSecond() { return second; }
        public Collection getThird() { return third; }

        public String sayHello(String name) {
            return String.format("hello, %s!", name);
        }

        public String toString() {
            return String.format("first = %s, second = %s, third = %s", first, second, third);
        }
    }

    static class Helpers {
        public static boolean isEven(int value) {
            return value % 2 == 0;
        }
        public static boolean isOdd(int value) {
            return value % 2 != 0;
        }
        public static int countChars(String value) {
            return value.toCharArray().length;
        }
    }
}
