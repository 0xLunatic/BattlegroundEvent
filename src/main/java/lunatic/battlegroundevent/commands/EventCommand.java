package lunatic.battlegroundevent.commands;

import lunatic.battlegroundevent.EventManager;
import lunatic.battlegroundevent.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventCommand implements CommandExecutor {

    private final EventManager eventManager;
    private final PartyManager partyManager;

    public EventCommand(EventManager eventManager, PartyManager partyManager) {
        this.eventManager = eventManager;
        this.partyManager = partyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /bgevent join");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                if (eventManager.registerParty(player)) {
                    player.sendMessage(ChatColor.GREEN + "Your party has been registered for the event.");
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage("§fTeam §a" + partyManager.getParty(player).name + " §fbaru saja mendaftar §eEvent§f!");
                    Bukkit.broadcastMessage("");
                    for (Player onlinePlayers : Bukkit.getOnlinePlayers()){
                        onlinePlayers.playSound(onlinePlayers, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You must be in party or you've been registered for this event!");
                }
                break;

            case "leave":
                if (eventManager.isPartyRegistered(player)) {
                    eventManager.leaveEvent(player);
                    PartyManager.Party party = partyManager.getParty(player);
                    if (party != null) {
                        Bukkit.broadcastMessage("");
                        Bukkit.broadcastMessage("§fTeam §a" + party.name + " §ftelah keluar dari daftar §eEvent§f!");
                        Bukkit.broadcastMessage("");
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_EVOKER_PREPARE_WOLOLO, 1f, 1f);
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Your team is not registered in the event.");
                }
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /bgevent join");
                break;
        }

        return true;
    }
}
