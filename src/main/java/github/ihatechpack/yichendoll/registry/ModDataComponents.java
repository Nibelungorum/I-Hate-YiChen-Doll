package github.ihatechpack.yichendoll.registry;

import com.mojang.serialization.Codec;
import github.ihatechpack.yichendoll.IHateYiChenDoll;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Persistent item data owned by this mod, even when optional integrations are absent. */
public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> REGISTER =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, IHateYiChenDoll.MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY =
            REGISTER.register("energy", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, 1_000_000)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> ME_LINK =
            REGISTER.register("me_link", () -> DataComponentType.<GlobalPos>builder()
                    .persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> COSMIC_WINGS_VISIBLE =
            REGISTER.register("cosmic_wings_visible", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> COSMIC_WINGS_DYE =
            REGISTER.register("cosmic_wings_dye", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BUDAI_SHIELD_VISIBLE =
            REGISTER.register("budai_shield_visible", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

    private ModDataComponents() {}
}
