package me.neoblade298.neorogue.session.fight.buff;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BuffStatTracker extends StatTracker {
    /* If true, any buffs with the same id will be combined instead of replaced
        Useful for statuses like strength where you have multiple sources of the same strength at a time
        instead of replacing them */
    private boolean shouldCombine;
                                
    protected BuffStatTracker(String id, Component display, boolean invert, boolean shouldCombine) {
        super(id, display, invert);
        this.shouldCombine = shouldCombine;
    }
    
    protected BuffStatTracker(Equipment eq, String sfx) {
        super(eq, sfx);
        this.shouldCombine = false;
    }

    protected BuffStatTracker(StatusType type, boolean damage) {
        this(type.name(), type.ctag.append(
            Component.text(damage ? " - Damage Buffed" : " - Damage Mitigated", NamedTextColor.GRAY)), false, true);
    }

    protected BuffStatTracker(StatusType type, boolean damage, boolean invert) {
        this(type.name(), type.ctag.append(
            Component.text(damage ? " - Damage Buffed" : " - Damage Mitigated", NamedTextColor.GRAY)), invert, true);
    }
    
    protected BuffStatTracker(String id) {
        super(id);
    }
    
    protected BuffStatTracker(Equipment eq) {
        super(eq);
    }

    public static BuffStatTracker of(Equipment eq, String statTitle) {
        return new BuffStatTracker(eq, statTitle);
    }

    public static BuffStatTracker ignored(String id) {
        return new BuffStatTracker(id);
    }

    public static BuffStatTracker ignored(Equipment eq) {
        return new BuffStatTracker(eq);
    }

    public boolean shouldCombine() {
        return shouldCombine;
    }
}
