package me.timwastaken.minipvp.game.factory;

import me.timwastaken.minipvp.PluginResourceManager;
import me.timwastaken.minipvp.exceptions.CreateGameException;
import me.timwastaken.minipvp.game.MiniPvPGame;
import me.timwastaken.minipvp.game.config.GameConfiguration;
import org.bukkit.GameMode;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions;

public class WorldGameFactory implements GameFactory {
    public MiniPvPGame createGame(PluginResourceManager resourceManager, String worldName, GameConfiguration configuration) throws CreateGameException {
        LoadedMultiverseWorld overworld = resourceManager.getMultiverseCoreApi().getWorldManager()
                .createWorld(CreateWorldOptions.worldName(worldName)
                        .environment(World.Environment.NORMAL))
                .getOrThrow(reason -> new CreateGameException(
                        reason.getFailureMessage().formatted()
                ));
        LoadedMultiverseWorld nether = resourceManager.getMultiverseCoreApi().getWorldManager()
                .createWorld(CreateWorldOptions.worldName(String.format(
                        "%s%s%s", resourceManager.getMultiverseNetherPortalsApi().getNetherPrefix(),
                                worldName,
                                resourceManager.getMultiverseNetherPortalsApi().getNetherSuffix()
                        )).environment(World.Environment.NETHER))
                .getOrThrow(reason -> new CreateGameException(
                        reason.getFailureMessage().formatted()
                ));
        resourceManager.getMultiverseNetherPortalsApi().addWorldLink(overworld.getName(), nether.getName(), PortalType.NETHER);
        resourceManager.getMultiverseNetherPortalsApi().addWorldLink(nether.getName(), overworld.getName(), PortalType.NETHER);

        overworld.setGameMode(GameMode.SURVIVAL);
        nether.setGameMode(GameMode.SURVIVAL);
        overworld.setRespawnWorld(overworld.getName());
        nether.setRespawnWorld(overworld.getName());

        resourceManager.getMultiverseCoreApi().getWorldManager().saveWorldsConfig();

        resourceManager.trackTempWorld(overworld);
        resourceManager.trackTempWorld(nether);

        World bukkitOverworld = overworld.getBukkitWorld().getOrElseThrow(() -> new CreateGameException(
                String.format("Could not load bukkit world '%s'", overworld.getName())
        ));
        World bukkitNether = nether.getBukkitWorld().getOrElseThrow(() -> new CreateGameException(
                String.format("Could not load bukkit world '%s'", nether.getName())
        ));

        return new MiniPvPGame(resourceManager, bukkitOverworld, bukkitNether, configuration);
    }
}
