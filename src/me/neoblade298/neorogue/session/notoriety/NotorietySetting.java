package me.neoblade298.neorogue.session.notoriety;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class NotorietySetting {
    private String name;
    private String description;
    private Material emptyIcon, icon;
    private int level;

    public NotorietySetting(String name, String description, Material emptyIcon, Material icon) {
        this.name = name;
        this.description = description;
        this.emptyIcon = emptyIcon;
        this.icon = icon;
    }

    public ItemStack getItem() {
        return CoreInventory.createButton(level == 0 ? emptyIcon : icon, Component.text(name, NamedTextColor.GOLD),
                Component.text(description, NamedTextColor.WHITE),
                Component.text("Current Level: " + level, NamedTextColor.YELLOW));
    }

    public abstract void increase();
    public abstract void decrease();
}
