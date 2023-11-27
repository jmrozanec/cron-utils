package com.cronutils.model.time;

import static com.cronutils.model.field.CronFieldName.DAY_OF_MONTH;
import static com.cronutils.model.field.CronFieldName.DAY_OF_WEEK;
import static com.cronutils.model.field.CronFieldName.DAY_OF_YEAR;
import static com.cronutils.model.field.value.SpecialChar.QUESTION_MARK;
import static com.cronutils.model.time.generator.FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance;
import static com.cronutils.model.time.generator.FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance;
import static com.cronutils.model.time.generator.FieldValueGeneratorFactory.createDayOfYearValueGeneratorInstance;
import static com.cronutils.utils.Predicates.not;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.definition.DayOfWeekFieldDefinition;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.QuestionMark;

/*
 * Generate days for execution time
 */
class DaysGenerator {

	private final CronField daysOfWeekCronField;
    private final CronField daysOfMonthCronField;
    private final CronField daysOfYearCronField;
    private final CronDefinition cronDefinition;
   
    DaysGenerator(CronField daysOfWeekCronField, CronField daysOfMonthCronField, CronField daysOfYearCronField,
			CronDefinition cronDefinition) {
		super();
		this.daysOfWeekCronField = daysOfWeekCronField;
		this.daysOfMonthCronField = daysOfMonthCronField;
		this.daysOfYearCronField = daysOfYearCronField;
		this.cronDefinition = cronDefinition;
	}

	Optional<TimeNode> generateDays(final CronDefinition cronDefinition, final ZonedDateTime date) {
        if (isGenerateDaysAsDoY(cronDefinition)) {
            return generateDayCandidatesUsingDoY(date);
        }
        //If DoW is not supported in custom definition, we just return an empty list.
        if (cronDefinition.getFieldDefinition(DAY_OF_WEEK) != null && cronDefinition.getFieldDefinition(DAY_OF_MONTH) != null) {
            return generateDaysDoWAndDoMSupported(cronDefinition, date);
        }
        if (cronDefinition.getFieldDefinition(DAY_OF_WEEK) == null) {
            return generateDayCandidatesUsingDoM(date);
        }
        return Optional
                .of(generateDayCandidatesUsingDoW(date, ((DayOfWeekFieldDefinition) cronDefinition.getFieldDefinition(DAY_OF_WEEK)).getMondayDoWValue()));
    }
    
    private boolean isGenerateDaysAsDoY(final CronDefinition cronDefinition) {
        if (!cronDefinition.containsFieldDefinition(DAY_OF_YEAR)) {
            return false;
        }

        if (!cronDefinition.getFieldDefinition(DAY_OF_YEAR).getConstraints().getSpecialChars().contains(QUESTION_MARK)) {
            return true;
        }

        return !(daysOfYearCronField.getExpression() instanceof QuestionMark);
    }

    private Optional<TimeNode> generateDayCandidatesUsingDoY(final ZonedDateTime reference) {
        final int year = reference.getYear();
        final int month = reference.getMonthValue();
        final LocalDate date = LocalDate.of(year, 1, 1);
        final int lengthOfYear = date.lengthOfYear();

        final List<Integer> candidates = createDayOfYearValueGeneratorInstance(daysOfYearCronField, year).generateCandidates(1, lengthOfYear);

        final int low = LocalDate.of(year, month, 1).getDayOfYear();
        final int high = month == 12
                ? LocalDate.of(year, 12, 31).getDayOfYear() + 1
                : LocalDate.of(year, month + 1, 1).getDayOfYear();

        final List<Integer> collectedCandidates = candidates.stream().filter(dayOfYear -> dayOfYear >= low && dayOfYear < high)
                .map(dayOfYear -> LocalDate.ofYearDay(reference.getYear(), dayOfYear).getDayOfMonth())
                .collect(Collectors.toList());

        return Optional.of(collectedCandidates).filter(not(List::isEmpty)).map(TimeNode::new);
    }

    private Optional<TimeNode> generateDaysDoWAndDoMSupported(final CronDefinition cronDefinition, final ZonedDateTime date) {
        final boolean questionMarkSupported = cronDefinition.getFieldDefinition(DAY_OF_WEEK).getConstraints().getSpecialChars().contains(QUESTION_MARK);
        if (questionMarkSupported) {
            final List<Integer> candidates = generateDayCandidatesQuestionMarkSupportedUsingDoWAndDoM(
                    date.getYear(),
                    date.getMonthValue(),
                    ((DayOfWeekFieldDefinition) cronDefinition.getFieldDefinition(DAY_OF_WEEK)).getMondayDoWValue()
            );
            return Optional.of(candidates).filter(not(List::isEmpty)).map(TimeNode::new);
        } else {
            final List<Integer> candidates = generateDayCandidatesQuestionMarkNotSupportedUsingDoWAndDoM(
                    date.getYear(), date.getMonthValue(),
                    ((DayOfWeekFieldDefinition)
                            cronDefinition.getFieldDefinition(DAY_OF_WEEK)
                    ).getMondayDoWValue()
            );
            return Optional.of(candidates).filter(not(List::isEmpty)).map(TimeNode::new);
        }
    }
    
