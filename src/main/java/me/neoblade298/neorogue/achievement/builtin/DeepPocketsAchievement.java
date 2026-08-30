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

public class DeepPocketsAchievement implements Achievement {
	private static final String ID = "deep_pockets";
	private static final int[] THRESHOLDS = { 500, 1000, 2000 };

	@Override public String getId() { return ID; }
	@Override public Component getDisplayName() { return Component.text("Deep Pockets", NamedTextColor.GOLD); }
	@Override public Material getMaterial() { return Material.RAW_GOLD_BLOCK; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.SESSION); }

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		return List.of(Component.text("Hold " + target + " " + PlayerSessionData.CURRENCY + " at once.", NamedTextColor.GRAY));
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		check(data, progress);
		data.addTrigger(ID, SessionTrigger.CURRENCY_CHANGED, (pdata, in) -> {
			check(pdata, progress);
			return TriggerResult.keep();
		});
	}

	private void check(PlayerSessionData data, AchievementProgress progress) {
		int balance = data.getCurrency();
		if (balance > progress.getProgress() && progress.addProgress(balance - progress.getProgress())) {
			AchievementManager.notifyMastery(data.getPlayer(), this, progress);
		}
	}
}
