package github.ihatechpack.yichendoll.registry;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The modifier system owns values; damage hooks only implement their combat semantics. */
@EventBusSubscriber(modid = IHateYiChenDoll.MOD_ID)
public final class ModAttributes {
    public static final DeferredRegister<Attribute> REGISTER =
            DeferredRegister.create(Registries.ATTRIBUTE, IHateYiChenDoll.MOD_ID);
    public static final DeferredHolder<Attribute, Attribute> ALL_DAMAGE = register("all_damage", Integer.MAX_VALUE);
    public static final DeferredHolder<Attribute, Attribute> INVULNERABILITY_TICKS = register("invulnerability_ticks", Integer.MAX_VALUE);
    public static final DeferredHolder<Attribute, Attribute> DAMAGE_REDUCTION = register("damage_reduction", 100);

    private ModAttributes() {}

    private static DeferredHolder<Attribute, Attribute> register(String name, double max) {
        return REGISTER.register(name, () -> new RangedAttribute(
                "attribute.ihateyichendoll." + name, 0, 0, max).setSyncable(true));
    }

    @SubscribeEvent
    public static void addPlayerAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ALL_DAMAGE);
        event.add(EntityType.PLAYER, INVULNERABILITY_TICKS);
        event.add(EntityType.PLAYER, DAMAGE_REDUCTION);
    }

    public static void update(ServerPlayer player, Holder<Attribute> type, ResourceLocation id, double amount) {
        var attribute = player.getAttribute(type);
        if (attribute == null) return;
        var previous = attribute.getModifier(id);
        if (amount <= 0 || !Double.isFinite(amount)) {
            if (previous != null) attribute.removeModifier(id);
        } else if (previous == null || Double.compare(previous.amount(), amount) != 0) {
            attribute.addOrUpdateTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
