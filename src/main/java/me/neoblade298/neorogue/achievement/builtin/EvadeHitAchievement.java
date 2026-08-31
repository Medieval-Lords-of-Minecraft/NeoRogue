package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementScope;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.EvadeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EvadeHitAchievement implements Achievement {
	private static final String ID = "clean_getaway";
	private static final int[] THRESHOLDS = { 10, 25, 50 };

	@Override public String getId() { return ID; }
	@Override public Component getDisplayName() { return Component.text("Clean Getaway", NamedTextColor.GOLD); }
	@Override public Material getMaterial() { return Material.RABBIT_FOOT; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public AchievementScope getScope() { return AchievementScope.CLASS; }
	@Override public EquipmentClass getRequiredClass() { return EquipmentClass.THIEF; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.FIGHT); }

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		return List.of(Component.text("Fully evade a single hit dealing at least " + target + " damage.", NamedTextColor.GRAY));
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		data.addTrigger(ID, Trigger.EVADE, (pdata, in) -> {
			EvadeEvent event = (EvadeEvent) in;
			if (!event.isFullyEvaded()) return TriggerResult.keep();
			int damage = (int) Math.floor(event.getStartingDamage());
			if (damage > progress.getProgress() && progress.addProgress(damage - progress.getProgress())) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
