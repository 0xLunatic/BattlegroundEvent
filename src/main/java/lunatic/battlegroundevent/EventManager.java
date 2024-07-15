package lunatic.battlegroundevent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventManager {
    private final int minTeams;
    private final List<PartyManager.Party> registeredParties = new ArrayList<>();
    private final PartyManager partyManager;
    private Main plugin;
    private boolean eventStarted = false;

    public EventManager(Main plugin, PartyManager partyManager, int minTeams) {
        this.partyManager = partyManager;
        this.minTeams = minTeams;
        this.plugin = plugin;

        // Start the scheduler to check for event start every 10 seconds
        startEventScheduler();
    }

    private void startEventScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (registeredParties.size() >= minTeams && !eventStarted) {
                    startEvent();
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 200, 200); // 10 seconds (20 ticks) delay between checks
    }

    public boolean registerParty(Player player) {
        PartyManager.Party party = partyManager.getParty(player);
        if (party == null || !party.leader.equals(player.getUniqueId()) || party.members.size() < partyManager.getMaxPartySize() - 1) {
            return false;
        }
        if (registeredParties.contains(party)) {
            return false;
        }
        registeredParties.add(party);
        return true;
    }

    public boolean isPartyRegistered(Player player) {
        PartyManager.Party party = partyManager.getParty(player);
        return party != null && registeredParties.contains(party);
    }

    public boolean isEventStarted() {
        return eventStarted;
    }

    public boolean leaveEvent(Player player) {
        PartyManager.Party party = partyManager.getParty(player);
        if (party == null || !registeredParties.contains(party)) {
            return false;
        }
        registeredParties.remove(party);
        player.sendMessage(ChatColor.RED + "You have left the Battleground Event.");
        return true;
    }

    private void startEvent() {
        eventStarted = true; // Mark the event as started
        World world = Bukkit.getWorld("flat");
        if (world == null) {
            Bukkit.getLogger().severe("World 'flat' not found!");
            return;
        }

        // Calculate unique teleport locations for each party
        List<Location> teleportLocations = calculateUniqueTeleportLocations(world, registeredParties.size());

        int index = 0;
        for (PartyManager.Party party : registeredParties) {
            Location loc = teleportLocations.get(index);
            index++;

            // Broadcast countdown and teleport message to all party members
            broadcastCountdownAndTeleport(party, loc);
        }
    }

    private List<Location> calculateUniqueTeleportLocations(World world, int numLocationsNeeded) {
        List<Location> locations = new ArrayList<>();
        double radius = 50; // Radius within which locations should be generated
        int locationsPerSide = (int) Math.ceil(Math.sqrt(numLocationsNeeded)); // Number of locations per side of the square grid

        Location center = new Location(world, 0, 65, 0); // Center point for the grid (adjust as needed)

        // Generate random offsets for each location
        for (int i = 0; i < numLocationsNeeded; i++) {
            double angle = Math.random() * Math.PI * 2; // Random angle
            double randomRadius = Math.sqrt(Math.random()) * radius; // Random distance within radius

            double offsetX = Math.cos(angle) * randomRadius;
            double offsetZ = Math.sin(angle) * randomRadius;

            Location location = center.clone().add(offsetX, 0, offsetZ);

            // Ensure the location is safe and valid (e.g., not in the air)
            location.setY(world.getHighestBlockYAt(location));

            locations.add(location);
        }

        return locations;
    }

    private void broadcastCountdownAndTeleport(PartyManager.Party party, Location location) {
        // Broadcast message about teleportation
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + "Prepare for the Battleground Event!");
        Bukkit.broadcastMessage(ChatColor.GREEN + "Teleporting all teams to the arena in 20 seconds...");
        Bukkit.broadcastMessage("");

        // Start countdown
        new BukkitRunnable() {
            int count = 10; // Countdown starts from 10 seconds

            @Override
            public void run() {
                if (count <= 0) {
                    // Teleport all members including the leader
                    for (UUID memberId : new ArrayList<>(party.members)) {
                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null && member.isOnline()) {
                            member.teleport(location);
                            member.sendMessage(ChatColor.GREEN + "Teleported to the arena!");
                        } else {
                            // Remove offline or non-existent players from the party
                            party.members.remove(memberId);
                        }
                    }

                    // Teleport leader if online
                    Player leader = Bukkit.getPlayer(party.leader);
                    if (leader != null && leader.isOnline()) {
                        leader.teleport(location);
                        leader.sendMessage(ChatColor.GREEN + "Teleported to the arena!");
                    }

                    // Cancel the countdown task after teleporting
                    cancel();
                    return;
                }

                // Broadcast countdown message
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Teleporting in " + count + " seconds...");
                count--;
            }
        }.runTaskTimer(plugin, 0, 20); // 1 second (20 ticks) interval
    }
}
