package lunatic.battlegroundevent;

import lunatic.battlegroundevent.commands.EventCommand;
import lunatic.battlegroundevent.commands.PartyCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        int maxPartySize = 1; // Default to 3 if not specified
        int minTeams = 1; // Default to 3 if not specified

        PartyManager partyManager = new PartyManager(maxPartySize);
        EventManager eventManager = new EventManager(this, partyManager, minTeams);

        if (getCommand("battlegroundparty") != null) {
            getCommand("battlegroundparty").setExecutor(new PartyCommand(partyManager));
        } else {
            getLogger().severe("Failed to register command 'battlegroundparty'.");
        }

        if (getCommand("battlegroundevent") != null) {
            getCommand("battlegroundevent").setExecutor(new EventCommand(eventManager, partyManager));
        } else {
            getLogger().severe("Failed to register command 'battlegroundevent'.");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
