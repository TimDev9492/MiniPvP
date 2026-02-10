package me.timwastaken.minipvp.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import me.timwastaken.minipvp.PluginResourceManager;
import me.timwastaken.minipvp.common.OptionalOnlinePlayer;
import me.timwastaken.minipvp.exceptions.CreateGameException;
import me.timwastaken.minipvp.exceptions.IllegalOperationException;
import me.timwastaken.minipvp.exceptions.MiniPvPException;
import me.timwastaken.minipvp.game.MiniPvPGame;
import me.timwastaken.minipvp.ui.Notifications;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

@Command(name = "game")
public class GameCommand {
    private PluginResourceManager resourceManager;

    public GameCommand(PluginResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Execute(name = "quickstart")
    public void quickStartGame(@Context CommandSender sender) {
        try {
            String quickGameName = resourceManager.createGame(Optional.of("quickplay"));
            Notifications.sendGameCreatedSuccesfully(sender, quickGameName);
            MiniPvPGame game = resourceManager.getGame(quickGameName);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                OptionalOnlinePlayer p = OptionalOnlinePlayer.of(onlinePlayer.getUniqueId());
                resourceManager.removeFromAllGames(p);
                game.registerPlayer(p);
            }
            Notifications.announceAllGameJoined(quickGameName);
            game.startCountdown(10);
        } catch (MiniPvPException ex) {
            Notifications.errorChat(sender, ex.getMessage());
        }
    }

    @Execute(name = "create")
    public void createGame(@Context CommandSender sender, @Arg Optional<String> name) {
        try {
            Notifications.sendGameCreatedSuccesfully(sender, resourceManager.createGame(name));
        } catch (CreateGameException ex) {
            Notifications.errorChat(sender, ex.getMessage());
        }
    }

    @Execute(name = "join")
    public void joinGame(@Context Player sender, @Arg String name) {
        addPlayerToGame(sender, sender, name);
    }

    @Execute(name = "leave")
    public void leaveGame(@Context Player sender, @Arg String name) {
        removePlayerFromGame(sender, sender, name);
    }

    @Execute(name = "add")
    public void addPlayerToGame(@Context Player sender, @Arg OfflinePlayer player, @Arg String name) {
        OptionalOnlinePlayer p = OptionalOnlinePlayer.of(player.getUniqueId());
        MiniPvPGame game = resourceManager.getGame(name);
        if (game == null) {
            Notifications.errorGameDoesNotExist(sender, name);
            return;
        } else if (game.hasStarted()) {
            Notifications.errorGameIsRunning(sender, name);
            return;
        }
        resourceManager.removeFromAllGames(p);
        game.registerPlayer(p);
        Notifications.announceGameJoined(sender, player, name);
    }

    @Execute(name = "remove")
    public void removePlayerFromGame(@Context Player sender, @Arg OfflinePlayer player, @Arg String name) {
        MiniPvPGame game = resourceManager.getGame(name);
        if (game == null) {
            Notifications.errorGameDoesNotExist(sender, name);
            return;
        }
        game.removePlayer(OptionalOnlinePlayer.of(sender));
        Notifications.announceGameLeft(sender, player, name);
    }

    @Execute(name = "add all")
    public void addAllPlayersToGame(@Context CommandSender sender, @Arg String name) {
        MiniPvPGame game = resourceManager.getGame(name);
        if (game == null) {
            Notifications.errorGameDoesNotExist(sender, name);
            return;
        } else if (game.hasStarted()) {
            Notifications.errorGameIsRunning(sender, name);
            return;
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            OptionalOnlinePlayer p = OptionalOnlinePlayer.of(onlinePlayer.getUniqueId());
            resourceManager.removeFromAllGames(p);
            game.registerPlayer(p);
        }
        Notifications.announceAllGameJoined(name);
    }

    @Execute(name = "start")
    public void startGame(@Context CommandSender sender, @Arg String name) {
        MiniPvPGame game = resourceManager.getGame(name);
        if (game == null) {
            Notifications.errorGameDoesNotExist(sender, name);
            return;
        } else if (game.hasStarted()) {
            Notifications.errorGameIsRunning(sender, name);
            return;
        }
        try {
            game.startCountdown(10);
        } catch (IllegalOperationException ex) {
            Notifications.errorChat(sender, ex.getMessage());
        }
    }

    @Execute(name = "list")
    public void listGames(@Context CommandSender sender) {
        Notifications.sendGamesOverview(sender, resourceManager.getGames());
    }
}
