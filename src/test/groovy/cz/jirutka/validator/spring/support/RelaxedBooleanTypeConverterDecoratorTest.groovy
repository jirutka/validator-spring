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
package cz.jirutka.validator.spring.support

import org.springframework.core.convert.TypeDescriptor
import org.springframework.expression.TypeConverter
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class RelaxedBooleanTypeConverterDecoratorTest extends Specification {

    def parentConverter = Mock(TypeConverter)
    def converter = new RelaxedBooleanTypeConverterDecorator(parentConverter)


    def 'canConvert: returns true for #sourceType.simpleName -> Boolean'() {
        setup:
            0 * parentConverter.canConvert(*_)
        expect:
            converter.canConvert(type(sourceType), type(Boolean))
        where:
            sourceType << [Number, Collection, Object[]]
    }

    def 'canConvert: delegates to parent when target type is not boolean'() {
        when:
            converter.canConvert(type(String), type(Number))
        then:
            1 * parentConverter.canConvert(type(String), type(Number))
    }


    def 'convertValue: converts #value to #expected'() {
        expect:
            converter.convertValue(value, type(value.class), type(Boolean)) == expected
        where:
            value               | expected
            0                   | false
            1                   | true
            42                  | true
            0L                  | false
            1L                  | true
            []                  | false
            ['list']            | true
            [].toArray()        | false
            ['array'].toArray() | true
    }

    def 'convertValue: delegates to parent when target type is not boolean'() {
        when:
            converter.convertValue('42', type(String), type(Number))
        then:
            1 * parentConverter.convertValue('42', type(String), type(Number))
    }


    ////////// Helpers //////////

    def type(obj) {
        if (obj instanceof Class) {
            TypeDescriptor.valueOf(obj)
        } else {
            TypeDescriptor.forObject(obj)
        }
    }
}
