package github.ihatechpack.yichendoll.integration.ae2lt;

import github.ihatechpack.yichendoll.common.doll.behavior.MXmDollBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Supplier;

public final class CelestweaveBypass {
    private static final ThreadLocal<Attack> CURRENT = new ThreadLocal<>();

    private CelestweaveBypass() {}

    public interface DeathState {
        boolean yichendoll$hasDied();
        boolean yichendoll$isBypassedDeath();
        void yichendoll$setBypassedDeath(boolean bypassed);
    }

    private record Attack(LivingEntity target, boolean piercing, Attack previous) {}

    public static boolean isPiercing(LivingEntity target, DamageSource source) {
        return source.getEntity() != target && MXmDollBehavior.isWearing(source.getEntity());
    }

    public static boolean shouldBypass(LivingEntity target) {
        if (!(target instanceof ServerPlayer)) return false;
        for (Attack attack = CURRENT.get(); attack != null; attack = attack.previous()) {
            if (attack.target() == target) return attack.piercing();
        }
        DeathState state = (DeathState) target;
        if (!target.isDeadOrDying()) {
            state.yichendoll$setBypassedDeath(false);
            return false;
        }
        return state.yichendoll$isBypassedDeath();
    }

    public static <T> T duringDamage(LivingEntity target, DamageSource source, Supplier<T> action) {
        // A new damage source must not inherit piercing from a nested, unrelated attack.
        return duringAttack(target, isPiercing(target, source), action);
    }

    public static <T> void duringDeath(LivingEntity target, DamageSource source, Supplier<T> action) {
        boolean piercing = isPiercing(target, source) || shouldBypass(target);
        duringAttack(target, piercing, () -> {
            try {
                return action.get();
            } finally {
                // Only an actual death retains the decision for delayed undying checks.
                // A canceled death or a successful totem must not disable future protection.
                if (target instanceof ServerPlayer) {
                    DeathState state = (DeathState) target;
                    state.yichendoll$setBypassedDeath(piercing && state.yichendoll$hasDied());
                }
            }
        });
    }

    public static <T> T duringAttack(LivingEntity target, boolean piercing, Supplier<T> action) {
        if (!(target instanceof ServerPlayer)) return action.get();
        Attack previous = CURRENT.get();
        CURRENT.set(new Attack(target, piercing, previous));
        try {
            return action.get();
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }
}
