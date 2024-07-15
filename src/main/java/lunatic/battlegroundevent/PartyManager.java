package lunatic.battlegroundevent;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PartyManager {
    private final int maxPartySize;
    private final Map<String, Party> partiesByName = new HashMap<>();
    private final Map<UUID, String> playerToPartyMap = new HashMap<>();

    public PartyManager(int maxPartySize) {
        this.maxPartySize = maxPartySize;
    }

    public boolean createParty(Player player, String partyName) {
        if (partiesByName.containsKey(partyName) || playerToPartyMap.containsKey(player.getUniqueId())) {
            return false;
        }
        Party party = new Party(player.getUniqueId(), partyName);
        partiesByName.put(partyName, party);
        playerToPartyMap.put(player.getUniqueId(), partyName);
        return true;
    }

    public boolean inviteToParty(Player leader, Player playerToAdd) {
        String partyName = playerToPartyMap.get(leader.getUniqueId());
        if (partyName == null) {
            return false;
        }
        Party party = partiesByName.get(partyName);
        if (party == null || party.members.size() >= maxPartySize - 1) {
            return false;
        }
        party.members.add(playerToAdd.getUniqueId());
        playerToPartyMap.put(playerToAdd.getUniqueId(), partyName);
        return true;
    }

    public boolean acceptInvitation(Player player) {
        String partyName = playerToPartyMap.get(player.getUniqueId());
        if (partyName == null) {
            return false;
        }
        Party party = partiesByName.get(partyName);
        if (party == null || party.members.size() >= maxPartySize - 1) {
            return false;
        }
        party.members.add(player.getUniqueId());
        return true;
    }

    public boolean leaveParty(Player player) {
        String partyName = playerToPartyMap.remove(player.getUniqueId());
        if (partyName == null) {
            return false;
        }
        Party party = partiesByName.get(partyName);
        if (party == null) {
            return false;
        }
        if (party.leader.equals(player.getUniqueId())) {
            disbandParty(party.leader);
        } else {
            party.members.remove(player.getUniqueId());
        }
        return true;
    }

    public boolean disbandParty(UUID leader) {
        String partyName = playerToPartyMap.remove(leader);
        if (partyName == null) {
            return false;
        }
        Party party = partiesByName.remove(partyName);
        if (party == null) {
            return false;
        }
        for (UUID member : party.members) {
            playerToPartyMap.remove(member);
        }
        return true;
    }

    public boolean isPartyLeader(Player player) {
        String partyName = playerToPartyMap.get(player.getUniqueId());
        if (partyName == null) {
            return false;
        }
        Party party = partiesByName.get(partyName);
        return party != null && party.leader.equals(player.getUniqueId());
    }

    public Party getParty(Player player) {
        String partyName = playerToPartyMap.get(player.getUniqueId());
        if (partyName == null) {
            return null;
        }
        return partiesByName.get(partyName);
    }
    public int getMaxPartySize() {
        return maxPartySize;
    }

    public class Party {
        public UUID leader;
        public String name;
        public Set<UUID> members;

        public Party(UUID leader, String name) {
            this.leader = leader;
            this.name = name;
            this.members = new HashSet<>();
        }
    }
}
