package com.mypermissions.listener;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.mypermissions.Main;
import com.mypermissions.manager.PermissionManager;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener {
    
    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fk-or])");
    
    public static void register(@Nonnull JavaPlugin plugin) {
        plugin.getEventRegistry().registerGlobal(PlayerChatEvent.class, ChatListener::onPlayerChat);
    }
    
    private static void onPlayerChat(@Nonnull PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        String uuid = sender.getUuid().toString();
        
        PermissionManager permManager = Main.getPermissionManager();
        String prefix = permManager.getUserPrefix(uuid);
        String suffix = permManager.getUserSuffix(uuid);
        
        // Create custom formatter with prefix/suffix
        event.setFormatter((playerRef, message) -> {
            Message result = Message.raw("");
            
            // Add prefix with colors
            if (prefix != null && !prefix.isEmpty()) {
                result.insert(parseColors(prefix));
            }
            
            // Add player name
            result.insert(Message.raw(playerRef.getUsername()).color(Color.WHITE));
            
            // Add suffix with colors
            if (suffix != null && !suffix.isEmpty()) {
                result.insert(parseColors(suffix));
            }
            
            // Add message
            result.insert(Message.raw(": " + message).color(Color.WHITE));
            
            return result;
        });
    }
    
    /**
     * Converts color codes (&0-9, &a-f, &l, &r) into Message with colors
     * 
     * Supported codes:
     * &0 = Black, &1 = Dark Blue, &2 = Dark Green, &3 = Dark Cyan
     * &4 = Dark Red, &5 = Purple, &6 = Gold, &7 = Gray
     * &8 = Dark Gray, &9 = Blue, &a = Green, &b = Cyan
     * &c = Red, &d = Pink, &e = Yellow, &f = White
     * &l = Bold, &r = Reset
     */
    private static Message parseColors(@Nonnull String text) {
        Message result = Message.raw("");
        
        Matcher matcher = COLOR_PATTERN.matcher(text);
        int lastEnd = 0;
        
        while (matcher.find()) {
            // Add text before the color code
            if (matcher.start() > lastEnd) {
                String beforeColor = text.substring(lastEnd, matcher.start());
                result.insert(Message.raw(beforeColor));
            }
            
            // Apply color/format
            String code = matcher.group(1).toLowerCase();
            Message segment = Message.raw("");
            
            switch (code) {
                // Colors
                case "0" -> segment.color(new Color(0, 0, 0));           // Black
                case "1" -> segment.color(new Color(0, 0, 170));         // Dark Blue
                case "2" -> segment.color(new Color(0, 170, 0));         // Dark Green
                case "3" -> segment.color(new Color(0, 170, 170));       // Dark Cyan
                case "4" -> segment.color(new Color(170, 0, 0));         // Dark Red
                case "5" -> segment.color(new Color(170, 0, 170));       // Purple
                case "6" -> segment.color(new Color(255, 170, 0));       // Gold
                case "7" -> segment.color(new Color(170, 170, 170));     // Gray
                case "8" -> segment.color(new Color(85, 85, 85));        // Dark Gray
                case "9" -> segment.color(new Color(85, 85, 255));       // Blue
                case "a" -> segment.color(new Color(85, 255, 85));       // Green
                case "b" -> segment.color(new Color(85, 255, 255));      // Cyan
                case "c" -> segment.color(new Color(255, 85, 85));       // Red
                case "d" -> segment.color(new Color(255, 85, 255));      // Pink
                case "e" -> segment.color(new Color(255, 255, 85));      // Yellow
                case "f" -> segment.color(new Color(255, 255, 255));     // White
                
                // Formats
                case "l" -> segment.bold(true);                          // Bold
                case "o" -> segment.italic(true);                        // Italic
                case "r" -> segment = Message.raw("");                   // Reset
            }
            
            // Find next color code or end of text
            int nextColorStart = text.indexOf("&", matcher.end());
            if (nextColorStart == -1) {
                nextColorStart = text.length();
            }
            
            // Add text with the applied color/format
            if (matcher.end() < nextColorStart) {
                String coloredText = text.substring(matcher.end(), nextColorStart);
                segment.insert(Message.raw(coloredText));
                result.insert(segment);
            }
            
            lastEnd = nextColorStart;
        }
        
        // Add remaining text
        if (lastEnd < text.length()) {
            result.insert(Message.raw(text.substring(lastEnd)));
        }
        
        return result;
    }
}
