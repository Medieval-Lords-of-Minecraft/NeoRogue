package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MinibossMarathonAchievement implements Achievement {
	private static final String ID = "miniboss_marathon";
	private static final int[] THRESHOLDS = { 3, 4, 5 };

	@Override public String getId() { return ID; }
	@Override public Component getDisplayName() { return Component.text("Miniboss Marathon", NamedTextColor.GOLD); }
	@Override public Material getMaterial() { return Material.IRON_BOOTS; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.SESSION); }
	@Override public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		return List.of(Component.text("Defeat " + target + " minibosses in one region.", NamedTextColor.GRAY));
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(ID, SessionTrigger.WIN_MINIBOSS, (pdata, in) -> {
			int count = pdata.getCurrentRegionMinibossCount();
			if (count > progress.getProgress() && progress.addProgress(count - progress.getProgress())) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
