package github.ihatechpack.yichendoll.common.doll.behavior;

import github.ihatechpack.yichendoll.Res;
import github.ihatechpack.yichendoll.api.doll.DollBehavior;
import github.ihatechpack.yichendoll.api.doll.DollContext;
import github.ihatechpack.yichendoll.integration.ae2.AE2Bridge;
import github.ihatechpack.yichendoll.integration.ae2.MeNetworkStats;
import github.ihatechpack.yichendoll.registry.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.ToDoubleFunction;

/** Shared refresh and modifier lifecycle for the four network-linked doll behaviors. */
public final class MeDollBehavior implements DollBehavior {
    public static final MeDollBehavior M_XM = new MeDollBehavior("m_xm_", ModAttributes.ALL_DAMAGE, MeNetworkStats::usage);
    public static final MeDollBehavior BUDAI = new MeDollBehavior("budai", ModAttributes.INVULNERABILITY_TICKS, MeNetworkStats::keyTypes);
    public static final MeDollBehavior BUDAI_SEE_U = new MeDollBehavior("budai_see_u", ModAttributes.DAMAGE_REDUCTION,
            stats -> Math.min(100, stats.nodes()));
    public static final MeDollBehavior YUANXI = new MeDollBehavior("yuanxi_19", Attributes.MAX_HEALTH,
            stats -> stats.storedFE() / 1_000_000_00d);

    private final ResourceLocation modifier;
    private final Holder<Attribute> attribute;
    private final ToDoubleFunction<MeNetworkStats> value;

    private MeDollBehavior(String id, Holder<Attribute> attribute, ToDoubleFunction<MeNetworkStats> value) {
        this.modifier = Res.rl("me/" + id);
        this.attribute = attribute;
        this.value = value;
    }

    @Override
    public void onEquip(DollContext context) {
        update(context);
    }

    @Override
    public void onUnequip(DollContext context) {
        ModAttributes.update(context.player(), attribute, modifier, 0);
        clampHealth(context);
    }

    @Override
    public void tick(DollContext context) {
        if (context.player().getServer().getTickCount() % 40 != 0) return;
        update(context);
        if (attribute == Attributes.MAX_HEALTH) heal(context);
    }

    private void update(DollContext context) {
        double highest = 0;
        for (var stack : context.stacks()) {
            highest = Math.max(highest, value.applyAsDouble(AE2Bridge.get().sample(context.player(), stack)));
        }
        ModAttributes.update(context.player(), attribute, modifier, Math.min(Integer.MAX_VALUE, highest));
        clampHealth(context);
    }

    private void heal(DollContext context) {
        var player = context.player();
        if (!player.isAlive() || player.getHealth() >= player.getMaxHealth()) return;
        long cost = (long) Math.ceil((player.getMaxHealth() - (double) player.getHealth()) * 100);
        for (var stack : context.stacks()) {
            if (AE2Bridge.get().consumeFE(player, stack, cost)) {
                player.setHealth(player.getMaxHealth());
                return;
            }
        }
    }

    private void clampHealth(DollContext context) {
        if (attribute == Attributes.MAX_HEALTH && context.player().getHealth() > context.player().getMaxHealth()) {
            context.player().setHealth(context.player().getMaxHealth());
        }
    }
}
