package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ThriveInChaos extends Equipment {
	private static final String ID = "ThriveInChaos";
	private static final TargetProperties tp = TargetProperties.radius(7, false, TargetType.ENEMY);
	private int insanityPerStack;
	
	public ThriveInChaos(boolean isUpgraded) {
		super(ID, "Thrive in Chaos", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		insanityPerStack = isUpgraded ? 75 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() >= 3) {
				am.setCount(0);
				
				int totalInsanity = 0;
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
					FightData fd = FightInstance.getFightData(ent);
					if (fd.hasStatus(StatusType.INSANITY)) {
						totalInsanity += fd.getStatus(StatusType.INSANITY).getStacks();
					}
				}
				
				// Calculate stealth stacks to gain
				int stealthStacks = totalInsanity / insanityPerStack;
				if (stealthStacks > 0) {
					data.applyStatus(StatusType.STEALTH, data, stealthStacks, 60);
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CHORUS_FRUIT,
				"Passive. Every <white>3s</white>, count the " + GlossaryTag.INSANITY.tag(this) + 
				" stacks of all enemies in range. For every " + 
				GlossaryTag.INSANITY.tag(this, insanityPerStack, true) + ", gain " + 
				GlossaryTag.STEALTH.tag(this, 1, false) + " for <white>3s</white>.");
	}
}
