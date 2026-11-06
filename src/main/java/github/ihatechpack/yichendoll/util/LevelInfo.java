package github.ihatechpack.yichendoll.util;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;

/**
 * @description: TODO
 * @author: HowXu
 * @date: 2026/11/6 09:51
 */
public class LevelInfo {
    public static ChatFormatting getColor(String id){
        // this is dev only function
        return Level.valueOf(id).color;
    }

    public static ChatFormatting getColor(Level level){
        // this is dev only function
        return level.color;
    }

    public static Rarity getRarity(String id){
        return switch (id) {
            case "god" -> Rarity.EPIC;
            case "streamer" -> Rarity.RARE;
            case "kernel" -> Rarity.UNCOMMON;
            default -> Rarity.COMMON;
        };
    }

    public static Rarity getRarity(Level level){
        return switch (level) {
            case God -> Rarity.EPIC;
            case Streamer -> Rarity.RARE;
            case Kernel -> Rarity.UNCOMMON;
            default -> Rarity.COMMON;
        };
    }

    public enum Level{
        God("god", ChatFormatting.GOLD),
        Streamer("streamer",ChatFormatting.DARK_PURPLE),
        Kernel("kernel",ChatFormatting.LIGHT_PURPLE),
        Fan("fan",ChatFormatting.GRAY);


        final String id;
        final ChatFormatting color;
        Level(String id, ChatFormatting color){
            this.id = id;
            this.color = color;
        }
    }
}
