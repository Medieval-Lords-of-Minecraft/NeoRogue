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
import me.neoblade298.neorogue.session.reward.RunReward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

// Accumulates the crowns earned from finishing runs (win or loss). Mastery tiers at
// 1,000 / 10,000 / 50,000 / 100,000 crowns.
public class CrownsEarnedAchievement implements Achievement {
	private static final String ID = "crown_collector";
	private static final int[] THRESHOLDS = { 1000, 10000, 50000, 100000 };

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Crown Collector", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.GOLD_BLOCK;
	}

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		return List.of(Component.text("Earn " + target + " crowns from runs.", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.SESSION);
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(ID, SessionTrigger.FINISH_RUN, (pdata, in) -> {
			boolean won = in instanceof Boolean b && b;
			int earned = (int) Math.round(RunReward.calculateBreakdown(session, won).total);
			if (earned <= 0) return;
			if (progress.addProgress(earned)) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
		});
	}
}
