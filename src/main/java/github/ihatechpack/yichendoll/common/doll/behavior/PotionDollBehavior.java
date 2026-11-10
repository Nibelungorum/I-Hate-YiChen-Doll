package github.ihatechpack.yichendoll.common.doll.behavior;

import github.ihatechpack.yichendoll.api.doll.DollBehavior;
import github.ihatechpack.yichendoll.api.doll.DollContext;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Supplements missing effects without taking ownership of effects already present. */
public enum PotionDollBehavior implements DollBehavior {
    YITIAOTIANYU("yitiaotianyu", List.of(MobEffects.DOLPHINS_GRACE, MobEffects.CONDUIT_POWER, MobEffects.LUCK)),
    SUIFENGYAO("suifengyao", List.of(MobEffects.GLOWING, MobEffects.NIGHT_VISION)),
    CNANSZ("cnansz", List.of(MobEffects.FIRE_RESISTANCE, MobEffects.SATURATION));

    private static final String OWNERSHIP = "ihateyichendoll_potion_ownership";
    private final String id;
    private final List<Holder<MobEffect>> effects;
    private final Map<UUID, Map<Holder<MobEffect>, MobEffectInstance>> owned = new HashMap<>();

    PotionDollBehavior(String id, List<Holder<MobEffect>> effects) {
        this.id = id;
        this.effects = effects;
    }

    @Override
    public void onEquip(DollContext context) {
        supplement(context.player());
    }

    @Override
    public void tick(DollContext context) {
        if (context.player().getServer().getTickCount() % 40 == 0) supplement(context.player());
    }

    private void supplement(ServerPlayer player) {
        var records = owned.computeIfAbsent(player.getUUID(), key -> new HashMap<>());
        for (var effect : effects) {
            var recorded = records.get(effect);
            if (recorded != null && player.getEffect(effect) != recorded) records.remove(effect);
            if (player.hasEffect(effect)) continue;
            var instance = new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION,
                    MobEffectInstance.MAX_AMPLIFIER, false, false, true);
            if (player.addEffect(instance) && player.getEffect(effect) == instance) records.put(effect, instance);
        }
        writeMarker(player, records);
        if (records.isEmpty()) owned.remove(player.getUUID());
    }

    @Override
    public void onUnequip(DollContext context) {
        var player = context.player();
        var records = owned.remove(player.getUUID());
        if (records != null) records.forEach((effect, instance) -> {
            if (player.getEffect(effect) == instance && matches(instance)) player.removeEffect(effect);
        });
        writeMarker(player, Map.of());
    }

    private static boolean matches(MobEffectInstance instance) {
        return instance != null && instance.isInfiniteDuration()
                && instance.getAmplifier() == MobEffectInstance.MAX_AMPLIFIER
                && !instance.isAmbient() && !instance.isVisible() && instance.showIcon();
    }

    private void writeMarker(ServerPlayer player, Map<Holder<MobEffect>, MobEffectInstance> records) {
        var data = player.getPersistentData();
        var marker = data.getCompound(OWNERSHIP);
        int mask = 0;
        for (int i = 0; i < effects.size(); i++) {
            var instance = records.get(effects.get(i));
            if (instance != null && player.getEffect(effects.get(i)) == instance && matches(instance)) mask |= 1 << i;
        }
        if (mask == 0) marker.remove(id); else marker.putInt(id, mask);
        if (marker.isEmpty()) data.remove(OWNERSHIP); else data.put(OWNERSHIP, marker);
    }

    /** Saved ownership survives logout/save ordering; object identity does not survive loading. */
    public static void restore(ServerPlayer player) {
        var data = player.getPersistentData();
        var marker = data.getCompound(OWNERSHIP);
        for (var behavior : values()) {
            behavior.owned.remove(player.getUUID());
            int mask = marker.getInt(behavior.id);
            for (int i = 0; i < behavior.effects.size(); i++) {
                var effect = behavior.effects.get(i);
                if ((mask & (1 << i)) != 0 && matches(player.getEffect(effect))) player.removeEffect(effect);
            }
        }
        data.remove(OWNERSHIP);
    }

    /** Used at vanilla's clone copy call, before the original player's ownership is forgotten. */
    public static boolean ownsEffect(ServerPlayer player, MobEffectInstance instance) {
        if (!matches(instance) || player.getEffect(instance.getEffect()) != instance) return false;
        for (var behavior : values()) {
            var records = behavior.owned.get(player.getUUID());
            if (records != null && records.get(instance.getEffect()) == instance) return true;
        }
        return false;
    }

    /** Reconcile live ownership before serialization; saving must not change active effects. */
    public static void beforeSave(ServerPlayer player) {
        for (var behavior : values()) {
            var records = behavior.owned.get(player.getUUID());
            if (records == null) {
                behavior.writeMarker(player, Map.of());
                continue;
            }
            records.entrySet().removeIf(entry -> player.getEffect(entry.getKey()) != entry.getValue()
                    || !matches(entry.getValue()));
            behavior.writeMarker(player, records);
            if (records.isEmpty()) behavior.owned.remove(player.getUUID());
        }
    }

    public static void clearAll() {
        for (var behavior : values()) behavior.owned.clear();
    }
}
