package me.timwastaken.minipvp.game.factory;

import me.timwastaken.minipvp.PluginResourceManager;
import me.timwastaken.minipvp.exceptions.CreateGameException;
import me.timwastaken.minipvp.game.MiniPvPGame;
import me.timwastaken.minipvp.game.config.GameConfiguration;

public interface GameFactory {
    MiniPvPGame createGame(PluginResourceManager resourceManager, String worldName, GameConfiguration configuration) throws CreateGameException;
}
