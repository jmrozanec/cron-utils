package com.cronutils.utils.descriptor;

import java.util.Locale;

import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

/**
 *  Issue 334 - Getting exception {@link IllegalArgumentException} "Both, a day-of-week AND a day-of-month parameter, are not supported."
 *  when trying to get description for valid cron expression with {@link CronDefinition} of {@link CronType#SPRING} type.
 *  
 **/ 
//@RunWith(Parameterized.class)
public class Issue343Test {
	
	/**
	 * Cron expressions from spring docs <a href="https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html">Class CronSequenceGenerator</a>
	 **/ 
	@Parameters
	public static Object[] expressions() {
		return new Object[] {
				new Expr("0 0 * * * *", "every hour"),
				new Expr("*/10 * * * * *", "every 10 seconds"),
				new Expr("0 0 8-10 * * *", "every hour between 8 and 10"),
				new Expr("0 0 6,19 * * *", "at 6 and 19 hours"),
				new Expr("0 0/30 8-10 * * *", "every 30 minutes every hour between 8 and 10"),
				new Expr("0 0 9-17 * * MON-FRI", "every hour between 9 and 17 every day between Monday and Friday")
		};
	}
	
	private Expr expressionToTest;
	
	public Issue343Test(Expr expressionToTest) {
		this.expressionToTest = expressionToTest;
	}
	
	//@Test
	public void test() {
		CronParser pareser = new CronParser( CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING) );
		
		String actualDescription = CronDescriptor.instance(Locale.ENGLISH)
				.describe(pareser.parse(expressionToTest.getExpression()));
		
		Assert.assertThat(actualDescription, IsEqual.equalTo(expressionToTest.getExpectedDescription()));
	}
	
	//@Test
	public void workaround() {
		CronParser pareser = new CronParser( workingSpringCronDefinition() );
		
		String actualDescription = CronDescriptor.instance(Locale.ENGLISH)
				.describe(pareser.parse(expressionToTest.getExpression()));
		
		Assert.assertThat(actualDescription, IsEqual.equalTo(expressionToTest.getExpectedDescription()));
	}
	
	private static CronDefinition workingSpringCronDefinition() {
        return CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().supportsL().supportsW().supportsLW().supportsQuestionMark().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(2).supportsHash().supportsL().supportsQuestionMark().and()
//                .withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth())
                .instance();
    }
	
	
	private static class Expr {
		private final String expression;
		private final String expectedDescription;
		
		public Expr(String expression, String expectedDescription) {
			super();
			this.expression = expression;
			this.expectedDescription = expectedDescription;
		}
		public String getExpression() {
			return expression;
		}
		public String getExpectedDescription() {
			return expectedDescription;
		}
	}
}
