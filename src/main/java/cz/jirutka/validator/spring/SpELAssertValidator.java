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

import cz.jirutka.validator.spring.support.RelaxedBooleanTypeConverterDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static cz.jirutka.validator.spring.support.ReflectionUtils.extractStaticMethods;
import static org.springframework.util.StringUtils.hasText;

/**
 * Constraint validator for {@link SpELAssert} that evaluates Spring Expression (SpEL).
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class SpELAssertValidator implements ConstraintValidator<SpELAssert, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(SpELAssertValidator.class);
    private static final TypeConverter TYPE_CONVERTER
            = new RelaxedBooleanTypeConverterDecorator(new StandardTypeConverter());

    private Expression expression;
    private Expression applyIfExpression;
    private List<Method> functions = new LinkedList<>();

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private ApplicationContext applicationContext;

    public void initialize(SpELAssert constraint) {
        ExpressionParser parser = new SpelExpressionParser();

        expression = parser.parseExpression(constraint.value());
        if (hasText(constraint.applyIf())) {
            applyIfExpression = parser.parseExpression(constraint.applyIf());
        }
        for (Class<?> clazz : constraint.helpers()) {
            functions = extractStaticMethods(clazz);
        }
    }

    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null) return true;

        EvaluationContext evalContext = createEvaluationContext(object);

        if (isApplyIfValid(evalContext)) {
            LOG.trace("Evaluating expression {{}} on object: {}", expression.getExpressionString(), object);
            return evaluate(expression, evalContext);
        }
        return true;
    }


    private boolean isApplyIfValid(EvaluationContext context) {
        if (applyIfExpression == null) return true;

        LOG.trace("Evaluating applyIf {{}} on object: {}", applyIfExpression.getExpressionString(), context);
        return evaluate(applyIfExpression, context);
    }

    private boolean evaluate(Expression expression, EvaluationContext context) {
        Boolean result = expression.getValue(context, Boolean.class);
        return result == null ? false : result;
    }

    private StandardEvaluationContext createEvaluationContext(Object rootObject) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        context.setRootObject(rootObject);
        context.setTypeConverter(TYPE_CONVERTER);
        if (applicationContext != null) {
            context.setBeanResolver(new BeanResolver() {
                @Override
                public Object resolve(EvaluationContext evaluationContext, String beanName) throws AccessException {
                    return applicationContext.getBean(beanName);
                }
            });
        }

        if (! functions.isEmpty()) {
            for (Method helper : functions) {
                context.registerFunction(helper.getName(), helper);
            }
            LOG.trace(inspectFunctions(context));
        }

        return context;
    }

    private String inspectFunctions(EvaluationContext context) {
        StringBuilder message = new StringBuilder();
        Set<String> names = new HashSet<>(functions.size());

        message.append("Registered functions: \n");

        for (Method function : functions) {
            names.add(function.getName());
        }
        for (String name : names) {
            Object obj = context.lookupVariable(name);
            if (obj instanceof Method) {
                message.append("     #").append(name).append(" -> ").append(obj).append("\n");
            }
        }
        return message.toString();
    }
}
