package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementScope;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EquipRaritySetAchievement implements Achievement {
	private static final int[] THRESHOLDS = { 5 };
	private static final EnumSet<EquipmentType> REQUIRED_TYPES = EnumSet.of(EquipmentType.WEAPON,
			EquipmentType.ABILITY, EquipmentType.ARMOR, EquipmentType.ACCESSORY, EquipmentType.OFFHAND);

	private final String id;
	private final Component displayName;
	private final Material material;
	private final Rarity rarity;

	public EquipRaritySetAchievement(String id, String displayName, Material material, Rarity rarity) {
		this.id = id;
		this.displayName = Component.text(displayName, NamedTextColor.GOLD);
		this.material = material;
		this.rarity = rarity;
	}

	@Override public String getId() { return id; }
	@Override public Component getDisplayName() { return displayName; }
	@Override public Material getMaterial() { return material; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public AchievementScope getScope() { return AchievementScope.GLOBAL; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.SESSION); }

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Equip a " + rarity.name().toLowerCase()
				+ " weapon, ability, armor, accessory, and offhand at the same time.", NamedTextColor.GRAY));
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		check(data, progress);
		data.addTrigger(id, SessionTrigger.EQUIPMENT_LAYOUT_CHANGED, (pdata, in) -> {
			check(pdata, progress);
			return TriggerResult.keep();
		});
	}

	private void check(PlayerSessionData data, AchievementProgress progress) {
		EnumSet<EquipmentType> equipped = EnumSet.noneOf(EquipmentType.class);
		for (PlayerSessionData.EquipmentMetadata meta : data.aggregateEquipment(m ->
				m.getEquipSlot() != EquipSlot.STORAGE && m.getEquipment().getRarity() == rarity)) {
			if (REQUIRED_TYPES.contains(meta.getEquipment().getType())) equipped.add(meta.getEquipment().getType());
		}
		int count = equipped.size();
		if (count > progress.getProgress() && progress.addProgress(count - progress.getProgress())) {
			AchievementManager.notifyMastery(data.getPlayer(), this, progress);
		}
	}
}
