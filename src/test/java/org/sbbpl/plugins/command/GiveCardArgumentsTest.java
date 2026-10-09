package org.sbbpl.plugins.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class GiveCardArgumentsTest {
    @ParameterizedTest
    @CsvSource({"set,2,无限,-1", "SET,3,100,100", "add,4,-1,-1", "ADD,5,100,100"})
    void newAndLegacySyntaxDeliverTheSameCard(String mode, int quantity, String value, int frequency) {
        var expected = new GiveCardArguments("Steve", quantity, frequency, mode.equalsIgnoreCase("set"));
        assertEquals(expected, GiveCardArguments.parse(new String[]{"givecard", mode, "Steve", "" + quantity, value}));
        assertEquals(expected, GiveCardArguments.parse(new String[]{"givecard", "Steve", "" + quantity, value, mode}));
    }

    @ParameterizedTest
    @ValueSource(strings = {"set", "add"})
    void playerNamesMatchingModesAreNotReinterpreted(String player) {
        var expected = new GiveCardArguments(player, 2, -1, false);
        assertEquals(expected, GiveCardArguments.parse(new String[]{"givecard", "add", player, "2", "-1"}));
        assertEquals(expected, GiveCardArguments.parse(new String[]{"givecard", player, "2", "-1", "add"}));
    }

    @Test void malformedCommandsFailBeforePlayerLookupOrDelivery() {
        String[][] invalid = {
                {}, {"givecard", "set", "Steve", "1"},
                {"givecard", "unknown", "Steve", "1", "100"},
                {"givecard", "set", "Steve", "many", "无限"},
                {"givecard", "add", "Steve", "1", "无限"},
                {"givecard", "Steve", "1", "无限", "add"},
                {"givecard", "set", "Steve", "1", "unknown"}
        };
        for (String[] args : invalid) assertThrows(IllegalArgumentException.class, () -> GiveCardArguments.parse(args));
    }
}
