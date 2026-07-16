package me.neoblade298.neorogue.session.settings;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBT;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SessionSetting {
    public static HashMap<Integer, SessionSetting> settings = new HashMap<Integer, SessionSetting>();
    protected SettingEffect effect;
    protected final String title;
    protected SettingValueRetriever valueRetriever;
    private SettingIconUpdater iconUpdater;

    public static final SessionSetting ENDLESS_MODE = new SessionSetting(
        0, "Endless Mode", Material.SCULK_CATALYST,
        "Enable to repeatedly cycle through regions.",
        (s, leftClick) -> s.setEndless(!s.isEndless()),
        s -> s.isEndless() ? 1 : 0
    );

    public static final SessionSetting OPEN_LOBBY = new SessionSetting(
        1, "Open Lobby", Material.OAK_DOOR,
        "Enable to auto-accept join requests without host approval.",
        (s, leftClick) -> s.setLobbyOpen(!s.isLobbyOpen()),
        s -> s.isLobbyOpen() ? 1 : 0
    );

    // Toggle setting with standardized icon (enchant glow when enabled, Yes/No lore)
    public SessionSetting(int id, String title, Material mat, String description,
            SettingEffect effect, SettingValueRetriever valueRetriever) {
        this.title = title;
        this.effect = effect;
        this.valueRetriever = valueRetriever;
        this.iconUpdater = (s) -> {
            boolean enabled = valueRetriever.get(s) != 0;
            ItemStack icon = new ItemStack(mat);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(Component.text(title, NamedTextColor.GOLD));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.text("Enabled: ", NamedTextColor.WHITE)
                    .append(enabled
                            ? Component.text("Yes", NamedTextColor.GREEN)
                            : Component.text("No", NamedTextColor.RED)));
            lore.add(Component.text(description, NamedTextColor.GRAY));
            meta.lore(lore);
            if (enabled) {
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            icon.setItemMeta(meta);
            NBT.modify(icon, nbt -> { nbt.setInteger("id", id); });
            return icon;
        };
        settings.put(id, this);
    }

    // Used for settings with a fully custom icon
    public SessionSetting(String title, SettingEffect effect, SettingIconUpdater iconUpdater, SettingValueRetriever valueRetriever) {
        this.title = title;
        this.effect = effect;
        this.iconUpdater = iconUpdater;
        this.valueRetriever = valueRetriever;
    }

    public ItemStack getItem(Session s) {
        return iconUpdater.onChange(s);
    }

    public static interface SettingEffect {
        public void onChange(Session s, boolean leftClick);
    }

    public static interface SettingIconUpdater {
        public ItemStack onChange(Session s);
    }

    public static interface SettingValueRetriever {
        public int get(Session s);
    }

    public static SessionSetting getById(int id) {
        return settings.get(id);
    }
    public void leftClick(Session s) {
        effect.onChange(s, true);
    }
    public void rightClick(Session s) {
        effect.onChange(s, false);
    }
    public int getValue(Session s) {
        return valueRetriever.get(s);
    }
    public String getTitle() {
        return title;
    }
    public boolean canLeftClick(Session s) {
        return true;
    }
    public boolean canRightClick(Session s) {
        return true;
    }
}
