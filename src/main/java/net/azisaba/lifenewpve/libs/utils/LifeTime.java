package net.azisaba.lifenewpve.libs.utils;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("FieldCanBeLocal")
public class LifeTime {

    private final String PREFIX = "§a";
    private final String SECOND_LABEL = "秒";
    private final String MINUTE_LABEL = "分";
    private final String HOUR_LABEL = "時間";
    private final String DAY_LABEL = "日";
    private final long SECONDS_IN_A_DAY = 86400;
    private final long SECONDS_IN_AN_HOUR = 3600;
    private final long SECONDS_IN_A_MINUTE = 60;

    @NotNull
    public String getTimer(long time) {
        long days;
        long hours;
        long minutes;
        long seconds;

        days = extractTimeUnit(time, SECONDS_IN_A_DAY);
        time %= SECONDS_IN_A_DAY;

        hours = extractTimeUnit(time, SECONDS_IN_AN_HOUR);
        time %= SECONDS_IN_AN_HOUR;

        minutes = extractTimeUnit(time, SECONDS_IN_A_MINUTE);
        time %= SECONDS_IN_A_MINUTE;

        seconds = time;

        StringBuilder result = new StringBuilder(PREFIX);
        if (days != -1) result.append(days).append(DAY_LABEL);
        if (hours != -1) result.append(hours).append(HOUR_LABEL);
        if (minutes != -1) result.append(minutes).append(MINUTE_LABEL);
        if (seconds != 0) result.append(seconds).append(SECOND_LABEL);

        return result.toString();
    }

    private long extractTimeUnit(long time, long unitInSeconds) {
        return time >= unitInSeconds ? time / unitInSeconds : -1;
    }

    public long getTime(@NotNull String timeStr) {
        try {
            long time = Long.parseLong(timeStr.substring(0, timeStr.length() - 1));
            return switch (timeStr.charAt(timeStr.length() - 1)) {
                case 's' -> time;
                case 'm' -> time * 60L;
                case 'h' -> time * 3600L;
                case 'd' -> time * 86400L;
                case 'w' -> time * 604800L;
                case 'y' -> time * 31536000L;
                default -> throw new NumberFormatException();
            };
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
