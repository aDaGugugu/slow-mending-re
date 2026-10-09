package org.sbbpl.plugins;

import java.util.Locale;

/** Values below zero are modes, never the result of subtraction. */
public final class MendCount {
    private static final String UNLIMITED = "无限次数（遵循减速设置）";
    private static final String UNRESTRICTED = "无限次数（不受减速限制）";
    private static final String DISABLED = "禁止经验修补";
    private static final String EXHAUSTED = "已耗尽（无法修补）";

    private MendCount() {}

    public static int validate(int value) {
        if (value < -3) throw new IllegalArgumentException("次数必须为非负整数；模式可用“无限”“无限不减速”或“禁用”。");
        return value;
    }

    /** Human-readable UI only; persistent item data keeps the original integers. */
    public static String format(int value) {
        return switch (validate(value)) {
            case -1 -> UNLIMITED;
            case -2 -> UNRESTRICTED;
            case -3 -> DISABLED;
            case 0 -> EXHAUSTED;
            default -> Integer.toString(value);
        };
    }

    /** Accept old numeric commands, short mode names and our displayed lore. */
    public static int parseSetting(String input) {
        if (input == null) throw new IllegalArgumentException("请输入次数或修补模式。");
        String value = input.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "无限", "unlimited", UNLIMITED -> -1;
            case "无限不减速", "unlimited-fast", UNRESTRICTED -> -2;
            case "禁用", "disabled", DISABLED -> -3;
            case EXHAUSTED -> 0;
            default -> {
                try {
                    yield validate(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("请输入非负整数，或“无限”“无限不减速”“禁用”。", e);
                }
            }
        };
    }

    /** A negative add-card value is a deduction, never a special mode. */
    public static String formatDelta(int delta) {
        if (delta == 0) return "不改变次数";
        return (delta > 0 ? "增加 " : "减少 ") + Math.abs((long) delta) + " 次";
    }

    public static int add(int current, int delta) {
        validate(current);
        if (current < 0) throw new IllegalArgumentException("当前为“" + format(current) + "”，请使用 set 模式修改。");
        long result = (long) current + delta;
        if (result > Integer.MAX_VALUE) throw new IllegalArgumentException("次数超出整数上限。");
        // A deduction must not accidentally grant unlimited mending.
        return (int) Math.max(0L, result);
    }
}
