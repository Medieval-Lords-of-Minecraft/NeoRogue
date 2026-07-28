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
    // Returns a reason string when the setting is locked (uninteractable), or null when unlocked.
    private SettingLock lock;

    public static final SessionSetting ENDLESS_MODE = new SessionSetting(
        0, "Endless Mode", Material.SCULK_CATALYST,
        "Enable to repeatedly cycle through regions.\nDisables competitive aspects like winrates, achievements, rewards.",
        (s, leftClick) -> s.setEndless(!s.isEndless()),
        s -> s.isEndless() ? 1 : 0,
        s -> s.isCompetitiveRun() ? "Disabled while Competitive is on" : null
    );

    public static final SessionSetting OPEN_LOBBY = new SessionSetting(
        1, "Open Lobby", Material.OAK_DOOR,
        "Enable to auto-accept join requests without host approval.",
        (s, leftClick) -> s.setLobbyOpen(!s.isLobbyOpen()),
        s -> s.isLobbyOpen() ? 1 : 0
    );

    public static final SessionSetting COMPETITIVE_MODE = new SessionSetting(
        2, "Competitive Mode", Material.DIAMOND_SWORD,
        "Enable to record this run for competitive leaderboards.\nCasual runs are tracked separately.",
        (s, leftClick) -> s.setCompetitive(!s.isCompetitiveRun()),
        s -> s.isCompetitiveRun() ? 1 : 0,
        s -> s.isEndless() ? "Disabled while Endless is on" : null
    );

    // Toggle setting with standardized icon (enchant glow when enabled, Yes/No lore)
    public SessionSetting(int id, String title, Material mat, String description,
            SettingEffect effect, SettingValueRetriever valueRetriever) {
        this(id, title, mat, description, effect, valueRetriever, null);
    }

    // Toggle setting with a lock predicate: when lock returns a reason, the icon greys out and clicks are blocked.
    public SessionSetting(int id, String title, Material mat, String description,
            SettingEffect effect, SettingValueRetriever valueRetriever, SettingLock lock) {
        this.title = title;
        this.effect = effect;
        this.valueRetriever = valueRetriever;
        this.lock = lock;
        this.iconUpdater = (s) -> {
            String lockReason = lock == null ? null : lock.getReason(s);
            boolean locked = lockReason != null;
            boolean enabled = valueRetriever.get(s) != 0;
            ItemStack icon = new ItemStack(locked ? Material.GRAY_DYE : mat);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(Component.text(title, locked ? NamedTextColor.GRAY : NamedTextColor.GOLD));
            ArrayList<Component> lore = new ArrayList<>();
            if (locked) {
                lore.add(Component.text("Locked: ", NamedTextColor.RED)
                        .append(Component.text(lockReason, NamedTextColor.GRAY)));
            } else {
                lore.add(Component.text("Enabled: ", NamedTextColor.WHITE)
                        .append(enabled
                                ? Component.text("Yes", NamedTextColor.GREEN)
                                : Component.text("No", NamedTextColor.RED)));
            }
            for (String line : description.split("\n")) {
                lore.add(Component.text(line, NamedTextColor.GRAY));
            }
            meta.lore(lore);
            if (enabled && !locked) {
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

    public static interface SettingLock {
        // Returns a reason string when locked, or null when the setting is interactable.
        public String getReason(Session s);
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
    public boolean isLocked(Session s) {
        return lock != null && lock.getReason(s) != null;
    }
    public boolean canLeftClick(Session s) {
        return !isLocked(s);
    }
    public boolean canRightClick(Session s) {
        return !isLocked(s);
    }
}
