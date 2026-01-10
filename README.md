<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.19%2B-brightgreen?style=for-the-badge&logo=minecraft" alt="Minecraft Version">
  <img src="https://img.shields.io/badge/Platform-Paper%20%7C%20Spigot-blue?style=for-the-badge" alt="Platform">
  <img src="https://img.shields.io/badge/License-AGPL--3.0-red?style=for-the-badge" alt="License">
  <img src="https://img.shields.io/badge/Version-2.0.0-orange?style=for-the-badge" alt="Version">
</p>

<h1 align="center">⚔️ VirtualKit</h1>

<p align="center">
  <b>The ultimate per-player kit management system for PvP servers</b><br>
  Create, save, share, and manage personal kits with a beautiful GUI
</p>

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🎒 **9 Personal Kit Slots** | Each player can save up to 9 unique kits |
| 📦 **9 Virtual Enderchests** | Separate enderchest storage slots per player |
| 🏪 **Kit Room** | Admin-configured item shop with 5 pages |
| 🎁 **Premade Kits** | Server-wide public kits for quick access |
| 🔗 **Kit Sharing** | Share kits with other players via codes |
| 🔍 **Staff Inspection** | Moderators can view and edit player kits |
| ⚡ **Quick Commands** | `/k1`-`/k9` and `/ec1`-`/ec9` for instant loading |
| 🔄 **Regear System** | Quickly refill consumables mid-fight |
| 🛡️ **Anti-Exploit** | Protection against illegal items and exploits |
| 💾 **Multiple Storage** | SQLite, MySQL, Redis, or YAML storage |
| 🎨 **Fully Customizable** | Every message, title, and GUI element is configurable |

---

## 📸 Screenshots

<details>
<summary>Click to view screenshots</summary>

### Main Menu
Players can view all their kit and enderchest slots at a glance.

### Kit Editor
Drag and drop items to create your perfect loadout.

### Kit Room
Browse admin-curated items across 5 pages.

</details>

---

## 🚀 Quick Start

### Installation

1. Download the latest `VirtualKit-x.x.x.jar` from [Releases](../../releases)
2. Place it in your server's `plugins/` folder
3. Start your server
4. Stop the server and configure `plugins/VirtualKit/config.yml`
5. Restart and enjoy!

### Basic Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/kit` | Open main menu | `virtualkit.menu` |
| `/k1` - `/k9` | Load kit 1-9 | `virtualkit.kit` |
| `/ec1` - `/ec9` | Load enderchest 1-9 | `virtualkit.enderchest` |
| `/pk` | Load premade kit | `virtualkit.publickit` |
| `/sharekit <slot>` | Share a kit | `virtualkit.sharekit` |
| `/copykit <code>` | Copy a shared kit | `virtualkit.copykit` |
| `/regear` or `/rg` | Refill consumables | `virtualkit.regear` |
| `/heal` | Heal yourself | `virtualkit.heal` |
| `/repair` | Repair all items | `virtualkit.repair` |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/kitroom` | Edit the kit room | `virtualkit.editkitroom` |
| `/savepublickit <id>` | Save premade kit | `virtualkit.admin` |
| `/inspectkit <player> <slot>` | View player's kit | `virtualkit.staff` |
| `/inspectec <player> <slot>` | View player's enderchest | `virtualkit.staff` |
| `/virtualkit reload` | Reload configuration | `virtualkit.admin` |

---

## ⚙️ Configuration

### Storage Options

```yaml
storage:
  type: "sqlite"    # Options: sqlite, mysql, redis, yml
```

**SQLite** (Default) - Best for single servers, no setup required  
**MySQL** - Best for networks and multi-server setups  
**Redis** - For advanced caching setups  
**YAML** - Simple file storage, best for small servers

### Key Features

```yaml
feature:
  # Auto-rekit on respawn
  rekit-on-respawn: true
  # Give kit to killer
  rekit-on-kill: false
  # Heal on kit load
  set-health-on-kit-load: false
