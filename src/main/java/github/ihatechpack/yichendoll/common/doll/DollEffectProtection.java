package github.ihatechpack.yichendoll.common.doll;

import github.ihatechpack.yichendoll.integration.curios.CuriosBridge;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Equipment queries shared by the specific effect-impact hooks, never global hasEffect. */
public final class DollEffectProtection {
    private DollEffectProtection() {}

    public static boolean isWearing(LivingEntity entity, String id) {
        if (!(entity instanceof Player)) return false;
        return entity instanceof ServerPlayer ? DollRuntime.isWearing(entity, id)
                : !CuriosBridge.get().activeStacks(entity, id).isEmpty();
    }

    public static boolean ignores(LivingEntity entity, Holder<MobEffect> effect) {
        return (effect == MobEffects.BLINDNESS || effect == MobEffects.DARKNESS || effect == MobEffects.CONFUSION)
                && isWearing(entity, "yuanxi_19_new");
    }
}
