package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ApplyNegativeStatusAchievement implements Achievement {
	private static final String ID = "afflictor";
	private static final int[] THRESHOLDS = { 100, 1000, 10000, 100000 };

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Afflictor", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.WITHER_ROSE;
	}

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		return List.of(Component.text("Apply " + target + " stacks of negative statuses.", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.FIGHT);
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		data.addTrigger(ID, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (ev.getStatusClass() != StatusClass.NEGATIVE) return TriggerResult.keep();
			int stacks = ev.getStacks();
			if (stacks <= 0) return TriggerResult.keep();
			if (progress.addProgress(stacks)) {
				Player p = pdata.getPlayer();
				AchievementManager.notifyMastery(p, this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
