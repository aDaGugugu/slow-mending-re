package org.sbbpl.plugins;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LegacyAndConfigTest {
    @Test void migratesOldPrefixWithUnrelatedLore() throws Exception {
        assertEquals(7, MendingItem.readLegacyCount(List.of("other lore", "old:7"), "new:", List.of("old:")));
    }

    @Test void matchesLongestPrefixFirst() throws Exception {
        assertEquals(9, MendingItem.readLegacyCount(List.of("mends:old:9"), "mends:", List.of("mends:old:")));
    }

    @Test void missingAndMalformedDataAreDistinct() {
        assertThrows(NoSuchFieldException.class, () -> MendingItem.readLegacyCount(List.of("other"), "mends:", List.of()));
        assertThrows(IllegalArgumentException.class, () -> MendingItem.readLegacyCount(List.of("mends:invalid"), "mends:", List.of()));
        assertThrows(IllegalArgumentException.class, () -> MendingItem.readLegacyCount(List.of("mends:-4"), "mends:", List.of()));
    }

    @Test void toleratesNullOldNameListAndWhitespace() throws Exception {
        assertEquals(12, MendingItem.readLegacyCount(List.of("mends: 12 "), "mends:", null));
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, Integer.MAX_VALUE})
    void readsBothNumericAndReadableLoreWithoutPersistentData(int value) throws Exception {
        assertEquals(value, MendingItem.readLegacyCount(List.of("old:" + value), "new:", List.of("old:")));
        assertEquals(value, MendingItem.readLegacyCount(List.of("old:" + MendCount.format(value)), "new:", List.of("old:")));
    }

    @Test void doesNotCoerceMalformedConfigurationIntoZeroOrFalse() {
        var config = new YamlConfiguration();
        config.set("factor", "five");
        config.set("enabled", "invalid");
        assertThrows(IllegalArgumentException.class, () -> loadPL.integer(config, "factor", 5));
        assertThrows(IllegalArgumentException.class, () -> loadPL.bool(config, "enabled", true));
        assertEquals(5, loadPL.integer(config, "missing", 5));
        assertTrue(loadPL.bool(config, "missing", true));
    }
}
