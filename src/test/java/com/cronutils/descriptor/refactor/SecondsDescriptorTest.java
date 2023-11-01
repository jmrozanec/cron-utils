package com.cronutils.descriptor.refactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;

class SecondsDescriptorTest {

	ResourceBundle resourceBundle;
	SecondsDescriptor secondsDescriptor;
	
	@BeforeEach
	public void setUp() {
		resourceBundle = ResourceBundle.getBundle("com.cronutils.CronUtilsI18N", Locale.UK);
		secondsDescriptor = new SecondsDescriptor(resourceBundle);
	}
	
	@Test
	void testDescribeForAndExpression() {
		String expectedDesc = "at 7  and 3";
		And and = new And();
		and.and(new On(new IntegerFieldValue(7)));
		and.and(new On(new IntegerFieldValue(3)));
		String description = secondsDescriptor.describe(and);
		assertNotNull(description);
		assertEquals(expectedDesc, description);
	}

	@Test
	void testVisitForEvery() {
		SecondsDescriptor mockedDescriptor = mock(SecondsDescriptor.class);
		Every every = new Every(new On(new IntegerFieldValue(7)), new IntegerFieldValue(2));
		mockedDescriptor.visit(every);
		verify(mockedDescriptor, times(1)).visit(every);
	}
}
