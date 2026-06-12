package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AcquireRarityAchievement implements Achievement {
	private static final int[] THRESHOLDS = { 1, 10, 50 };

	private final String id;
	private final Component displayName;
	private final Material material;
	private final Rarity rarity;

	public AcquireRarityAchievement(String id, Component displayName, Material material, Rarity rarity) {
		this.id = id;
		this.displayName = displayName;
		this.material = material;
		this.rarity = rarity;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Component getDisplayName() {
		return displayName;
	}

	@Override
	public Material getMaterial() {
		return material;
	}

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public int getSortPriority() {
		return 30;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		String name = rarity.name().charAt(0) + rarity.name().substring(1).toLowerCase();
		return List.of(Component.text("Acquire a(n) " + name + " equipment (not artifact/consumable).", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.SESSION);
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(id, SessionTrigger.ACQUIRE_EQUIPMENT, (pdata, in) -> {
			Equipment eq = (Equipment) in;
			if (eq.getRarity() != rarity) return;
			EquipmentType type = eq.getType();
			if (type == EquipmentType.ARTIFACT || type == EquipmentType.CONSUMABLE) return;
			if (progress.addProgress(1)) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
		});
	}
}
