package me.timwastaken.minipvp.common;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class Utils {
    public static void spreadPlayersInCircle(List<Player> players, Location center, double radius) {
        // specific edge case: ensure list isn't empty to avoid division by zero
        if (players == null || players.isEmpty() || center.getWorld() == null) {
            return;
        }

        // center middle of block
        center = center.clone();
        center.setX(center.getBlockX() + 0.5);
        center.setZ(center.getBlockZ() + 0.5);

        // Calculate the angle increment (in radians) for each player
        // 360 degrees = 2 * PI radians
        double angleIncrement = (2 * Math.PI) / players.size();

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            // Calculate current angle for this specific player
            double angle = i * angleIncrement;

            // Calculate new X and Z coordinates
            // cos(angle) gives x component, sin(angle) gives z component
            int x = Math.toIntExact(Math.round(center.getX() + (radius * Math.cos(angle))));
            int z = Math.toIntExact(Math.round(center.getZ() + (radius * Math.sin(angle))));
            double y = center.getWorld().getHighestBlockYAt(x, z) + 1;

            // Create the target location object
            Location targetLocation = new Location(center.getWorld(), x + 0.5, y, z + 0.5);

            // MATHEMATICALLY FACE INWARDS:
            // To face a target, we calculate the vector: TargetPosition - CurrentPosition
            Vector direction = center.toVector().add(new Vector(0, 1, 0)).subtract(targetLocation.toVector());
            targetLocation.setDirection(direction);

            // Teleport the player
            player.teleport(targetLocation);
        }
    }
}