    private List<Integer> generateDayCandidatesQuestionMarkNotSupportedUsingDoWAndDoM(final int year, final int month, final WeekDay mondayDoWValue) {
        final LocalDate date = LocalDate.of(year, month, 1);
        final int lengthOfMonth = date.lengthOfMonth();
        if (daysOfMonthCronField.getExpression() instanceof Always && daysOfWeekCronField.getExpression() instanceof Always) {
            return createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth)
                    .stream().distinct().sorted()
                    .collect(Collectors.toList());
        } else if (daysOfMonthCronField.getExpression() instanceof Always) {
            return createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue)
                    .generateCandidates(1, lengthOfMonth)
                    .stream().distinct().sorted()
                    .collect(Collectors.toList());
        } else if (daysOfWeekCronField.getExpression() instanceof Always) {
            return createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth)
                    .stream().distinct().sorted()
                    .collect(Collectors.toList());
        } else {
            final List<Integer> dayOfWeekCandidates = createDayOfWeekValueGeneratorInstance(daysOfWeekCronField,
                    year, month, mondayDoWValue).generateCandidates(1, lengthOfMonth);
            final List<Integer> dayOfMonthCandidates = createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth);
            if (cronDefinition.isMatchDayOfWeekAndDayOfMonth()) {
                final Set<Integer> intersection = new HashSet<>(dayOfWeekCandidates);
                return dayOfMonthCandidates
                        .stream().filter(intersection::contains)
                        .distinct().sorted()
                        .collect(Collectors.toList());
            } else {
                return Stream.concat(dayOfWeekCandidates.stream(), dayOfMonthCandidates.stream())
                        .distinct().sorted()
                        .collect(Collectors.toList());
            }
        }
    }

    private List<Integer> generateDayCandidatesQuestionMarkSupportedUsingDoWAndDoM(final int year, final int month, final WeekDay mondayDoWValue) {
        final LocalDate date = LocalDate.of(year, month, 1);
        final int lengthOfMonth = date.lengthOfMonth();
        if (daysOfMonthCronField.getExpression() instanceof Always && daysOfWeekCronField.getExpression() instanceof Always) {
            return createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth)
                    .stream().distinct().sorted()
                    .collect(Collectors.toList());
        } else if (daysOfMonthCronField.getExpression() instanceof QuestionMark) {
            return createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue)
                    .generateCandidates(1, lengthOfMonth)
                    .stream().distinct().sorted()
                    .collect(Collectors.toList());
        } else if (daysOfWeekCronField.getExpression() instanceof QuestionMark) {
            return createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth)
                    .stream().distinct().sorted()
                    .collect(Collectors.toList());
        } else {
            Set<Integer> candidates = new HashSet<>(createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month).generateCandidates(1, lengthOfMonth));
            Set<Integer> daysOfWeek = new HashSet<>(createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue).generateCandidates(1, lengthOfMonth));
            // Only the intersection of valid days from the days of week and valid days of month should be returned.
            candidates.retainAll(daysOfWeek);
            return candidates.stream().sorted().collect(Collectors.toList());
        }
    }

    private Optional<TimeNode> generateDayCandidatesUsingDoM(final ZonedDateTime reference) {
        final LocalDate date = LocalDate.of(reference.getYear(), reference.getMonthValue(), 1);
        final int lengthOfMonth = date.lengthOfMonth();
        final List<Integer> candidates = createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, reference.getYear(), reference.getMonthValue())
                .generateCandidates(1, lengthOfMonth)
                .stream().distinct().sorted()
                .collect(Collectors.toList());
        return candidates.isEmpty() ? Optional.empty() : Optional.of(new TimeNode(candidates));
    }

    private TimeNode generateDayCandidatesUsingDoW(final ZonedDateTime reference, final WeekDay mondayDoWValue) {
        final LocalDate date = LocalDate.of(reference.getYear(), reference.getMonthValue(), 1);
        final int lengthOfMonth = date.lengthOfMonth();
        final List<Integer> candidates = createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, reference.getYear(), reference.getMonthValue(), mondayDoWValue)
                .generateCandidates(1, lengthOfMonth)
                .stream().distinct().sorted()
                .collect(Collectors.toList());
        return new TimeNode(candidates);
    }
}
