package me.timwastaken.minipvp.game.config;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.Map;

public interface GameConfiguration {
    Duration protectionTime();
    long initialWorldBorderSize();
    long finalWorldBorderSize();
    Duration gatheringTime();
    ItemStack startingFood();
    int playerLives();
    boolean disableAllDamageDuringProtection();

    Map<Material, ItemStack> modifiedDrops();
    Map<Material, Material> modifiedEntityDrops();
}