```

### Enderchest Cooldowns

```yaml
ec-cooldown:
  enabled: true
  time-seconds: 60        # Cooldown duration
  reset-on-kill: true     # Reset cooldown on kill
```

---

## 🔐 Permissions

### Permission Groups

| Permission | Description |
|------------|-------------|
| `virtualkit.admin` | Full access (includes all below) |
| `virtualkit.staff` | Moderation features (inspect kits) |
| `virtualkit.use` | All player features |

### Individual Permissions

<details>
<summary>Click to expand full permission list</summary>

| Permission | Description |
|------------|-------------|
| `virtualkit.menu` | Access `/kit` main menu |
| `virtualkit.kit` | Load kits via `/k1`-`/k9` |
| `virtualkit.enderchest` | Load enderchests via `/ec1`-`/ec9` |
| `virtualkit.publickit` | Load premade kits |
| `virtualkit.sharekit` | Share kits with other players |
| `virtualkit.copykit` | Copy shared kits |
| `virtualkit.swapkit` | Swap kit positions |
| `virtualkit.deletekit` | Delete saved kits |
| `virtualkit.regear` | Use regear command |
| `virtualkit.heal` | Use heal command |
| `virtualkit.repair` | Use repair command |
| `virtualkit.editkitroom` | Edit kit room (admin) |
| `virtualkit.rekitonrespawn` | Auto-rekit on respawn |
| `virtualkit.rekitonkill` | Receive kit on kill |

</details>

---

## 📁 File Structure

```
plugins/VirtualKit/
├── config.yml          # Main configuration
├── messages.yml        # All messages and titles
├── gui/
│   ├── main-menu.yml   # Main menu layout
│   ├── kit-editor.yml  # Kit editor buttons
│   ├── kit-room.yml    # Kit room buttons
│   └── public-kits.yml # Public kits menu
└── storage/            # Player data (if using file storage)
```

---

## 🎨 Customization

### Titles (On-Screen Messages)

Edit `messages.yml` to customize the big on-screen titles:

```yaml
titles:
  kit-saved:
    enabled: true
    title: "§aᴋɪᴛ ꜱᴀᴠᴇᴅ"
    subtitle: "§7ꜱʟᴏᴛ %slot%"
  kit-empty:
    enabled: true
    title: "§cɴᴏᴛ ꜱᴀᴠᴇᴅ"
    subtitle: "§7ᴇᴍᴘᴛʏ ᴋɪᴛ"
```

### GUI Appearance

Customize colors in `config.yml`:

```yaml
appearance:
  primary-color: "<#00FF00>"           # GUI title color
  glass-material: LIME_STAINED_GLASS_PANE
```

---

## 🔧 Dependencies

- **Paper/Spigot 1.19+**
- [Canvas GUI Library](https://github.com/IPVP-MC/canvas) (bundled)
- PlaceholderAPI *(optional)*

---

## 📜 License

VirtualKit is licensed under the [GNU Affero General Public License v3.0](LICENSE).

This means:
- ✅ You can use, modify, and distribute this software
- ✅ You can use it on commercial servers
- ⚠️ You must keep it open source if you distribute modified versions
- ⚠️ Network use counts as distribution

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

---

## 💬 Support

- 📖 [Configuration Guide](./CONFIG.md)
- 📋 [Commands Reference](./COMMANDS.md)
- 🔌 [API Documentation](./API.md)
- 🐛 [Report a Bug](../../issues)

---

## 🙏 Credits

VirtualKit is a fork and continuation of the original **PerPlayerKit** plugin.

| Credit | Contribution |
|--------|--------------|
| [**PerPlayerKit**](https://github.com/noahbclarkson/PerPlayerKit) | Original plugin by Noah Clark |
| [**Canvas**](https://github.com/IPVP-MC/canvas) | GUI library |
| **ACE** | VirtualKit 2.0 development |

Special thanks to the original developers for creating such an amazing foundation!

---

<p align="center">
  Made with ❤️ by <b>ACE</b><br>
  <i>Based on PerPlayerKit by Noah Clark</i>
</p>
