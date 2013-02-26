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
package cz.jirutka.validator.spring.support;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.TypeConverter;

import java.util.Collection;

/**
 * Decorator for {@linkplain TypeConverter} that can convert numbers,
 * collections and arrays to boolean value. Not matching types delegates to
 * the decorated converter.
 *
 * <h2>Number -> Boolean:</h2>
 * <tt>value = 0 ? false : true</tt>
 *
 * <h2>Collection -> Boolean:</h2>
 * <tt>value.isEmpty()? ? false : true</tt>
 *
 * <h2>Array -> Boolean:</h2>
 * <tt>value.length == 0 ? false : true</tt>
 *
 * <h2>Else</h2>
 * delegate to decorated
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class RelaxedBooleanTypeConverterDecorator implements TypeConverter {

    private static final TypeDescriptor BOOLEAN_TYPE = TypeDescriptor.valueOf(Boolean.class);
    private static final TypeDescriptor NUMBER_TYPE = TypeDescriptor.valueOf(Number.class);

    private final TypeConverter decorated;


    /**
     * @param decorated converter that will handle all unsupported conversions
     */
    public RelaxedBooleanTypeConverterDecorator(TypeConverter decorated) {
        this.decorated = decorated;
    }


    public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return targetType.isAssignableTo(BOOLEAN_TYPE)
                && (
                    sourceType.isAssignableTo(NUMBER_TYPE)
                    || sourceType.isCollection()
                    || sourceType.isArray()
                )
                || decorated.canConvert(sourceType, targetType);
    }

    public Object convertValue(Object value, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (targetType.isAssignableTo(BOOLEAN_TYPE)) {
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            }
            if (sourceType.isCollection()) {
                return ! ((Collection) value).isEmpty();
            }
            if (sourceType.isArray()) {
                return ((Object[]) value).length != 0;
            }
            return value;

        } else {
            return decorated.convertValue(value, sourceType, targetType);
        }
    }
}
