package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class VersatileAchievement implements Achievement {
	private static final String ID = "versatile";
	private static final int[] THRESHOLDS = { 3, 4, 5 };

	@Override public String getId() { return ID; }
	@Override public Component getDisplayName() { return Component.text("Versatile", NamedTextColor.GOLD); }
	@Override public Material getMaterial() { return Material.BOOK; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.FIGHT); }
	@Override public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		return List.of(Component.text("Cast " + target + " distinct abilities in one fight.", NamedTextColor.GRAY));
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		Set<String> castIds = new HashSet<>();
		data.addTrigger(ID, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent event = (CastUsableEvent) in;
			if (event.getInstance().getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();
			castIds.add(event.getInstance().getEquipment().getId());
			int count = castIds.size();
			if (count > progress.getProgress() && progress.addProgress(count - progress.getProgress())) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
