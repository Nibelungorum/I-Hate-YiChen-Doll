package github.ihatechpack.yichendoll.registry;

import github.ihatechpack.yichendoll.api.doll.DollBehavior;
import github.ihatechpack.yichendoll.common.doll.behavior.MXmDollBehavior;
import github.ihatechpack.yichendoll.util.LevelInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.LinkedList;
import java.util.List;

/**
 * @description: TODO
 * @author: HowXu
 * @date: 2026/11/6 09:50
 */
public class DollRegistery {

    public static final LinkedList<Doll> DOLLS = new LinkedList<>();
    private static final String prefix_tip = "item.ihateyichendoll.doll_tip.";

    static {
        addGod("howxu");
        addStreamer("m_xm_free");
        addStreamer("m_xm_", MXmDollBehavior.INSTANCE);
        addStreamer("budai_see_u");
        addStreamer("budai");
        addStreamer("qingjiao");
        addKernel("yuanxi_19");
    }

    private static void addGod(String id) {
        DOLLS.add(new Doll(id, LevelInfo.Level.God, list -> {
            list.add(Component.translatable(prefix_tip + id + ".0").withStyle(LevelInfo.getColor(LevelInfo.Level.God)));
            list.add(Component.translatable(prefix_tip + id + ".1").withStyle(LevelInfo.getColor(LevelInfo.Level.God)));
        }));
    }

    private static void addStreamer(String id) {
        addStreamer(id, DollBehavior.NONE);
    }

    private static void addStreamer(String id, DollBehavior behavior) {
        DOLLS.add(new Doll(id, LevelInfo.Level.Streamer,list -> {
            list.add(Component.translatable(prefix_tip + id + ".0").withStyle(LevelInfo.getColor(LevelInfo.Level.Streamer)));
            list.add(Component.translatable(prefix_tip + id + ".1").withStyle(LevelInfo.getColor(LevelInfo.Level.Streamer)));
        }, behavior));
    }

    private static void addKernel(String id) {
        DOLLS.add(new Doll(id, LevelInfo.Level.Kernel, list -> {
            list.add(Component.translatable(prefix_tip + id + ".0").withStyle(LevelInfo.getColor(LevelInfo.Level.Kernel)));
            list.add(Component.translatable(prefix_tip + id + ".1").withStyle(LevelInfo.getColor(LevelInfo.Level.Kernel)));
        }));
    }

    private static void addFan(String id) {
        DOLLS.add(new Doll(id, LevelInfo.Level.Fan, list -> {
            list.add(Component.translatable(prefix_tip + id + ".0").withStyle(LevelInfo.getColor(LevelInfo.Level.Fan)));
        }));
    }

    public record Doll(String id, LevelInfo.Level level, Descriptaion desc, DollBehavior behavior) {
        public Doll(String id, LevelInfo.Level level, Descriptaion desc) {
            this(id, level, desc, DollBehavior.NONE);
        }

        public void appendHoverText(List<Component> tooltipComponents) {
            desc.appendHoverText(tooltipComponents);
        }
    }

    @FunctionalInterface
    interface Descriptaion {
        void appendHoverText(List<Component> tooltipComponents);
    }
}
