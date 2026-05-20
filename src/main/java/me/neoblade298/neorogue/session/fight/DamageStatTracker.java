package me.neoblade298.neorogue.session.fight;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DamageStatTracker extends StatTracker {
    protected DamageStatTracker(String id) {
        super(id);
    }

    protected DamageStatTracker(String id, Equipment eq, String sfx) {
        super(id, eq, sfx);
    }

    protected DamageStatTracker(String id, Component display) {
        super(id, display);
    }

    protected DamageStatTracker(String id, boolean ignored) {
        super(id);
    }

    public static DamageStatTracker of(String id, Equipment eq) {
        return new DamageStatTracker(id, eq, "Damage dealt");
    }

    public static DamageStatTracker thorns() {
        return new DamageStatTracker("thorns", Component.text("Thorns", NamedTextColor.GRAY));
    }

    public static DamageStatTracker reflect() {
        return new DamageStatTracker("reflect", Component.text("Reflect", NamedTextColor.GRAY));
    }

    public static DamageStatTracker fall() {
        return new DamageStatTracker("fall", Component.text("Fall", NamedTextColor.GRAY));
    }

    public static DamageStatTracker poison() {
        return new DamageStatTracker("poison", Component.text("Poison", NamedTextColor.GRAY));
    }

    public static DamageStatTracker electrified() {
        return new DamageStatTracker("electrified", Component.text("Electrified", NamedTextColor.GRAY));
    }

    public static DamageStatTracker of(String id, Equipment eq, String sfx) {
        return new DamageStatTracker(id, eq, sfx);
    }

    public static DamageStatTracker ignored(String id) {
        return new DamageStatTracker(id, true);
    }
}
