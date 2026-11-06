package github.ihatechpack.yichendoll.api.doll;

/** Server-side behavior, dispatched once per player and equipped doll type each tick. */
public interface DollBehavior {
    DollBehavior NONE = new DollBehavior() {};

    /** Called when the first active copy is equipped. */
    default void onEquip(DollContext context) {}

    /** Called when the last active copy is removed; context describes the previous equipment. */
    default void onUnequip(DollContext context) {}

    default void tick(DollContext context) {}
}
