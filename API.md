# VirtualKit API

The VirtualKit API is a simple Java API that allows developers to interact with the plugin. The API is **NOT** stable and will possibly change in the future.

## Example Usage

Add plugin jar to `./lib` folder in your project.

Add to pom.xml:

```xml
<dependency>
    <groupId>dev.ace</groupId>
    <artifactId>VirtualKit</artifactId>
    <version>2.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/VirtualKit-2.0.0.jar</systemPath>
</dependency>
```

```java
import dev.ace.virtualkit.KitManager;
import dev.ace.virtualkit.PublicKit;
// other imports

public class ExamplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Make sure VirtualKit is loaded
        if (getServer().getPluginManager().getPlugin("VirtualKit") == null) {
            getLogger().warning("VirtualKit not found!");
            return;
        }
    }

    // Give a player a public kit when they join the server
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // Get the KitManager instance
        KitManager kitManager = KitManager.get();

        // Get a list of all public kits
        List<PublicKit> publicKits = kitManager.getPublicKitList();

        // Load a public kit for the player
        if (!publicKits.isEmpty()) {
            kitManager.loadPublicKit(e.getPlayer(), publicKits.get(0).id);
        }
    }

    // Load a specific kit slot for a player
    public void loadPlayerKit(Player player, int slot) {
        KitManager.get().loadKit(player, slot);
    }

    // Check if a player has a kit saved in a slot
    public boolean hasKit(Player player, int slot) {
        return KitManager.get().hasKit(player.getUniqueId(), slot);
    }

}
```

## Available Methods

| Method | Description |
|--------|-------------|
| `KitManager.get()` | Get the KitManager singleton instance |
| `loadKit(Player, int slot)` | Load a kit from a slot |
| `loadEnderchest(Player, int slot)` | Load an enderchest from a slot |
| `loadPublicKit(Player, String id)` | Load a public/premade kit |
| `hasKit(UUID, int slot)` | Check if player has a kit in slot |
| `hasEC(UUID, int slot)` | Check if player has an enderchest in slot |
| `getPublicKitList()` | Get all public kits |
| `regearKit(Player, int slot)` | Regear from a kit slot |
