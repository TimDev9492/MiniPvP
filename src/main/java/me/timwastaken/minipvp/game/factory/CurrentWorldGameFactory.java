package me.timwastaken.minipvp.game.factory;

import me.timwastaken.minipvp.PluginResourceManager;
import me.timwastaken.minipvp.exceptions.CreateGameException;
import me.timwastaken.minipvp.game.MiniPvPGame;
import me.timwastaken.minipvp.game.config.GameConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Optional;

public class CurrentWorldGameFactory implements GameFactory {
    @Override
    public MiniPvPGame createGame(PluginResourceManager resourceManager, String worldName, GameConfiguration configuration) throws CreateGameException {
        String overworldName = "world";
        String netherName = "world_nether";
        World bukkitOverworld = Optional.ofNullable(Bukkit.getWorld(overworldName)).orElseThrow(() -> new CreateGameException(
                String.format("Could not load bukkit world '%s'", overworldName)
        ));
        World bukkitNether = Optional.ofNullable(Bukkit.getWorld(netherName)).orElseThrow(() -> new CreateGameException(
                String.format("Could not load bukkit world '%s'", netherName)
        ));

        return new MiniPvPGame(resourceManager, bukkitOverworld, bukkitNether, configuration);
    }
}
