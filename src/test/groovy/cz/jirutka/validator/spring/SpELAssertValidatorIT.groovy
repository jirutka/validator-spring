/*
 * The MIT License
 *
 * Copyright 2013-2015 Jakub Jirutka <jakub@jirutka.cz>.
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
package cz.jirutka.validator.spring

import groovy.transform.CompileStatic
import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.cfg.ConstraintDef
import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.Validation

@Unroll
class SpELAssertValidatorIT extends Specification {

    def "validate #entity with @SpELAssert(value = '#value')"() {
        given:
            def constraint = new SpELAssertDef(value: value)
        expect:
            assert isValid(constraint, entity as StubEntity) == expected,
                    "if '$value', then entity $entity should ${expected ? '' : '*not*'} be valid"
        where:
            value                               | entity               || expected
            "a == b"                            | [a: 42,    b: 42]    || true
            "a == b"                            | [a: 0,     b: 66]    || false
            "a.equals(b)"                       | [a: 'foo', b: 'foo'] || true
            "!c"                                | [c: []]              || true
            "a"                                 | [c: []]              || false
            "c"                                 | [c: ['foo']]         || true
            "c.contains('foo')"                 | [c: ['foo']]         || true
            "sayHello(a).equals('hello, Jan!')" | [a: 'Jan']           || true
            "sayHello(a).equals('hello, Dan!')" | [a: 'Jan']           || false
    }

    def "validate #entity with @SpELAssert(value = '#value', applyIf = '#applyIf')"() {
        given:
            def constraint = new SpELAssertDef(value: value, applyIf: applyIf)
        expect:
            assert isValid(constraint, entity as StubEntity) == expected,
                    "if '$value' and '$applyIf' or not '$applyIf', " +
                    "then entity $entity should ${expected ? '' : '*not*'} be valid"
        where:
            value                               | applyIf   | entity               || expected
            "a > b"                             | 'a == 42' | [a: 42,    b: 24]    || true
            "a > b"                             | 'a == 42' | [a: 0,     b: 66]    || true
            "a == b"                            | 'a == 42' | [a: 42,    b: 66]    || false
            "a.equals('foo')"                   | 'b'       | [a: 'bar', b: false] || true
            "a.equals('foo')"                   | 'b'       | [a: 'foo', b: 1]     || true
            "a == 66"                           | 'b'       | [a: 0,     b: true]  || false
    }

    def "validate #entity with @SpELAssert(value = '#value', helpers = Helpers)"() {
        given:
            def constraint = new SpELAssertDef(value: value, helpers: [Helpers])
        expect:
            assert isValid(constraint, entity as StubEntity) == expected,
                    "if $value, then entity $entity should ${expected ? '' : '*not*'} be valid"
        where:
            value                 | entity      || expected
            "#isEven(a)"          | [a: 2]      || true
            "#countChars(a) == 4" | [a: 'cool'] || true
            "#isEven(a)"          | [a: 1]      || false
    }


    ////////// Helpers //////////

    def isValid(ConstraintDef constraintDef, entity) {
        createValidator(StubEntity, constraintDef).validate(entity).isEmpty()
    }

    def createValidator(Class type, ConstraintDef constraint) {
        def cfg = Validation.byProvider(HibernateValidator).configure()
        cfg.addMapping( cfg.createConstraintMapping().with { mapping ->
            mapping.type(type).constraint(constraint); mapping
        })
        cfg.buildValidatorFactory().validator
    }


    @CompileStatic
    class SpELAssertDef extends ConstraintDef<SpELAssertDef, SpELAssert> {

        SpELAssertDef() {
            super(SpELAssert)
        }

        def setValue(String value) {
            addParameter('value', value)
        }

        def setApplyIf(String applyIf) {
            addParameter('applyIf', applyIf)
        }

        def setHelpers(Class[] helpers) {
            addParameter('helpers', helpers)
        }
    }

    @CompileStatic
    static class StubEntity {

        Object a
        Object b
        Collection c

        // not needed for Groovy, just to be more clear for non-Groovy devs
        Object getA() { a }
        Object getB() { b }
        Collection getC() { c }

        String sayHello(String name) {
            "hello, ${name}!"
        }
    }

    @CompileStatic
    static class Helpers {

        static boolean isEven(int value) {
            value % 2 == 0
        }
        static boolean isOdd(int value) {
            value % 2 != 0
        }
        static int countChars(String value) {
            value.toCharArray().length
        }
    }
}
