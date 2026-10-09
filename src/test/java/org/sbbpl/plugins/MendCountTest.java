package org.sbbpl.plugins;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class MendCountTest {
    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, Integer.MAX_VALUE})
    void acceptsOnlyDocumentedStates(int value) {
        assertEquals(value, MendCount.validate(value));
    }

    @Test void rejectsUndefinedNegativeState() {
        assertThrows(IllegalArgumentException.class, () -> MendCount.validate(-4));
    }

    @Test void subtractionNeverGrantsUnlimitedMode() {
        assertEquals(0, MendCount.add(5, -6));
        assertEquals(0, MendCount.add(5, Integer.MIN_VALUE));
    }

    @Test void detectsOverflowWithoutWrappingIntoSpecialMode() {
        assertThrows(IllegalArgumentException.class, () -> MendCount.add(Integer.MAX_VALUE, 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -2, -3})
    void specialStatesRequireExplicitSet(int mode) {
        assertThrows(IllegalArgumentException.class, () -> MendCount.add(mode, 100));
    }

    @ParameterizedTest
    @CsvSource({"无限,-1", "UNLIMITED,-1", "无限不减速,-2", "Unlimited-Fast,-2", "禁用,-3", "DISABLED,-3",
            "-1,-1", "-2,-2", "-3,-3", "0,0", "1000,1000", "2147483647,2147483647"})
    void acceptsNamedModesAndExistingNumericCommands(String input, int expected) {
        assertEquals(expected, MendCount.parseSetting(" " + input + " "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "unknown", "无限减速", "-4", "2147483648", "-2147483649", "1.5", "减少 1 次"})
    void rejectsInvalidSettingsWithoutGrantingQuota(String input) {
        assertThrows(IllegalArgumentException.class, () -> MendCount.parseSetting(input));
    }

    @Test void negativeDeltaIsPresentedAsDeduction() {
        assertEquals("减少 1 次", MendCount.formatDelta(-1));
        assertEquals("减少 2147483648 次", MendCount.formatDelta(Integer.MIN_VALUE));
        assertEquals("增加 5 次", MendCount.formatDelta(5));
        assertEquals("不改变次数", MendCount.formatDelta(0));
    }
}
