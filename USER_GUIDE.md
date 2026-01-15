# MyPermissions - User Guide

Welcome to **MyPermissions**! This guide will help you understand and use the permission system on your Hytale server.

## 📋 Table of Contents

- [What are Permissions?](#what-are-permissions)
- [Understanding Groups](#understanding-groups)
- [Command List](#command-list)
- [Common Use Cases](#common-use-cases)
- [Color Codes](#color-codes)
- [Troubleshooting](#troubleshooting)

---

## 🎯 What are Permissions?

Permissions control what players can do on your server. Each command or feature can require a specific permission. For example:
- `server.fly` - Allows flying
- `server.gamemode` - Allows changing game mode
- `myperms.admin` - Full access to MyPermissions commands

### Permission Formats

- **Exact permission**: `server.fly`
- **Wildcard (all)**: `*` - Grants ALL permissions
- **Partial wildcard**: `server.*` - Grants all permissions starting with "server."
- **Negated permission**: `-server.fly` - Explicitly denies this permission (even if granted by wildcard)

---

## 👥 Understanding Groups

Groups are collections of permissions that can be assigned to multiple players. Every server has at least two default groups:

### Default Groups

**default** (Priority: 0)
- Assigned automatically to new players
- Basic permissions for regular players
- Prefix: `&8[Default]` (dark gray)

**admin** (Priority: 100)
- Full server access with `*` permission
- Highest priority for chat prefix/suffix
- Prefix: `&c[Admin]` (red)

### Group Features

- **Inheritance**: Groups can inherit permissions from parent groups
- **Priority**: Higher priority groups take precedence for chat prefix/suffix
- **Prefix/Suffix**: Customize player names in chat with colors and text

---

## 📝 Command List

### Getting Help

```
/myperms
```
Shows all available commands with syntax.

---

### 👤 User Management

#### Add Player to Group
```
/mpuser-add <player> <group>
```
**Example**: `/mpuser-add Steve vip`

#### Remove Player from Group
```
/mpuser-remove <player> <group>
```
**Example**: `/mpuser-remove Steve vip`

#### View Player Info
```
/mpuser-info <player>
```
Shows player's groups and permissions.  
**Example**: `/mpuser-info Steve`

#### Give Player Permission
```
/mpuser-addperm <player> <permission>
```
**Example**: `/mpuser-addperm Steve server.fly`

#### Remove Player Permission
```
/mpuser-removeperm <player> <permission>
```
**Example**: `/mpuser-removeperm Steve server.fly`

---

### 👥 Group Management

#### List All Groups
```
/mpgroup-list
```
Shows all groups with their priorities.

#### View Group Details
```
/mpgroup-info <group>
```
Shows group's permissions, parents, prefix, suffix, and priority.  
**Example**: `/mpgroup-info vip`

#### Create New Group
```
/mpgroup-create <group>
```
**Example**: `/mpgroup-create moderator`

#### Delete Group
```
/mpgroup-delete <group>
```
**Example**: `/mpgroup-delete oldgroup`

#### Add Permission to Group
```
/mpgroup-addperm <group> <permission>
```
**Example**: `/mpgroup-addperm vip server.fly`

#### Remove Permission from Group
```
/mpgroup-removeperm <group> <permission>
```
**Example**: `/mpgroup-removeperm vip server.fly`

#### Set Parent Group
```
/mpgroup-setparent <group> <parent>
```
Makes a group inherit permissions from a parent group.  
**Example**: `/mpgroup-setparent vip default`

#### Set Group Priority
```
/mpgroup-setpriority <group> <number>
```
Sets priority for prefix/suffix display (higher = more important).  
**Example**: `/mpgroup-setpriority vip 50`

---

### 🔧 Utility Commands

#### Check Permission
```
/mpcheck <player> <permission>
```
Tests if a player has a specific permission.  
**Example**: `/mpcheck Steve server.fly`

#### Reload Configuration
```
/mpreload
```
Reloads all permissions from disk without restarting the server.

#### List Backups
```
/mpbackup-list
```
Shows all available configuration backups.

#### Restore Backup
```
/mpbackup-restore <backup_name>
```
Restores configuration from a backup file.  
**Example**: `/mpbackup-restore config_2026-01-15_14-30-00.json`

---

## 💡 Common Use Cases

### Creating a VIP Group

1. Create the group:
   ```
   /mpgroup-create vip
   ```

2. Set it to inherit from default group:
   ```
   /mpgroup-setparent vip default
   ```

3. Set a higher priority for chat:
   ```
   /mpgroup-setpriority vip 50
   ```

4. Add VIP permissions:
   ```
   /mpgroup-addperm vip server.fly
   /mpgroup-addperm vip server.speed
   ```

5. Set a colored prefix:
   ```
   Edit config.json manually to set prefix: "&6[VIP] "
   Then run: /mpreload
   ```

6. Add a player to VIP:
   ```
   /mpuser-add Steve vip
   ```

---

### Creating a Moderator Group

1. Create the group:
   ```
   /mpgroup-create moderator
   ```

2. Set moderate priority:
   ```
   /mpgroup-setpriority moderator 75
   ```

3. Add moderation permissions:
   ```
   /mpgroup-addperm moderator server.kick
   /mpgroup-addperm moderator server.ban
   /mpgroup-addperm moderator server.mute
   ```

4. Set prefix in config.json:
   ```json
   "prefix": "&9[Mod] "
   ```

5. Reload:
   ```
   /mpreload
   ```

---

### Giving Temporary Permissions

Give a player a specific permission without changing their group:
```
/mpuser-addperm Steve server.fly
```

Remove it later:
```
/mpuser-removeperm Steve server.fly
```

---

### Denying Specific Permissions

To deny a permission even if granted by wildcard:
```
/mpgroup-addperm builders -server.gamemode
```
This prevents the `server.gamemode` permission even if the group has `server.*`

---

## 🎨 Color Codes

Use these codes in prefixes and suffixes:

### Colors
- `&0` - Black
- `&1` - Dark Blue
- `&2` - Dark Green
- `&3` - Dark Cyan
- `&4` - Dark Red
- `&5` - Purple
- `&6` - Gold
- `&7` - Gray
- `&8` - Dark Gray
- `&9` - Blue
- `&a` - Green
- `&b` - Cyan
- `&c` - Red
- `&d` - Pink
- `&e` - Yellow
- `&f` - White

### Formats
- `&l` - **Bold**
- `&o` - *Italic*
- `&r` - Reset formatting

### Examples
- `&c[Admin] ` - Red "[Admin]" prefix
- `&6&l[VIP] ` - Bold gold "[VIP]" prefix
- `&a[Helper]&r ` - Green "[Helper]" with reset

---

## 🔍 Troubleshooting

### Player doesn't have expected permissions

1. Check player's groups:
   ```
   /mpuser-info PlayerName
   ```

2. Check if the group has the permission:
   ```
   /mpgroup-info groupname
   ```

3. Test the specific permission:
   ```
   /mpcheck PlayerName permission.node
   ```

4. Remember: Negated permissions (`-permission`) override granted permissions!

---

### Changes not applying

1. Try reloading the configuration:
   ```
   /mpreload
   ```

2. If that doesn't work, check the console for errors

3. Player might need to rejoin the server for changes to take full effect

---

### Prefix/Suffix not showing

1. Check group priority - higher priority groups override lower ones
2. Make sure the prefix/suffix is set in config.json
3. Reload after editing config.json:
   ```
   /mpreload
   ```

---

### Accidentally broke permissions

1. List available backups:
   ```
   /mpbackup-list
   ```

2. Restore from the most recent backup:
   ```
   /mpbackup-restore config_2026-01-15_14-30-00.json
   ```

3. The system automatically creates backups before each save!

---

## 📞 Need More Help?

- Configuration files are located in: `plugins/MyPermissions/`
- Backups are stored in: `plugins/MyPermissions/backups/`
- The system keeps the last 5 backups automatically
- Check server console for detailed error messages

---

**Required Permission**: `myperms.admin` - Needed for all MyPermissions commands

**Tip**: Always test permission changes with `/mpcheck` before applying them to many players!
