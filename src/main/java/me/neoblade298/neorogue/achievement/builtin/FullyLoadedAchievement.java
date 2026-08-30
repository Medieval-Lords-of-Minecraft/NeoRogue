package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class FullyLoadedAchievement implements Achievement {
	private static final String ID = "fully_loaded";
	private static final int[] THRESHOLDS = { 1 };

	@Override public String getId() { return ID; }
	@Override public Component getDisplayName() { return Component.text("Fully Loaded", NamedTextColor.GOLD); }
	@Override public Material getMaterial() { return Material.CHEST; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.SESSION); }
	@Override public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Equip 6 abilities, 4 armor, 5 accessories, and an offhand at once.", NamedTextColor.GRAY));
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		check(data, progress);
		data.addTrigger(ID, SessionTrigger.EQUIPMENT_LAYOUT_CHANGED, (pdata, in) -> {
			check(pdata, progress);
			return TriggerResult.keep();
		});
	}

	private void check(PlayerSessionData data, AchievementProgress progress) {
		if (data.getAbilitiesEquipped() >= 6 && data.getArmorEquipped() >= 4
				&& data.getAccessoriesEquipped() >= 5 && data.getSessionEquipment(EquipSlot.OFFHAND)[0] != null
				&& progress.addProgress(1)) {
			AchievementManager.notifyMastery(data.getPlayer(), this, progress);
		}
	}
}
