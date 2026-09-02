package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class SeeingTripleAchievement implements Achievement {
	private static final String ID = "seeing_triple";
	private static final int[] THRESHOLDS = { 1 };

	@Override public String getId() { return ID; }
	@Override public Component getDisplayName() { return Component.text("Seeing Triple", NamedTextColor.GOLD); }
	@Override public Material getMaterial() { return Material.TRIPWIRE_HOOK; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.SESSION); }
	@Override public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Beat a boss with 3 of the same equipment.", NamedTextColor.GRAY));
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(ID, SessionTrigger.WIN_BOSS, (pdata, in) -> {
			Map<String, Integer> counts = new HashMap<>();
			for (PlayerSessionData.EquipmentMetadata meta : pdata.aggregateEquipment(
					m -> m.getEquipSlot() != EquipSlot.STORAGE)) {
				String rawId = meta.getEquipment().getId();
				if (counts.merge(rawId, 1, Integer::sum) >= 3 && progress.addProgress(1)) {
					AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
					break;
				}
			}
			return TriggerResult.keep();
		});
	}
}
