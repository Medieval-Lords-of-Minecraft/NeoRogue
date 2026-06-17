package me.neoblade298.neorogue.session.settings;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class IncreaseDamageNotorietySetting extends NotorietySetting {
    private static final IncreaseDamageNotorietySetting INSTANCE = new IncreaseDamageNotorietySetting();
    public static final double DAMAGE_MULTIPLIER = 1.2;

    public IncreaseDamageNotorietySetting() {
        super(getHeader(), getLore());
    }

    public static TextComponent getHeader() {
        return Component.text("Increase enemy damage by ")
            .color(NamedTextColor.GRAY)
            .append(Component.text("20%").color(NamedTextColor.YELLOW));
    }

    public static TextComponent getLore() {
        return Component.text("Your caravan is attracting more dangerous foes.");
    }

    public static IncreaseDamageNotorietySetting getInstance() { return INSTANCE; }
}
