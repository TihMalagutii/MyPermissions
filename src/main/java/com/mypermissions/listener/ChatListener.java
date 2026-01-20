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
     * Converts color codes (&0-9, &a-f, &l, &o, &r) into Message with colors
     * 
     * Supported codes:
     * &0 = Black, &1 = Dark Blue, &2 = Dark Green, &3 = Dark Cyan
     * &4 = Dark Red, &5 = Purple, &6 = Gold, &7 = Gray
     * &8 = Dark Gray, &9 = Blue, &a = Green, &b = Cyan
     * &c = Red, &d = Pink, &e = Yellow, &f = White
     * &l = Bold, &o = Italic, &r = Reset
     */
    private static Message parseColors(@Nonnull String text) {
        Message result = Message.raw("");
        
        // Process text character by character
        int i = 0;
        Color currentColor = null;
        boolean bold = false;
        boolean italic = false;
        StringBuilder currentText = new StringBuilder();
        
        while (i < text.length()) {
            if (text.charAt(i) == '&' && i + 1 < text.length()) {
                // Found a color code
                char code = Character.toLowerCase(text.charAt(i + 1));
                
                // Check if it's a valid color code
                if ("0123456789abcdef".indexOf(code) != -1 || "lor".indexOf(code) != -1) {
                    // Flush current text with accumulated formatting
                    if (currentText.length() > 0) {
                        Message segment = Message.raw(currentText.toString());
                        if (currentColor != null) segment.color(currentColor);
                        if (bold) segment.bold(true);
                        if (italic) segment.italic(true);
                        result.insert(segment);
                        currentText = new StringBuilder();
                    }
                    
                    // Apply new formatting
                    switch (code) {
                        // Colors
                        case '0' -> currentColor = new Color(0, 0, 0);           // Black
                        case '1' -> currentColor = new Color(0, 0, 170);         // Dark Blue
                        case '2' -> currentColor = new Color(0, 170, 0);         // Dark Green
                        case '3' -> currentColor = new Color(0, 170, 170);       // Dark Cyan
                        case '4' -> currentColor = new Color(170, 0, 0);         // Dark Red
                        case '5' -> currentColor = new Color(170, 0, 170);       // Purple
                        case '6' -> currentColor = new Color(255, 170, 0);       // Gold
                        case '7' -> currentColor = new Color(170, 170, 170);     // Gray
                        case '8' -> currentColor = new Color(85, 85, 85);        // Dark Gray
                        case '9' -> currentColor = new Color(85, 85, 255);       // Blue
                        case 'a' -> currentColor = new Color(85, 255, 85);       // Green
                        case 'b' -> currentColor = new Color(85, 255, 255);      // Cyan
                        case 'c' -> currentColor = new Color(255, 85, 85);       // Red
                        case 'd' -> currentColor = new Color(255, 85, 255);      // Pink
                        case 'e' -> currentColor = new Color(255, 255, 85);      // Yellow
                        case 'f' -> currentColor = new Color(255, 255, 255);     // White
                        
                        // Formats
                        case 'l' -> bold = true;                                 // Bold
                        case 'o' -> italic = true;                               // Italic
                        case 'r' -> {                                            // Reset
                            currentColor = null;
                            bold = false;
                            italic = false;
                        }
                    }
                    
                    i += 2; // Skip the & and the code
                    continue;
                }
            }
            
            // Regular character
            currentText.append(text.charAt(i));
            i++;
        }
        
        // Flush remaining text
        if (currentText.length() > 0) {
            Message segment = Message.raw(currentText.toString());
            if (currentColor != null) segment.color(currentColor);
            if (bold) segment.bold(true);
            if (italic) segment.italic(true);
            result.insert(segment);
        }
        
        return result;
    }
}
