package lunatic.battlegroundevent.commands;

import lunatic.battlegroundevent.PartyManager;
import lunatic.battlegroundevent.PartyManager.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class PartyCommand implements CommandExecutor {

    private final PartyManager partyManager;

    public PartyCommand(PartyManager partyManager) {
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
            player.sendMessage(ChatColor.RED + "Usage: /battlegroundparty <create|invite|leave|disband|list|accept> [player]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /battlegroundparty create <party name>");
                    return true;
                }
                String partyName = args[1];
                if (partyManager.createParty(player, partyName)) {
                    player.sendMessage(ChatColor.GREEN + "Party created with name '" + partyName + "'. You are now the leader.");
                } else {
                    player.sendMessage(ChatColor.RED + "You are already in a party or an error occurred.");
                }
                break;

            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /battlegroundparty invite <player>");
                    return true;
                }

                if (!partyManager.isPartyLeader(player)) {
                    player.sendMessage(ChatColor.RED + "You are not a party leader.");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }

                if (target.equals(player)) {
                    player.sendMessage(ChatColor.RED + "You can't invite yourself.");
                    return true;
                }

                if (partyManager.inviteToParty(player, target)) {
                    player.sendMessage(ChatColor.GREEN + target.getName() + " has been invited to the party.");
                    target.sendMessage(ChatColor.GREEN + "You have been invited to " + player.getName() + "'s party. Use /battlegroundparty accept to join.");
                } else {
                    player.sendMessage(ChatColor.RED + "Party is full or an error occurred.");
                }
                break;

            case "accept":
                if (partyManager.acceptInvitation(player)) {
                    player.sendMessage(ChatColor.GREEN + "You have joined the party.");
                } else {
                    player.sendMessage(ChatColor.RED + "No invitation found or party is full.");
                }
                break;

            case "leave":
                if (partyManager.leaveParty(player)) {
                    player.sendMessage(ChatColor.GREEN + "You have left the party.");
                } else {
                    player.sendMessage(ChatColor.RED + "You are not in a party.");
                }
                break;

            case "disband":
                if (partyManager.isPartyLeader(player)) {
                    if (partyManager.disbandParty(player.getUniqueId())) {
                        player.sendMessage(ChatColor.GREEN + "Party disbanded.");
                    } else {
                        player.sendMessage(ChatColor.RED + "An error occurred while disbanding the party.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Only the party leader can disband the party.");
                }
                break;

            case "list":
                Party party = partyManager.getParty(player);
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a party.");
                    return true;
                }

                player.sendMessage(ChatColor.DARK_AQUA + "===== " + ChatColor.GOLD + party.name + ChatColor.DARK_AQUA + " =====");
                player.sendMessage(ChatColor.DARK_AQUA + "Leader: " + ChatColor.WHITE + Bukkit.getPlayer(party.leader).getName());
                player.sendMessage(ChatColor.DARK_AQUA + "Members:");
                for (UUID memberId : party.members) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        player.sendMessage(ChatColor.WHITE + "- " + member.getName());
                    }
                }
                player.sendMessage(ChatColor.DARK_AQUA + "=======================");
                break;

            default:
                player.sendMessage(ChatColor.RED + "Usage: /battlegroundparty <create|invite|leave|disband|list|accept> [player]");
                break;
        }

        return true;
    }
}
