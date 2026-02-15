package me.timwastaken.minipvp.ui;

import me.timwastaken.minipvp.common.OptionalOnlinePlayer;
import me.timwastaken.minipvp.common.ReflectionUtils;
import me.timwastaken.minipvp.game.MiniPvPGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Notifications {
    private static final String PREFIX = String.format("[%sMiniPvP%s] ", ChatColor.LIGHT_PURPLE, ChatColor.RESET);
    private static final String LIFE_ICON = String.format("%s❤", ChatColor.RED);
    private static final String LIFE_LOST_ICON = String.format("%s✖", ChatColor.DARK_GRAY);

    public static void announceGameStart(Collection<OptionalOnlinePlayer> receivers, long protectionSeconds) {
        for (OptionalOnlinePlayer receiver : receivers) {
            receiver.run(player -> {
                ReflectionUtils.sendTitle(player,
                        String.format("%s%sGo!", ChatColor.GREEN, ChatColor.BOLD),
                        String.format("%s%d seconds of protection", ChatColor.GRAY, protectionSeconds),
                        0, 80, 20
                );
                player.sendMessage(String.format(
                        "%s%sThe game started!",
                        PREFIX,
                        ChatColor.GREEN
                ));
                player.sendMessage(String.format(
                        "%s%sProtection time lasts %s%d seconds.",
                        PREFIX,
                        ChatColor.GRAY,
                        ChatColor.YELLOW,
                        protectionSeconds
                ));
            });
        }
    }

    public static void showCountdownTo(Collection<OptionalOnlinePlayer> receivers, long remainingSeconds) {
        for (OptionalOnlinePlayer receiver : receivers) {
            receiver.run(player -> {
                ReflectionUtils.sendTitle(player,
                        String.format("%s%s%d", ChatColor.RED, ChatColor.BOLD, remainingSeconds),
                        null,
                        0, 15, 6
                );
            });
        }
    }

    public static void showEliminationTo(Player p) {
        ReflectionUtils.sendTitle(p,
                String.format("%s%sEliminated", ChatColor.DARK_RED, ChatColor.BOLD),
                String.format("%sYou are out of the game", ChatColor.GRAY),
                10, 80, 10
        );
    }

    public static void announcePlayerElimination(Collection<OptionalOnlinePlayer> receivers, Player p) {
        for (OptionalOnlinePlayer receiver : receivers) {
            receiver.run(player -> player.sendMessage(String.format(
                    "%s%s%s %shas been %seliminated.",
                    PREFIX,
                    ChatColor.RED,
                    p.getName(),
                    ChatColor.GRAY,
                    ChatColor.DARK_RED
            )));
        }
    }

    public static void announceGameWinnerTo(Set<OptionalOnlinePlayer> receivers, String winnerName) {
        for (OptionalOnlinePlayer receiver : receivers) {
            receiver.run(player -> ReflectionUtils.sendTitle(player,
                    String.format("%s%s%s", ChatColor.GOLD, ChatColor.BOLD, winnerName),
                    String.format("%swon the game", ChatColor.GRAY),
                    10, 80, 10
            ));
        }
    }

    public static void errorGameDoesNotExist(CommandSender receiver, String name) {
        receiver.sendMessage(String.format(
                "%s%sGame %s%s %sdoes not exist.",
                PREFIX,
                ChatColor.RED,
                ChatColor.AQUA,
                name,
                ChatColor.RED
        ));
    }

    public static void errorChat(CommandSender receiver, String description) {
        receiver.sendMessage(String.format(
                "%s%s%s",
                PREFIX,
                ChatColor.RED,
                description
        ));
    }

    public static void announceGameJoined(Player receiver, OfflinePlayer participant, String name) {
        receiver.sendMessage(String.format(
                "%s%s%s %ssuccessfully joined game %s%s",
                PREFIX,
                ChatColor.GREEN,
                participant.getName(),
                ChatColor.GRAY,
                ChatColor.AQUA,
                name
        ));
    }

    public static void announceGameLeft(Player receiver, OfflinePlayer left, String name) {
        receiver.sendMessage(String.format(
                "%s%s%s %sleft game %s%s",
                PREFIX,
                ChatColor.GREEN,
                left.getName(),
                ChatColor.GRAY,
                ChatColor.AQUA,
                name
        ));
    }

    public static void announceAllGameJoined(String name) {
        Bukkit.broadcastMessage(String.format(
                "%s%sAll players joined game %s%s",
                PREFIX,
                ChatColor.GRAY,
                ChatColor.AQUA,
                name
        ));
    }

    public static void errorGameIsRunning(CommandSender receiver, String name) {
        receiver.sendMessage(String.format(
                "%s%sGame %s%s %sis running.",
                PREFIX,
                ChatColor.RED,
                ChatColor.AQUA,
                name,
                ChatColor.RED
        ));
    }

    public static void sendGamesOverview(CommandSender receiver, Map<String, MiniPvPGame> games) {
        if (games.isEmpty()) {
            receiver.sendMessage(String.format("%s%sNo games registered.", PREFIX, ChatColor.RED));
            return;
        }
        receiver.sendMessage(String.format("%s%sRegistered games:", PREFIX, ChatColor.GRAY));
        for (Map.Entry<String, MiniPvPGame> gameByName : games.entrySet()) {
            String name = gameByName.getKey();
            MiniPvPGame game = gameByName.getValue();
            receiver.sendMessage(String.format(
                    " %s- %s%s %s(%s)",
                    ChatColor.DARK_GRAY,
                    ChatColor.AQUA,
                    name,
                    ChatColor.GRAY,
                    game.hasStarted() ? "Running" : "Not started"
            ));
        }
    }

    public static void sendGameCreatedSuccesfully(CommandSender receiver, String name) {
        receiver.sendMessage(String.format(
                "%s%sSuccessfully created game %s%s",
                PREFIX,
                ChatColor.GRAY,
                ChatColor.AQUA,
                name
        ));
    }

    public static void announceProtectionPeriodEndIn(Collection<OptionalOnlinePlayer> receivers, long seconds) {
        for (OptionalOnlinePlayer receiver : receivers) {
            receiver.run(player -> player.sendMessage(String.format(
                    "%s%sProtection period ends in %s%s",
                    PREFIX,
                    ChatColor.GRAY,
                    ChatColor.YELLOW,
                    formatSecondsHuman(seconds, true)
            )));
        }
    }

    public static void announceProtectionPeriodEnd(Collection<OptionalOnlinePlayer> receivers) {
        for (OptionalOnlinePlayer receiver : receivers) {
            receiver.run(player -> {
                ReflectionUtils.sendTitle(player,
                        String.format("%s%sProtection Over", ChatColor.YELLOW, ChatColor.BOLD),
                        String.format("%sFriendly Fire is off", ChatColor.GRAY),
                        10, 30, 10
                );
                player.sendMessage(String.format(
                        "%s%sProtection period is over! PvP is %senabled",
                        PREFIX,
                        ChatColor.GRAY,
                        ChatColor.YELLOW
                ));
            });
        }
    }

    private static String formatSecondsHuman(long totalSeconds, boolean includeSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        boolean printRest = false;
        if (h > 0) {
            sb.append(h).append("h ");
            printRest = true;
        }
        if (m > 0 || !includeSeconds || printRest) {
            sb.append(m).append("m ");
        }
        if (includeSeconds) sb.append(s).append("s");
        return sb.toString();
    }

    public static String getScoreboardTitle() {
        return String.format("%s%sMini%sPvP", ChatColor.BOLD, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE);
    }

    public static String getScoreboardHeader(String header) {
        return String.format("%s%s:", ChatColor.GRAY, header);
    }

    public static String getScoreboardTimerFormatted(long totalSeconds, ChatColor color) {
        long minPart = totalSeconds / 60;
        long secPart = totalSeconds % 60;
        return String.format("%s%02d:%02d", color, minPart, secPart);
    }

    public static String getProtectionTimeOverLine() {
        return String.format("%s%sOver", ChatColor.DARK_RED, ChatColor.BOLD);
    }

    public static String getLivesLine(String name, int lives, int livesLost) {
        return String.format(
                "%s%s %s%s%s",
                LIFE_ICON.repeat(lives),
                LIFE_LOST_ICON.repeat(livesLost),
                lives == 0 ? ChatColor.GRAY : ChatColor.GREEN,
                lives == 0 ? ChatColor.ITALIC : ChatColor.BOLD,
                name
        );
    }
}
