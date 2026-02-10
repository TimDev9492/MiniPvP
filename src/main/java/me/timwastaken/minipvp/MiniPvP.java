package me.timwastaken.minipvp;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import me.timwastaken.minipvp.commands.GameCommand;
import me.timwastaken.minipvp.commands.UsageHandler;
import me.timwastaken.minipvp.exceptions.MiniPvPException;
import me.timwastaken.minipvp.game.factory.CurrentWorldGameFactory;
import me.timwastaken.minipvp.game.factory.WorldGameFactory;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;

import java.util.Optional;

public final class MiniPvP extends JavaPlugin {
    private MultiverseCoreApi coreApi;
    private MultiverseNetherPortals netherPortalsApi;
    private LiteCommands<CommandSender> liteCommands;
    private PluginResourceManager resourceManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
//        loadDependencies();

        resourceManager = new PluginResourceManager(
                this,
                coreApi,
                netherPortalsApi,
                new CurrentWorldGameFactory()
        );

        liteCommands = LiteBukkitFactory.builder(getName().toLowerCase(), this)
                .commands(
                        new GameCommand(resourceManager)
                ).invalidUsage(new UsageHandler()).build();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (liteCommands != null) liteCommands.unregister();

        resourceManager.unregisterEvents();
        resourceManager.unregisterTasks();
        resourceManager.deleteTempWorlds();
    }

    private void loadDependencies() throws MiniPvPException {
        RegisteredServiceProvider<MultiverseCoreApi> mvCoreProvider = Optional.ofNullable(Bukkit.getServicesManager()
                .getRegistration(MultiverseCoreApi.class)).orElseThrow(() -> new MiniPvPException(
                        "Failed to load MultiverseCore API."
        ));
        coreApi = mvCoreProvider.getProvider();
        try {
            netherPortalsApi = (MultiverseNetherPortals) Optional.ofNullable(getServer().getPluginManager()
                    .getPlugin("Multiverse-NetherPortals")).orElseThrow(
                    () -> new MiniPvPException("Failed to load MultiverseNetherPortals API.")
            );
        } catch (ClassCastException ex) {
            throw new MiniPvPException(ex.getMessage());
        }
    }
}
