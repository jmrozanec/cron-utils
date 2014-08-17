package com.cron.utils.validator;

import com.cron.utils.CronType;
import com.cron.utils.model.CronDefinition;
import com.cron.utils.parser.CronDefinitionRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CronValidatorTest {

    private CronDefinition testCronDefinition;
    private String cron4jExpression = "* * * * *";
    private String invalidExpression = "* * * * * * *";

    private CronValidator cronValidator;

    @Before
    public void setUp() throws Exception {
        testCronDefinition = CronDefinitionRegistry.instance().retrieve(CronType.CRON4J);
        cronValidator = new CronValidator(testCronDefinition);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullCronDefinition() throws Exception {
        new CronValidator(null);
    }

    @Test
    public void testIsValidWithValidExpression() throws Exception {
        assertTrue(cronValidator.isValid(cron4jExpression));
    }

    @Test
    public void testIsValidWithInvalidExpression() throws Exception {
        assertFalse(cronValidator.isValid(invalidExpression));
    }

    @Test
    public void testIsValidWithNullExpression() throws Exception {
        assertFalse(cronValidator.isValid(null));
    }

    @Test
    public void testValidateWithValidExpression() throws Exception {
        assertEquals(cron4jExpression, cronValidator.validate(cron4jExpression));
    }

    @Test(expected = RuntimeException.class)
    public void testValidateWithInvalidExpression() throws Exception {
        cronValidator.validate(invalidExpression);
    }

    @Test(expected = RuntimeException.class)
    public void testValidateWithNullExpression() throws Exception {
        cronValidator.validate(null);
    }
}