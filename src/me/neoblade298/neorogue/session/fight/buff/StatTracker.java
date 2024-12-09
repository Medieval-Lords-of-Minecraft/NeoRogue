package me.neoblade298.neorogue.session.fight.buff;

import java.util.HashMap;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StatTracker {
    private Component display;
    private String id;

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
            statusOrigins.put(type, new StatTracker(type, false));
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

    public static StatTracker of(StatusType type) {
        return statusOrigins.get(type);
    }
    
    private StatTracker(Equipment eq, String sfx) {
        this.id = eq.getId();
        this.display = eq.getDisplay().append(Component.text(" - " + sfx, NamedTextColor.GRAY));
    }

    public boolean isSimilar(StatTracker other) {
        return this.id.equals(other.id);
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
}
