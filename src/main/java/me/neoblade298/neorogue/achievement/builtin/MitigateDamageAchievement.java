package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;
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
		return List.of(Component.text("Mitigate " + target + " incoming damage.", NamedTextColor.GRAY));
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
		// On each hit received, credit the damage buffs prevented for this player: the difference
		// between the pre- and post-mitigation damage on the DamageMeta. carry[0] holds fractional
		// damage between hits so nothing is lost to integer truncation.
		double[] carry = { 0 };
		data.addTrigger(ID, Trigger.RECEIVE_DAMAGE, (pdata, in) -> {
			DamageMeta meta = ((ReceiveDamageEvent) in).getMeta();
			if (meta == null) return TriggerResult.keep();
			double pre = 0, post = 0;
			for (double d : meta.getPreMitigationDamage().values()) pre += d;
			for (double d : meta.getPostMitigationDamage().values()) post += d;
			double mitigated = pre - post;
			if (mitigated <= 0) return TriggerResult.keep();
			carry[0] += mitigated;
			int whole = (int) carry[0];
			if (whole <= 0) return TriggerResult.keep();
			carry[0] -= whole;
			if (progress.addProgress(whole)) {
				Player p = pdata.getPlayer();
				AchievementManager.notifyMastery(p, this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
