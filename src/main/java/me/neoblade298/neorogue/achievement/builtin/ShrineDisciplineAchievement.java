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

public class ShrineDisciplineAchievement implements Achievement {
	private static final int[] THRESHOLDS = { 1 };
	private final boolean rest;

	public ShrineDisciplineAchievement(boolean rest) {
		this.rest = rest;
	}

	@Override public String getId() { return rest ? "no_rest_for_the_wicked" : "untempered"; }
	@Override public Component getDisplayName() {
		return Component.text(rest ? "No Rest for the Wicked" : "Untempered", NamedTextColor.GOLD);
	}
	@Override public Material getMaterial() { return rest ? Material.SOUL_LANTERN : Material.ANVIL; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.SESSION); }
	@Override public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text(rest ? "Win a run without the party choosing to rest at a shrine."
				: "Win a run without the party choosing to upgrade at a shrine.", NamedTextColor.GRAY));
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(getId(), SessionTrigger.WIN_RUN, (pdata, in) -> {
			boolean invalid = rest ? pdata.hasRestedAtShrine() : pdata.hasUpgradedAtShrine();
			if (!invalid && progress.addProgress(1)) AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			return TriggerResult.keep();
		});
	}
}
