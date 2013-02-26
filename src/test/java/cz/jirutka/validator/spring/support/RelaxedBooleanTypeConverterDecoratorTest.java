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

import org.mockito.Mockito;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.TypeConverter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class RelaxedBooleanTypeConverterDecoratorTest {

    private static final TypeDescriptor 
            BOOLEAN_TYPE = TypeDescriptor.valueOf(Boolean.class),
            NUMBER_TYPE = TypeDescriptor.valueOf(Number.class),
            COLLECTION_TYPE = TypeDescriptor.valueOf(Collection.class),
            ARRAY_TYPE = TypeDescriptor.forObject(new Object[]{}),
            STRING_TYPE = TypeDescriptor.valueOf(String.class);

    private TypeConverter parentTypeConverter;
    private TypeConverter typeConverter;


    @BeforeMethod
    public void initialize() {
        parentTypeConverter = Mockito.mock(TypeConverter.class);
        typeConverter = new RelaxedBooleanTypeConverterDecorator(parentTypeConverter);
    }

    @DataProvider
    public Object[][] supportedTypes() {
        return new Object[][] {
                { NUMBER_TYPE },
                { COLLECTION_TYPE },
                { ARRAY_TYPE }
        };
    }

    @DataProvider
    public Object[][] exampleValues() {
        return new Object[][] {
                { 0,                         false },
                { 1,                         true  },
                { 42,                        true  },
                { 0L,                        false },
                { 1L,                        true  },
                { emptyList(),               false },
                { asList("non-empty"),       true  },
                { new String[]{},            false },
                { new String[]{"non-empty"}, true  }
        };
    }


    @Test(dataProvider = "supportedTypes")
    public void should_pass_canConvert_for_supported_types(TypeDescriptor sourceType) {
        doReturn(false).when(parentTypeConverter).canConvert(sourceType, BOOLEAN_TYPE);

        assertTrue(typeConverter.canConvert(sourceType, BOOLEAN_TYPE), String.format(
                "Converter should be able to convert from %s to %s", sourceType.asString(), BOOLEAN_TYPE.asString()));
    }

    @Test
    public void should_delegate_canConvert_whe_target_not_boolean() {
        typeConverter.canConvert(STRING_TYPE, NUMBER_TYPE);

        verify(parentTypeConverter, times(1)).canConvert(STRING_TYPE, NUMBER_TYPE);
    }

    @Test(dataProvider = "exampleValues")
    public void should_convert_given_values_to_boolean(Object source, Object expected) {
        Object actual = typeConverter.convertValue(source, TypeDescriptor.forObject(source), BOOLEAN_TYPE);

        assertEquals(actual, expected);
    }

    @Test
    public void should_delegate_conversion_when_target_not_boolean() {
        typeConverter.convertValue("42", STRING_TYPE, NUMBER_TYPE);

        verify(parentTypeConverter, times(1)).convertValue("42", STRING_TYPE, NUMBER_TYPE);
    }
}
