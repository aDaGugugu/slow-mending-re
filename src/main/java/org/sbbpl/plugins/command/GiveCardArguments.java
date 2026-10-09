package org.sbbpl.plugins.command;

import org.sbbpl.plugins.MendCount;

/** Parse both command layouts before resolving a player or changing an inventory. */
record GiveCardArguments(String playerName, int quantity, int frequency, boolean setMode) {
    static final String USAGE = "/slmend givecard <set|add> <player> <数量> <次数或模式>";

    static boolean isMode(String value) {
        return value.equalsIgnoreCase("set") || value.equalsIgnoreCase("add");
    }

    static GiveCardArguments parse(String[] args) {
        if (args.length != 5) throw new IllegalArgumentException(USAGE);
        // A valid new value can never be the literal "set" or "add". Check the
        // old trailing mode first so players named set/add keep their old commands.
        boolean legacy = isMode(args[4]);
        String mode = legacy ? args[4] : args[1];
        if (!isMode(mode)) throw new IllegalArgumentException(USAGE + "；模式必须为 set 或 add。");
        boolean setMode = mode.equalsIgnoreCase("set");
        String player = args[legacy ? 1 : 2];
        int quantity = Integer.parseInt(args[legacy ? 2 : 3]);
        String value = args[legacy ? 3 : 4];
        int frequency = setMode ? MendCount.parseSetting(value) : Integer.parseInt(value);
        return new GiveCardArguments(player, quantity, frequency, setMode);
    }
}
