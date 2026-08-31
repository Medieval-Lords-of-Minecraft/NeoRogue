package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementScope;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MultiKillAchievement implements Achievement {
	private static final String ID = "one_shot_many_kills";
	private static final int[] THRESHOLDS = { 3, 5, 10 };

	@Override public String getId() { return ID; }
	@Override public Component getDisplayName() { return Component.text("One Shot, Many Kills", NamedTextColor.GOLD); }
	@Override public Material getMaterial() { return Material.SPECTRAL_ARROW; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public AchievementScope getScope() { return AchievementScope.CLASS; }
	@Override public EquipmentClass getRequiredClass() { return EquipmentClass.ARCHER; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.FIGHT); }

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		return List.of(Component.text("Kill " + target + " enemies with one projectile or ability activation.", NamedTextColor.GRAY));
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		Map<ProjectileInstance, Integer> projectileKills = new IdentityHashMap<>();
		int[] abilityTick = { -1 };
		int[] abilityKills = { 0 };
		data.addTrigger(ID, Trigger.KILL, (pdata, in) -> {
			DamageMeta damage = ((KillEvent) in).getDamageMeta();
			ProjectileInstance projectile = damage.getProjectile();
			int count;
			if (projectile != null) {
				count = projectileKills.merge(projectile, 1, Integer::sum);
			}
			else {
				if (damage.isBasicAttack()) return TriggerResult.keep();
				int currentTick = Bukkit.getCurrentTick();
				if (abilityTick[0] != currentTick) {
					abilityTick[0] = currentTick;
					abilityKills[0] = 0;
				}
				count = ++abilityKills[0];
			}
			if (count > progress.getProgress() && progress.addProgress(count - progress.getProgress())) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
