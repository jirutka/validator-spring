package cz.jirutka.validator.spring;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.validation.*;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;


public class SpELAssertValidatorBeanAccessTest implements ConstraintValidatorFactory {

    @Mock
    private ApplicationContext applicationContext;

    private Validator validator;

    @BeforeTest
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(applicationContext.getBean("mockService")).thenReturn(new MockService());

        Configuration<?> config = Validation.byDefaultProvider().configure();
        config.constraintValidatorFactory(this);
        ValidatorFactory factory = config.buildValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void test_valid_service_call() {
        Set<ConstraintViolation<MockBean>> constraintViolations = validator.validate(new MockBean("VALID"));

        assertThat(constraintViolations.size(), is(0));
    }

    @Test
    public void test_invalid_service_call() {
        Set<ConstraintViolation<MockBean>> constraintViolations = validator.validate(new MockBean("INVALID"));

        assertThat(constraintViolations.size(), is(1));
        ConstraintViolation<MockBean> violation = constraintViolations.iterator().next();
        assertThat(violation.getMessage(), is("must equal VALID"));
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> aClass) {
        SpELAssertValidator validator = new SpELAssertValidator();
        validator.setApplicationContext(applicationContext);
        return (T) validator;
    }

    ////////// Mocks //////////

    private static class MockBean {
        @SpELAssert(value = "@mockService.isValid(#this)", message = "must equal VALID")
        private String field;

        public MockBean(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }

    private static class MockService {
        public boolean isValid(String value) {
            return "VALID".equals(value);
        }
    }
}
