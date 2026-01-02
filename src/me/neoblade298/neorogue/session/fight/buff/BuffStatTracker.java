package me.neoblade298.neorogue.session.fight.buff;

import java.util.UUID;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BuffStatTracker extends StatTracker {
    /* If true, any buffs with the same id will be combined instead of replaced
        Useful for statuses like strength where you have multiple sources of the same strength at a time
        instead of replacing them */
    private boolean shouldCombine = false;
                                
    protected BuffStatTracker(String id, Component display, boolean invert, boolean shouldCombine) {
        super(id, display, invert);
        this.shouldCombine = shouldCombine;
    }
                                
    protected BuffStatTracker(String id, Component display, boolean invert) {
        super(id, display, invert);
    }

    protected BuffStatTracker(String id, Equipment eq, String sfx) {
        super(id, eq, sfx);
    }
    
    protected BuffStatTracker(String id, Equipment eq, String sfx, boolean shouldCombine) {
        super(id, eq, sfx);
        this.shouldCombine = shouldCombine;
    }
    
    protected BuffStatTracker(String id, Equipment eq, String sfx, boolean invert, boolean shouldCombine) {
        super(id, eq, sfx, invert);
        this.shouldCombine = shouldCombine;
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
    
    protected BuffStatTracker(String id, Equipment eq) {
        super(id, eq);
    }

    public static BuffStatTracker of(String id, Equipment eq, String statTitle) {
        return new BuffStatTracker(id, eq, statTitle, false);
    }

    public static BuffStatTracker of(String id, Equipment eq, String statTitle, boolean shouldCombine) {
        return new BuffStatTracker(id, eq, statTitle, shouldCombine);
    }

    public static BuffStatTracker ignored(String id) {
        return new BuffStatTracker(id);
    }

    public static BuffStatTracker ignored(String id, Equipment eq) {
        return new BuffStatTracker(id, eq);
    }

    public static BuffStatTracker ignored(Equipment eq) {
        return new BuffStatTracker(UUID.randomUUID().toString(), eq);
    }

    public boolean shouldCombine() {
        return shouldCombine;
    }
}
