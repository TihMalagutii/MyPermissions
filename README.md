# MyPermissions

A comprehensive, permission management system for Hytale servers. Provides fine-grained access control with group inheritance, wildcards, priority-based chat formatting, and automatic backup functionality.

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://www.oracle.com/java/)
[![Hytale](https://img.shields.io/badge/Hytale-Core%20API-blue.svg)](https://hytale.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## 📋 Features

### Core Permission System
- **Thread-safe permission checking** with caching for optimal performance
- **Wildcard support**: Full (`*`), partial (`plugin.*`), and mid-pattern (`plugin.*.admin`)
- **Permission negation**: Explicit denials with `-permission.node` override any grants
- **Group inheritance**: Recursive parent-child relationships with infinite loop prevention
- **UUID-based tracking**: Persistent player data with username mapping

### Group Management
- **Priority system**: Integer-based (higher = more important) for prefix/suffix resolution
- **Chat integration**: Automatic prefix/suffix with color code support (`&c`, `&l`, etc.)
- **Flexible inheritance**: Groups can inherit from multiple parents
- **Default group**: Automatic assignment for new players

### Data Persistence
- **JSON configuration**: Human-readable with pretty-printing via Gson
- **Automatic backups**: Creates timestamped backups before each save
- **Backup rotation**: Maintains last 5 backups automatically
- **Corruption recovery**: Auto-restore from latest backup on load failure

### Integration
- **Hytale Permissions API**: Registers as official provider via `PermissionsModule`
- **Public API**: Methods for external plugin integration
- **Event listeners**: Auto-assign default groups on player join
- **Chat formatter**: Custom message formatting with priority-based prefix/suffix

---

## 🏗️ Architecture

### Project Structure

```
src/main/java/com/mypermissions/
├── Main.java                          # Plugin entry point & public API
├── manager/
│   └── PermissionManager.java         # Permission checking engine
├── config/
│   ├── ConfigManager.java             # JSON persistence & backup system
│   └── MyPermissionsConfig.java       # Data models (Groups, Users)
├── provider/
│   └── MyPermissionsProvider.java     # Hytale API integration
├── listener/
│   ├── PlayerListener.java            # Join event handler
│   └── ChatListener.java              # Chat formatting with color codes
└── command/
    ├── MyPermsCommand.java            # Help command
    ├── MyPermsReloadCommand.java      # Reload configuration
    ├── MyPermsCheckCommand.java       # Test permissions
    ├── group/                         # Group management commands
    │   ├── GroupListCommand.java
    │   ├── GroupInfoCommand.java
    │   ├── GroupCreateCommand.java
    │   ├── GroupDeleteCommand.java
    │   ├── GroupAddPermCommand.java
    │   ├── GroupRemovePermCommand.java
    │   ├── GroupSetParentCommand.java
    │   └── GroupSetPriorityCommand.java
    ├── user/                          # User management commands
    │   ├── UserAddCommand.java
    │   ├── UserRemoveCommand.java
    │   ├── UserInfoCommand.java
    │   ├── UserAddPermCommand.java
    │   └── UserRemovePermCommand.java
    └── backup/                        # Backup commands
        ├── BackupListCommand.java
        └── BackupRestoreCommand.java
```

### Data Model

```json
{
  "defaultGroup": "default",
  "groups": {
    "groupName": {
      "permissions": ["permission.node", "-negated.permission"],
      "parents": ["parentGroup1", "parentGroup2"],
      "prefix": "&c[Admin] ",
      "suffix": " &7[Lvl 100]",
      "priority": 100
    }
  },
  "users": {
    "uuid-string": {
      "username": "PlayerName",
      "groups": ["group1", "group2"],
      "permissions": ["user.specific.permission"]
    }
  }
}
```

---

## 🚀 Installation

### Requirements
- Java 25 or higher
- Hytale Server with Core API support
- Gradle 9.2.0+ (for building from source)

### From Release
1. Download `MyPermissions-1.0-SNAPSHOT.jar` from releases
2. Place in your server's `mods/` folder
3. Restart the server
4. Configuration will be generated at `mods/MyPermissions/config.json`

### Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/MyPermissions.git
cd MyPermissions

# Build with Gradle
./gradlew build

# JAR will be at: build/libs/MyPermissions-1.0-SNAPSHOT.jar
```

---

## 🔧 Configuration

### Config Location
`plugins/MyPermissions/config.json`

### Default Configuration

```json
{
  "defaultGroup": "default",
  "groups": {
    "default": {
      "permissions": ["mypermissions.user"],
      "parents": [],
      "prefix": "&8[Default] ",
      "suffix": "",
      "priority": 0
    },
    "admin": {
      "permissions": ["*"],
      "parents": ["default"],
      "prefix": "&c[Admin] ",
      "suffix": "",
      "priority": 100
    }
  },
  "users": {}
}
```

### Permission Resolution Order

1. **User-specific permissions** (highest priority)
2. **User's group permissions** (checked in order of assignment)
3. **Parent group permissions** (recursive)
4. **Default group permissions** (fallback)

### Wildcard Matching Rules

```java
// Exact match
"server.fly" → matches "server.fly"

// Full wildcard
"*" → matches ALL permissions

// Partial wildcard
"server.*" → matches "server.fly", "server.speed", etc.

// Mid-pattern wildcard
"server.*.admin" → matches "server.world.admin", "server.player.admin"

// Negation (highest priority)
"-server.fly" → denies "server.fly" even if granted by "*"
```

---

## 📝 Commands

All commands require `myperms.admin` permission.

### Main Commands
| Command | Description |
|---------|-------------|
| `/myperms` | Display help menu |
| `/mpreload` | Reload configuration from disk |
| `/mpcheck <player> <permission>` | Test if player has permission |

### Group Commands
| Command | Description |
|---------|-------------|
| `/mpgroup-list` | List all groups |
| `/mpgroup-info <group>` | Show group details |
| `/mpgroup-create <group>` | Create new group |
| `/mpgroup-delete <group>` | Delete group |
| `/mpgroup-addperm <group> <permission>` | Add permission to group |
| `/mpgroup-removeperm <group> <permission>` | Remove permission from group |
| `/mpgroup-setparent <group> <parent>` | Set parent group |
| `/mpgroup-setpriority <group> <number>` | Set priority (higher = more important) |
| `/mpgroup-rename <oldName> <newName>` | Rename group |

### User Commands
| Command | Description |
|---------|-------------|
| `/mpuser-add <player> <group>` | Add player to group |
| `/mpuser-remove <player> <group>` | Remove player from group |
| `/mpuser-info <player>` | Show player's groups and permissions |
| `/mpuser-addperm <player> <permission>` | Give player permission |
| `/mpuser-removeperm <player> <permission>` | Remove player permission |

### Backup Commands
| Command | Description |
|---------|-------------|
| `/mpbackup-list` | List available backups |
| `/mpbackup-restore <backup>` | Restore from backup |

---

## 🎨 Color Code Support

Chat prefixes and suffixes support color codes:

### Colors
- `&0`-`&9`, `&a`-`&f` - Standard colors
- Examples: `&c` (red), `&6` (gold), `&a` (green)

### Formatting
- `&l` - Bold
- `&o` - Italic  
- `&r` - Reset all formatting

### Example Usage
```json
"prefix": "&c&l[ADMIN]&r &f",
"suffix": " &7[VIP]"
```

---

## 🔌 Developer API

### Public Methods in Main.java

```java
// Check if player has permission
Main.hasPermission(UUID uuid, String permission); // Returns boolean

// Get player's prefix
Main.getPermissionManager().getUserPrefix(String uuid); // Returns String

// Get player's suffix  
Main.getPermissionManager().getUserSuffix(String uuid); // Returns String

// Add player to group
Main.getPermissionManager().addUserToGroup(String uuid, String groupName); // Returns boolean

// Clear permission cache
Main.getPermissionManager().clearCache();
Main.getPermissionManager().clearCache(UUID uuid);
```

### Integration Example

```java
import com.mypermissions.Main;
import java.util.UUID;

public class YourPlugin {
    public void checkPlayerPermission(UUID playerUuid) {
        if (Main.hasPermission(playerUuid, "yourplugin.feature")) {
            // Player has permission
            String prefix = Main.getPermissionManager().getUserPrefix(playerUuid.toString());
            // Use the prefix in your plugin
        }
    }
}
```

---

## 🏗️ Technical Details

### Caching Strategy
- **ConcurrentHashMap** for thread-safe cache
- Cache key format: `uuid:permission.node`
- Automatic invalidation on permission changes
- Manual clearing via API or `/mpreload`

### Backup System
- Automatic backup before each save operation
- Timestamp format: `config_yyyy-MM-dd_HH-mm-ss.json`
- Retention policy: Keep last 5 backups
- Auto-recovery on config corruption

### Thread Safety
- All permission checks are thread-safe
- Uses `ConcurrentHashMap` for cache
- No synchronization locks needed for read operations

### Performance Considerations
- First check: ~100µs (cache miss + JSON read)
- Cached check: ~1µs (cache hit)
- Wildcard matching: O(n) where n = permission count
- Group inheritance: O(d) where d = inheritance depth

---

### Debug Mode

Check console logs for detailed information:
```
[MyPermissions] Player PlayerName (uuid) assigned to default group: default
[MyPermissions] Backup created: config_2026-01-15_14-30-00.json
[MyPermissions] Config restored from: config_2026-01-15_14-30-00.json
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/AmazingFeature`)
3. **Commit** your changes (`git commit -m 'Add some AmazingFeature'`)
4. **Push** to the branch (`git push origin feature/AmazingFeature`)
5. **Open** a Pull Request

### Code Style
- Follow existing code formatting
- Use meaningful variable/method names
- Add JavaDoc comments for public methods
- Keep comments in English
- Include unit tests for new features

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Built for the [Hytale](https://hytale.com/) server platform
- Uses [Gson](https://github.com/google/gson) for JSON serialization

---

## 📊 Roadmap

### Planned Features
- [ ] Admin panel in-game
- [ ] MySQL/PostgreSQL database support
- [ ] Temporary permissions with expiration
- [ ] Permission tracks (promotion/demotion chains)
- [ ] Verbose mode (detailed permission resolution logging)
- [ ] Context-based permissions (world-specific, time-based)
- [ ] Permission inheritance visualization

### Under Consideration
- [ ] Integration with Discord bots
- [ ] Multi-server synchronization
- [ ] Permission templates/presets
- [ ] Audit log for permission changes
- [ ] Migration tools from other permission plugins

---

## 📞 Support

- **Documentation**: See [USER_GUIDE.md](USER_GUIDE.md) for end-user documentation

---

**Made with ❤️ for the Hytale community**
