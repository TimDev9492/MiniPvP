package me.timwastaken.minipvp;

import me.timwastaken.minipvp.common.OptionalOnlinePlayer;
import me.timwastaken.minipvp.exceptions.CreateGameException;
import me.timwastaken.minipvp.game.MiniPvPGame;
import me.timwastaken.minipvp.game.config.MockGameConfiguration;
import me.timwastaken.minipvp.game.factory.GameFactory;
import me.timwastaken.minipvp.game.factory.WorldGameFactory;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;

import java.util.*;

public class PluginResourceManager {
    private final JavaPlugin plugin;
    private final List<Integer> taskIds;
    private final List<MultiverseWorld> trackedTempWorlds;
    private final MultiverseCoreApi coreApi;
    private final MultiverseNetherPortals netherPortalsApi;
    private final GameFactory factory;
    private final Map<String, MiniPvPGame> trackedGames;

    public PluginResourceManager(
            JavaPlugin plugin,
            MultiverseCoreApi coreApi,
            MultiverseNetherPortals netherPortalsApi,
            GameFactory factory
    ) {
        this.plugin = plugin;
        this.coreApi = coreApi;
        this.netherPortalsApi = netherPortalsApi;
        this.taskIds = new ArrayList<>();
        this.trackedTempWorlds = new ArrayList<>();
        this.factory = factory;
        this.trackedGames = new HashMap<>();
    }

    public MultiverseCoreApi getMultiverseCoreApi() {
        return coreApi;
    }

    public MultiverseNetherPortals getMultiverseNetherPortalsApi() {
        return netherPortalsApi;
    }

    public void registerEventListener(Listener listener) {
        this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
    }

    public void registerEventListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            this.registerEventListener(listener);
        }
    }

    public void unregisterEvents() {
        HandlerList.unregisterAll(this.plugin);
    }

    public void registerTask(int taskId) {
        this.taskIds.add(taskId);
    }

    public void runTaskLater(BukkitRunnable runnable, long delay) {
        this.registerTask(runnable.runTaskLater(this.plugin, delay).getTaskId());
    }

    public void runTaskTimerAsynchronously(BukkitRunnable runnable, long delay, long period) {
        this.registerTask(runnable.runTaskTimerAsynchronously(this.plugin, delay, period).getTaskId());
    }

    public void runTaskTimer(BukkitRunnable runnable, long delay, long period) {
        this.registerTask(runnable.runTaskTimer(this.plugin, delay, period).getTaskId());
    }

    public void unregisterTasks() {
        for (int taskId : this.taskIds) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public void trackTempWorld(LoadedMultiverseWorld world) {
        this.trackedTempWorlds.add(world);
    }

    public void deleteTempWorlds() {
        for (MultiverseWorld trackedTempWorld : trackedTempWorlds) {
            trackedTempWorld.asLoadedWorld().peek(loadedWorld -> {
                loadedWorld.getPlayers().peek(players -> {
                    coreApi.getWorldManager().getDefaultWorld().peek(defaultWorld -> {
                        coreApi.getSafetyTeleporter().to(defaultWorld.getSpawnLocation())
                                .checkSafety(false)
                                .teleport(players);
                    });
                });
            });
            coreApi.getWorldManager().deleteWorld(DeleteWorldOptions.world(trackedTempWorld))
                    .onFailure(failure -> {
                        plugin.getLogger().severe(failure.getFailureMessage().formatted());
                    });
        }
    }

    public void registerCommand(String commandName, CommandExecutor executor) {
        Optional.ofNullable(this.plugin.getCommand(commandName)).ifPresent(command -> command.setExecutor(executor));
    }

    public String createGame(Optional<String> name) throws CreateGameException {
        String finalGameName = name.orElse(fallbackGameName());
        MiniPvPGame game = trackedGames.getOrDefault(
                finalGameName,
                factory.createGame(this, finalGameName, new MockGameConfiguration())
        );
        trackedGames.put(finalGameName, game);
        return finalGameName;
    }

    public MiniPvPGame getGame(String identifier) {
        return trackedGames.get(identifier);
    }

    public void removeFromAllGames(OptionalOnlinePlayer p) {
        for (MiniPvPGame game : trackedGames.values()) {
            if (!game.isParticipant(p)) continue;
            game.removePlayer(p);
        }
    }

    private String fallbackGameName() {
        String identifier = "game";
        int num = 0;
        String uniqueName;
        do {
            uniqueName = String.format("%s%04d", identifier, num++);
        } while (Bukkit.getWorld(uniqueName) != null);
        return uniqueName;
    }

    public Map<String, MiniPvPGame> getGames() {
        return Collections.unmodifiableMap(trackedGames);
    }
}
