package me.timwastaken.minipvp.game;

import me.timwastaken.minipvp.PluginResourceManager;
import me.timwastaken.minipvp.common.OptionalOnlinePlayer;
import me.timwastaken.minipvp.common.Utils;
import me.timwastaken.minipvp.exceptions.IllegalOperationException;
import me.timwastaken.minipvp.game.config.GameConfiguration;
import me.timwastaken.minipvp.ui.Notifications;
import me.timwastaken.minipvp.ui.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MiniPvPGame implements Listener {
    private static final Predicate<Long> NOTIFY_PROT_END_PREDICATE = (remainingTicks) -> {
        long remSeconds = remainingTicks / 20;
        long remMinutes = remSeconds / 60;

        return (remSeconds > 0) && (remainingTicks % 20 == 0) &&
                (remSeconds % 60 == 0 && remMinutes <= 2 ||
                        remSeconds == 30 || remSeconds == 15 || remSeconds == 10 || remSeconds <= 5);
    };
    private static final String SCOREBOARD_TEAM_NAME = "nopvp";

    private final PluginResourceManager resourceManager;
    private final World overworld;
    private final World nether;
    private final GameConfiguration gameConfig;

    private final ScoreboardDisplay display;
    private final Set<OptionalOnlinePlayer> participants;
    private final Map<OptionalOnlinePlayer, Integer> playerLives;
    private Map<String, Integer> sortedByLives;
    private boolean lockPlayerMovement;
    private boolean gameRunning;
    private Instant protectionStart;

    private final Team pvpTeam;

    public MiniPvPGame(
            PluginResourceManager resourceManager,
            World overworld,
            World nether,
            GameConfiguration gameConfig
    ) {
        this.resourceManager = resourceManager;
        this.overworld = overworld;
        this.nether = nether;
        this.gameConfig = gameConfig;

        this.display = new ScoreboardDisplay();
        this.participants = new HashSet<>();
        this.playerLives = new HashMap<>();
        this.sortedByLives = new HashMap<>();

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.pvpTeam = scoreboard.registerNewTeam(SCOREBOARD_TEAM_NAME);

        prepareWorlds();
        this.lockPlayerMovement = true;
        this.gameRunning = false;

        resourceManager.registerEventListener(this);
    }

    public void registerPlayers(Collection<OptionalOnlinePlayer> players) {
        for (OptionalOnlinePlayer player : players) {
            registerPlayer(player);
        }
    }

    public boolean hasStarted() {
        return gameRunning;
    }

    public void registerPlayer(OptionalOnlinePlayer player) throws IllegalOperationException {
        if (gameRunning) throw new IllegalOperationException(
                "You can't add new participants while the game is running."
        );
        participants.add(player);
    }

    public void removePlayer(OptionalOnlinePlayer player) throws IllegalOperationException {
        if (!participants.contains(player)) throw new IllegalOperationException(
                "This player is not part of the game."
        );
        participants.remove(player);
        // add checks?
    }

    public void startCountdown(long countdownSeconds) {
        if (participants.size() < 2) throw new IllegalOperationException(
                "Cannot start the game with less than two players."
        );

        // teleport players to world
        teleportToSpawn(
                participants.stream()
                        .filter(OptionalOnlinePlayer::isOnline)
                        .map(OptionalOnlinePlayer::get).toList()
        );

        // reset players
        participants.forEach(participant -> {
            playerLives.put(participant, gameConfig.playerLives());
            participant.run(player -> {
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.setHealth(20);
                player.setFoodLevel(20);
                player.setSaturation(5);
                player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

                player.getInventory().addItem(gameConfig.startingFood());
            });
        });
        this.sortByLives();

        WorldBorder border = overworld.getWorldBorder();
        border.setCenter(overworld.getSpawnLocation());
        border.setSize(gameConfig.finalWorldBorderSize());
        border.setSize(gameConfig.initialWorldBorderSize(), countdownSeconds);

        resourceManager.runTaskTimer(new BukkitRunnable() {
            private long remainingSeconds = countdownSeconds;

            @Override
            public void run() {
                if (remainingSeconds <= 0) {
                    this.cancel();
                    Notifications.announceGameStart(participants, gameConfig.protectionTime().toSeconds());
                    Sounds.RESUME_GAME.playTo(participants);
                    startGame();
                } else {
                    Notifications.showCountdownTo(participants, remainingSeconds);
                    Sounds.COUNTDOWN.playTo(participants);
                    remainingSeconds--;
                }
            }
        }, 0L, 20L);
    }

    public void teleportToSpawn(List<Player> players) {
        Location to = overworld.getSpawnLocation();
        Utils.spreadPlayersInCircle(players, to, 3.5);
//        resourceManager.getMultiverseCoreApi().getSafetyTeleporter().to(to)
//                .checkSafety(false)
//                .teleport(players)
//                .onSuccess(() -> Utils.spreadPlayersInCircle(players, to, 3.5));

    }

    public boolean isParticipant(OptionalOnlinePlayer p) {
        return participants.contains(p);
    }

    private void startGame() {
        gameRunning = true;

//        overworld.setGameRule(GameRule.ADVANCE_TIME, true);
        overworld.setGameRuleValue("doDayLightCycle", "true");
//        overworld.setGameRule(GameRule.MOB_GRIEFING, true);
        overworld.setGameRuleValue("mobGriefing", "true");
//        overworld.setGameRule(GameRule.ADVANCE_WEATHER, true);
        overworld.setGameRuleValue("doWeatherCycle", "true");
        overworld.setTime(0L);
        lockPlayerMovement = false;
        protectionStart = Instant.now();

        WorldBorder border = overworld.getWorldBorder();
        border.setSize(gameConfig.finalWorldBorderSize(), gameConfig.gatheringTime().toSeconds());

        // run game loop
        resourceManager.runTaskTimer(new BukkitRunnable() {
            private long protCountdown = gameConfig.protectionTime().toSeconds() * 20L;
            private long elapsed = 0;

            @Override
            public void run() {
                if (gameRunning) elapsed++;

                if (NOTIFY_PROT_END_PREDICATE.test(protCountdown)) {
                    Notifications.announceProtectionPeriodEndIn(participants, protCountdown / 20L);
                    Sounds.COUNTDOWN.playTo(participants);
                }
                if (protCountdown == 0) {
                    endProtectionPeriod();
                }
                if (protCountdown >= -40) protCountdown--;

                for (OptionalOnlinePlayer participant : participants) {
                    if (!participant.isOnline()) continue;
                    Player player = participant.get();
                    display.updateBoard(
                            player,
                            elapsed / 20,
                            (protCountdown + 19) / 20,
                            sortedByLives,
                            gameConfig.playerLives()
                    );
                }
            }
        }, 0L, 1L);
    }

    private void endProtectionPeriod() {
        Notifications.announceProtectionPeriodEnd(participants);
        Sounds.FIGHT_ANNOUNCEMENT.playTo(participants);
        setPVP(true);
    }

    private void setPVP(boolean pvp) {
        pvpTeam.setAllowFriendlyFire(pvp);
    }

    private void prepareWorlds() {
//        overworld.setGameRule(GameRule.ADVANCE_TIME, false);
        overworld.setGameRuleValue("doDayLightCycle", "false");
//        overworld.setGameRule(GameRule.MOB_GRIEFING, false);
        overworld.setGameRuleValue("mobGriefing", "false");
//        overworld.setGameRule(GameRule.ADVANCE_WEATHER, false);
        overworld.setGameRuleValue("doDayLightCycle", "false");
        setPVP(false);
//        overworld.setGameRule(GameRule.KEEP_INVENTORY, true);
        overworld.setGameRuleValue("keepInventory", "true");
        overworld.setTime(6000L);

//        nether.setGameRule(GameRule.KEEP_INVENTORY, true);
        nether.setGameRuleValue("keepInventory", "true");

        for (OptionalOnlinePlayer participant : participants) {
            String name = participant.getOffline().getName();
            pvpTeam.addEntry(name);
        }
    }

    private boolean recordPlayerDeath(Player participant) {
        OptionalOnlinePlayer p = OptionalOnlinePlayer.of(participant);
        Integer lives = playerLives.get(p);
        if (lives == null) return false;
        lives--;
        playerLives.put(p, lives);
        this.sortByLives();
        if (lives <= 0) {
            eliminatePlayer(participant, !checkWinCondition());
            return true;
        }
        Sounds.PLAYER_DEATH.playTo(participants);
        return false;
    }

    private void sortByLives() {
        this.sortedByLives = playerLives.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        entry -> Optional.ofNullable(Bukkit
                                .getOfflinePlayer(entry.getKey().getPlayerUUID())
                                .getName()).orElse("Unknown"),
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private void eliminatePlayer(Player eliminated, boolean showEliminationScreen) {
        eliminated.setGameMode(GameMode.SPECTATOR);
        Notifications.announcePlayerElimination(participants, eliminated);
        if (showEliminationScreen)
            Notifications.showEliminationTo(eliminated);
        Sounds.PLAYER_ELIMINATION.playTo(participants);
    }

    private boolean checkWinCondition() {
        int playersAlive = 0;
        OptionalOnlinePlayer winnerCandidate = null;
        for (Map.Entry<OptionalOnlinePlayer, Integer> playerLivesEntry : playerLives.entrySet()) {
            if (playerLivesEntry.getValue() <= 0) continue;
            playersAlive++;
            winnerCandidate = playerLivesEntry.getKey();
        }
        if (playersAlive <= 1 && winnerCandidate != null) {
            endGame(winnerCandidate);
            return true;
        }
        return false;
    }

    private void endGame(OptionalOnlinePlayer winner) {
        String winnerName = winner
                .map(Player::getName)
                .orElse(
                        Optional.ofNullable(Bukkit.getOfflinePlayer(winner.getPlayerUUID()).getName())
                                .orElse("Unknown")
                );
        Notifications.announceGameWinnerTo(participants, winnerName);
        Sounds.GAME_END.playTo(participants);
        gameRunning = false;
    }

    private boolean isInGameEnvironment(Player p) {
        return isInGameEnvironment(p.getWorld());
    }

    private boolean isInGameEnvironment(World world) {
        if (world == null) return false;
        return world.equals(overworld) || world.equals(nether);
    }

    private boolean isParticipant(Player p) {
        return participants.contains(OptionalOnlinePlayer.of(p));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isInGameEnvironment(event.getPlayer())) return;
        if (!isParticipant(event.getPlayer())) return;
        if (event.getTo() == null) return;
        if (event.getFrom().distanceSquared(event.getTo()) > 0) event.setCancelled(lockPlayerMovement);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!isInGameEnvironment(victim)) return;
        if (!isParticipant(victim)) return;
        if (!gameRunning) return;
        Instant now = Instant.now();
        if (Duration.between(protectionStart, now).compareTo(gameConfig.protectionTime()) <= 0) {
            if (gameConfig.disableAllDamageDuringProtection()) {
                event.setCancelled(true);
                return;
            }
        }
        // check if player dies
        if (victim.getHealth() - event.getFinalDamage() <= 0) {
            if (recordPlayerDeath(victim)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(BlockBreakEvent event) {
        if (!isInGameEnvironment(event.getBlock().getWorld())) return;
        Map<Material, ItemStack> modifiedDrops = gameConfig.modifiedDrops();
        if (!modifiedDrops.containsKey(event.getBlock().getType())) return;
        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);
        final ItemStack modified = modifiedDrops.get(event.getBlock().getType());
        event.getBlock().getWorld().dropItemNaturally(
                event.getBlock().getLocation().add(0.5, 0.5, 0.5),
                modified
        );
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        ListIterator<ItemStack> dropIterator = event.getDrops().listIterator();

        while (dropIterator.hasNext()) {
            ItemStack drop = dropIterator.next();
            Material cookedType = gameConfig.modifiedEntityDrops().get(drop.getType());
            if (cookedType == null) continue;
            ItemStack cookedDrop = new ItemStack(cookedType, drop.getAmount());
            dropIterator.set(cookedDrop);
        }
    }
}
