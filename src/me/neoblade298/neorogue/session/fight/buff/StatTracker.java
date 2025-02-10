package me.neoblade298.neorogue.session.fight.buff;

import java.util.HashMap;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StatTracker {
    private Component display;
    private String id;
    private boolean invert; // Quick easy way to invert the stat number

    private static HashMap<StatusType, StatTracker> statusOrigins = new HashMap<StatusType, StatTracker>();
    public static StatTracker IGNORED = new StatTracker(null, (Component) null);

    static {
        StatusType[] damageStatuses = new StatusType[] { StatusType.STRENGTH, StatusType.INTELLECT,
            StatusType.INSANITY };
        StatusType[] defenseStatuses = new StatusType[] { StatusType.FROST, StatusType.CONCUSSED,
            StatusType.INJURY, StatusType.PROTECT, StatusType.SHELL };
        for (StatusType type : damageStatuses) {
            statusOrigins.put(type, new StatTracker(type, true));
        }
        for (StatusType type : defenseStatuses) {
            statusOrigins.put(type, new StatTracker(type, false, true));
        }
    }
    
    private StatTracker(String id, Component display) {
        this.display = display;
        this.id = id;
    }

    private StatTracker(StatusType type, boolean damage) {
        this(type.name(), type.ctag.append(
            Component.text(damage ? " - Damage Buffed" : " - Damage Mitigated", NamedTextColor.GRAY)));
    }

    private StatTracker(StatusType type, boolean damage, boolean invert) {
        this(type.name(), type.ctag.append(
            Component.text(damage ? " - Damage Buffed" : " - Damage Mitigated", NamedTextColor.GRAY)));
        this.invert = invert;
    }

    public static StatTracker of(StatusType type) {
        return statusOrigins.get(type);
    }

    public boolean isSimilar(StatTracker other) {
        return this.id != null && this.id.equals(other.id);
    }

    public boolean isInverted() {
        return invert;
    }
    
    private StatTracker(Equipment eq, String sfx) {
        this.id = eq.getId();
        this.display = eq.getDisplay().append(Component.text(" - " + sfx, NamedTextColor.GRAY));
    }

    public static StatTracker of(Equipment eq, String statTitle) {
        return new StatTracker(eq, statTitle);
    }

    public static StatTracker damageBuffAlly(Equipment eq) {
        return new StatTracker(eq, "Damage Buffed");
    }

    public static StatTracker damageDebuffAlly(Equipment eq) {
        return new StatTracker(eq, "Damage Reduced");
    }

    public static StatTracker defenseBuffAlly(Equipment eq) {
        return new StatTracker(eq, "Damage Mitigated");
    }

    public static StatTracker damageDebuffEnemy(Equipment eq) {
        return new StatTracker(eq, "Damage Mitigated");
    }

    public static StatTracker defenseDebuffEnemy(Equipment eq) {
        return new StatTracker(eq, "Defense Debuffed");
    }

    public static StatTracker damageBarriered(Equipment eq) {
        return new StatTracker(eq, "Damage Barriered");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((display == null) ? 0 : display.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatTracker other = (StatTracker) obj;
        if (display == null) {
            if (other.display != null)
                return false;
        } else if (!display.equals(other.display))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public Component getDisplay() {
        return display;
    }

    
}
