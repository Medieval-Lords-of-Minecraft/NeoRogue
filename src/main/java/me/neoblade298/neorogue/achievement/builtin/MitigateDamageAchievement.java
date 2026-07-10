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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MitigateDamageAchievement implements Achievement {
	private static final String ID = "bulwark";
	private static final int[] THRESHOLDS = { 1000, 10000, 100000, 1000000 };

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Bulwark", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.SHIELD;
	}

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		return List.of(Component.text("Mitigate " + target + " damage with buffs.", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.FIGHT);
	}

	@Override
	public int getSortPriority() {
		return 6;
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		// Poll the running buff-mitigation total each player tick (~1s). Crediting happens on the
		// mitigating player's stats regardless of who was hit, so polling captures both self- and
		// ally-mitigation. Only the delta since the last poll is credited, so nothing double-counts.
		// acc[0] = last-seen total, acc[1] = carried fractional damage.
		double[] acc = { 0, 0 };
		data.addTrigger(ID, Trigger.PLAYER_TICK, (pdata, in) -> {
			double total = pdata.getStats().getDamageMitigated();
			double delta = total - acc[0];
			acc[0] = total;
			if (delta <= 0) return TriggerResult.keep();
			acc[1] += delta;
			int whole = (int) acc[1];
			if (whole <= 0) return TriggerResult.keep();
			acc[1] -= whole;
			if (progress.addProgress(whole)) {
				Player p = pdata.getPlayer();
				AchievementManager.notifyMastery(p, this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
