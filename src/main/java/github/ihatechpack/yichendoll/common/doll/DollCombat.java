package github.ihatechpack.yichendoll.common.doll;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.registry.ModAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Applies outgoing damage, vanilla post-hit cooldown, and final incoming damage reduction. */
@EventBusSubscriber(modid = IHateYiChenDoll.MOD_ID)
public final class DollCombat {
    private DollCombat() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void outgoing(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer target) {
            double extra = target.getAttributeValue(ModAttributes.INVULNERABILITY_TICKS);
            if (extra > 0) event.setInvulnerabilityTicks((int) Math.min(Integer.MAX_VALUE, 20d + extra));
        }
        if (event.getSource().getEntity() instanceof ServerPlayer player
                && player != event.getEntity() && event.getAmount() > 0) {
            double bonus = player.getAttributeValue(ModAttributes.ALL_DAMAGE);
            if (bonus > 0) event.setAmount((float) Math.min(Float.MAX_VALUE, event.getAmount() + bonus));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void incoming(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        double reduction = player.getAttributeValue(ModAttributes.DAMAGE_REDUCTION);
        if (reduction > 0) {
            event.setNewDamage((float) Math.max(0, event.getNewDamage() * (1 - Math.min(100, reduction) / 100)));
        }
    }
}
