package me.timwastaken.minipvp.game;

import fr.mrmicky.fastboard.FastBoard;
import me.timwastaken.minipvp.MiniPvP;
import me.timwastaken.minipvp.ui.Notifications;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The display should include:
 * - Title (MinecraftVaro)
 * - Active period duration
// * - World size
 * - Teams alive
 * - Next doxing date
 */
public class ScoreboardDisplay {
    private Map<UUID, FastBoard> playerBoards = new HashMap<>();

    public ScoreboardDisplay() {}

    public void deleteBoard(Player p) {
        FastBoard board = playerBoards.remove(p.getUniqueId());
        if (board != null) board.delete();
    }

    public void updateBoard(
            Player p,
            long gameTimeElapsed,
            long protectionTimeRemaining,
            Map<String, Integer> playerLivesSorted,
            int startLives
    ) {
        if (!playerBoards.containsKey(p.getUniqueId())) {
            FastBoard board = new FastBoard(p);
            board.updateTitle(Notifications.getScoreboardTitle());
            playerBoards.put(p.getUniqueId(), board);
        }

        FastBoard playerBoard = playerBoards.get(p.getUniqueId());
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add(Notifications.getScoreboardHeader("Player Lives"));
        for (Map.Entry<String, Integer> nameWithLives : playerLivesSorted.entrySet()) {
            String name = nameWithLives.getKey();
            int lives = nameWithLives.getValue();
            lines.add(Notifications.getLivesLine(name, lives, startLives - lives));
        }
        lines.add("");
        lines.add(Notifications.getScoreboardHeader("Protection Time"));
        lines.add(protectionTimeRemaining > 0 ?
                Notifications.getScoreboardTimerFormatted(protectionTimeRemaining, ChatColor.RED) :
                Notifications.getProtectionTimeOverLine()
        );
        lines.add("");
        lines.add(Notifications.getScoreboardHeader("Elapsed Time"));
        lines.add(Notifications.getScoreboardTimerFormatted(gameTimeElapsed, ChatColor.GREEN));

        playerBoard.updateLines(lines);
    }
}
