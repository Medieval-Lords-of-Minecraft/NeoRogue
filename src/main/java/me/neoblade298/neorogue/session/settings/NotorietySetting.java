package me.neoblade298.neorogue.session.settings;

import java.util.ArrayList;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class NotorietySetting {
    public static final ArrayList<NotorietySetting> settings = new ArrayList<>();

    public static final double BOSS_HEAL_MULTIPLIER = 0.75;
    public static final NotorietySetting REDUCED_BOSS_HEAL = new NotorietySetting(
            Component.text("Only heal ").color(NamedTextColor.GRAY)
                    .append(Component.text("75%").color(NamedTextColor.YELLOW))
                    .append(Component.text(" of missing health after boss fights").color(NamedTextColor.GRAY)),
            Component.text("Powerful enemies leave more lasting injuries."));

    public static final double REDUCE_COINS_MULTIPLIER = 0.7;
    public static final NotorietySetting REDUCE_COINS = new NotorietySetting(
        Component.text("Enemies drop ")
            .color(NamedTextColor.GRAY)
            .append(Component.text("30%").color(NamedTextColor.YELLOW))
            .append(Component.text(" less coins").color(NamedTextColor.GRAY)),
        Component.text("Your caravan is a promising payday for the impoverished.")
    );

    public static final NotorietySetting LOWER_UPGRADE_CHANCE = new NotorietySetting(
        Component.text("Equipment has a lower chance to be upgraded")
            .color(NamedTextColor.GRAY),
        Component.text("The caravan demands higher quality equipment be sold instead of used.")
    );

    public static final double BREAKABLE_CHANCE = 0.2;
    public static final int BREAKABLE_DURABILITY = 7;
    public static final NotorietySetting BREAKABLE_EQUIPMENT = new NotorietySetting(
            Component.text("Equipment has a chance to spawn breakable, meaning it will break after ").color(NamedTextColor.GRAY)
                    .append(Component.text(BREAKABLE_DURABILITY).color(NamedTextColor.YELLOW))
                    .append(Component.text(" fights").color(NamedTextColor.GRAY)),
            Component.text("Enemies strategize towards breaking your gear."));

    public static final double SCORE_THRESHOLD_MULTIPLIER = 0.8;
    public static final NotorietySetting REDUCED_SCORE_THRESHOLDS = new NotorietySetting(
            Component.text("Fight score time thresholds reduced by ").color(NamedTextColor.GRAY)
                    .append(Component.text("20%").color(NamedTextColor.YELLOW))
                    .append(Component.text(" after first boss").color(NamedTextColor.GRAY)),
            Component.text("You have tighter deadlines to reach the market."));

    public static final NotorietySetting BOSS_RANDOM_MODIFIER = new NotorietySetting(
        Component.text("Minibosses and bosses have a random modifier").color(NamedTextColor.GRAY),
        Component.text("Dangerous enemies put everything on the line to defeat you."));
   
    public static final double INCREASE_DAMAGE_MULTIPLIER = 1.3;
    public static final NotorietySetting INCREASE_DAMAGE = new NotorietySetting(
            Component.text("Increase enemy damage by ").color(NamedTextColor.GRAY)
                    .append(Component.text("20%").color(NamedTextColor.YELLOW)),
            Component.text("Your caravan is attracting more dangerous enemies."));

    public static final double INCREASE_HEALTH_MULTIPLIER = 1.35;
    public static final NotorietySetting INCREASE_HEALTH = new NotorietySetting(
            Component.text("Increase enemy health by ").color(NamedTextColor.GRAY)
                    .append(Component.text("25%").color(NamedTextColor.YELLOW)),
            Component.text("Enemies prepare better armor as your reputation spreads."));

    public static final NotorietySetting LESS_SLOTS = new NotorietySetting(
            Component.text("Start with ").color(NamedTextColor.GRAY)
                    .append(Component.text("1").color(NamedTextColor.YELLOW))
                    .append(Component.text(" less accessory slot and armor slot").color(NamedTextColor.GRAY)),
            Component.text("The caravan requires you to carry more tradeable items and fewer combat items."));

    public static final NotorietySetting REFORGE_REQUIRES_BOTH = new NotorietySetting(
        Component.text("Reforges require both materials to be upgraded")
            .color(NamedTextColor.GRAY),
        Component.text("Power spent on reforging is instead spent moving the caravan.")
    );

    private final int level;
    private final ArrayList<TextComponent> header, lore;

    public NotorietySetting(TextComponent header, TextComponent lore) {
        this.header = SharedUtil.addLineBreaks(header, 160);
        this.lore = SharedUtil.addLineBreaks(lore, 160);
        settings.add(this);
        this.level = settings.size();
    }

    public ArrayList<TextComponent> getHeader() {
        return header;
    }

    public ArrayList<TextComponent> getLore() {
        return lore;
    }

    public int getLevel() {
        return level;
    }

    public boolean isActive(Session s) {
        return level <= s.getNotoriety();
    }

    /**
     * Rolls breakable chance for a SessionEquipment if the notoriety setting is active.
     * Skips artifacts and consumables. Sets durability on the item if successful.
     */
    public static void rollBreakable(Session s, SessionEquipment se) {
        if (!BREAKABLE_EQUIPMENT.isActive(s)) return;
        EquipmentType type = se.getEquipment().getType();
        if (type == EquipmentType.ARTIFACT || type == EquipmentType.CONSUMABLE) return;
        if (NeoRogue.gen.nextDouble() < BREAKABLE_CHANCE) {
            se.setDurability(BREAKABLE_DURABILITY);
        }
    }

    /**
     * Rolls breakable chance for a list of SessionEquipment.
     */
    public static void rollBreakable(Session s, ArrayList<SessionEquipment> equips) {
        for (SessionEquipment se : equips) {
            rollBreakable(s, se);
        }
    }
}
