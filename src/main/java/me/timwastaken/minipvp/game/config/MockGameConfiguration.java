package me.timwastaken.minipvp.game.config;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.Map;

public class MockGameConfiguration implements GameConfiguration {
    private final ItemStack TWO_IRON = new ItemStack(Material.IRON_INGOT, 2);
    private final ItemStack TWO_GOLD = new ItemStack(Material.GOLD_INGOT, 2);
    private final ItemStack TWO_COPPER = new ItemStack(Material.COPPER_INGOT, 2);
    private final ItemStack IRON_BLOCK = new ItemStack(Material.IRON_BLOCK, 1);
    private final ItemStack GOLD_BLOCK = new ItemStack(Material.GOLD_BLOCK);
    private final ItemStack COPPER_BLOCK = new ItemStack(Material.COPPER_BLOCK);

    private final Map<Material, ItemStack> MOD_DROPS = Map.of(
            Material.IRON_ORE, TWO_IRON,
            Material.DEEPSLATE_IRON_ORE, TWO_IRON,
            Material.RAW_IRON_BLOCK, IRON_BLOCK,
            Material.GOLD_ORE, TWO_GOLD,
            Material.DEEPSLATE_GOLD_ORE, TWO_GOLD,
            Material.RAW_GOLD_BLOCK, GOLD_BLOCK,
            Material.COPPER_ORE, TWO_COPPER,
            Material.DEEPSLATE_COPPER_ORE, TWO_COPPER,
            Material.RAW_COPPER_BLOCK, COPPER_BLOCK
    );
    private final Map<Material, Material> MOD_ENTITY_DROPS = Map.of(
            Material.BEEF, Material.COOKED_BEEF,
            Material.PORKCHOP, Material.COOKED_PORKCHOP,
            Material.CHICKEN, Material.COOKED_CHICKEN,
            Material.MUTTON, Material.COOKED_MUTTON,
            Material.RABBIT, Material.COOKED_RABBIT,
            Material.COD, Material.COOKED_COD,
            Material.SALMON, Material.COOKED_SALMON
    );

    @Override
    public Duration protectionTime() {
        return Duration.ofMinutes(2);
    }

    @Override
    public long initialWorldBorderSize() {
        return 512;
    }

    @Override
    public long finalWorldBorderSize() {
        return 48;
    }

    @Override
    public Duration gatheringTime() {
        return Duration.ofMinutes(30);
    }

    @Override
    public ItemStack startingFood() {
        return new ItemStack(Material.BREAD, 10);
    }

    @Override
    public int playerLives() {
        return 3;
    }

    @Override
    public boolean disableAllDamageDuringProtection() {
        return true;
    }

    @Override
    public Map<Material, ItemStack> modifiedDrops() {
        return MOD_DROPS;
    }

    @Override
    public Map<Material, Material> modifiedEntityDrops() {
        return MOD_ENTITY_DROPS;
    }
}
