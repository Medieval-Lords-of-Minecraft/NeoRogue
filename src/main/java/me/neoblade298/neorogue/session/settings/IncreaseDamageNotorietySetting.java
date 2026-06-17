package me.neoblade298.neorogue.session.settings;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class IncreaseDamageNotorietySetting extends NotorietySetting {
    private static final IncreaseDamageNotorietySetting INSTANCE = new IncreaseDamageNotorietySetting();
    public static final double DAMAGE_MULTIPLIER = 1.2;

    public IncreaseDamageNotorietySetting() {
        super(
            Component.text("Increase enemy damage by ")
                .color(NamedTextColor.GRAY)
                .append(Component.text("20%").color(NamedTextColor.YELLOW)),
            Component.text("Your caravan is attracting more dangerous foes.")
        );
    }

    public static IncreaseDamageNotorietySetting getInstance() { return INSTANCE; }
}
