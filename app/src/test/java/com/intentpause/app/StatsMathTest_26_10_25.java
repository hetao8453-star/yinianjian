package com.intentpause.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Calendar;

public class StatsMathTest_26_10_25 {
    @Test
    public void startOfDayIsMidnight() {
        Calendar c = Calendar.getInstance();
        c.set(2026, Calendar.OCTOBER, 25, 15, 42, 9);
        c.set(Calendar.MILLISECOND, 500);
        long start = StatsMath_26_10_25.startOfDay(c.getTimeInMillis());
        Calendar out = Calendar.getInstance();
        out.setTimeInMillis(start);
        assertEquals(0, out.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, out.get(Calendar.MINUTE));
        assertEquals(0, out.get(Calendar.SECOND));
        assertEquals(0, out.get(Calendar.MILLISECOND));
        assertEquals(25, out.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void addDaysCrossesMonth() {
        Calendar c = Calendar.getInstance();
        c.set(2026, Calendar.OCTOBER, 31, 8, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        long next = StatsMath_26_10_25.addDays(c.getTimeInMillis(), 1);
        Calendar out = Calendar.getInstance();
        out.setTimeInMillis(next);
        assertEquals(Calendar.NOVEMBER, out.get(Calendar.MONTH));
        assertEquals(1, out.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void durationFormatting() {
        assertEquals("0 分钟", StatsMath_26_10_25.duration(0));
        assertEquals("1 分钟", StatsMath_26_10_25.duration(1));
        assertEquals("12 分钟", StatsMath_26_10_25.duration(12 * 60_000L));
        assertEquals("1 小时", StatsMath_26_10_25.duration(60 * 60_000L));
        assertEquals("1 小时 5 分钟", StatsMath_26_10_25.duration(65 * 60_000L));
    }

    @Test
    public void savedEstimateUsesPlannedMinutes() {
        assertEquals(0, StatsMath_26_10_25.savedEstimateMs(0));
        assertEquals(600_000L, StatsMath_26_10_25.savedEstimateMs(10));
        assertEquals(1_200_000L, StatsMath_26_10_25.savedEstimateMs(20));
    }

    @Test
    public void percentIsBoundedAndRounded() {
        assertEquals(0, StatsMath_26_10_25.percent(0, 0));
        assertEquals(50, StatsMath_26_10_25.percent(1, 2));
        assertEquals(100, StatsMath_26_10_25.percent(3, 3));
    }

    @Test
    public void dateKeyMatchesLocalDate() {
        Calendar c = Calendar.getInstance();
        c.set(2026, Calendar.OCTOBER, 25, 23, 59, 0);
        assertEquals("2026-10-25", StatsMath_26_10_25.dateKey(c.getTimeInMillis()));
        assertTrue(StatsMath_26_10_25.weekday(c.getTimeInMillis()).startsWith("周"));
    }
}
