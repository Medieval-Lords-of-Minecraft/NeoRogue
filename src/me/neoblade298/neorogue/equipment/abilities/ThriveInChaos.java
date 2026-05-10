package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
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
				EquipmentProperties.ofUsable(30, 20, 0, 0));
		insanityPerStack = isUpgraded ? 7 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			ActionMeta am = new ActionMeta();
			data.addTrigger(id, Trigger.PLAYER_TICK, (pdata2, in2) -> {
				am.addCount(1);
				if (am.getCount() >= 3) {
					am.setCount(0);
					int totalInsanity = 0;
					Player p = data.getPlayer();
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
						FightData fd = FightInstance.getFightData(ent);
						if (fd.hasStatus(StatusType.INSANITY)) {
							totalInsanity += fd.getStatus(StatusType.INSANITY).getStacks();
						}
					}
					int stealthStacks = totalInsanity / insanityPerStack;
					if (stealthStacks > 0) {
						data.applyStatus(StatusType.STEALTH, data, stealthStacks, 60);
					}
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CHORUS_FRUIT,
				GlossaryTag.POWER.tag(this) + ". Every " + DescUtil.white("3s") + ", count the " + GlossaryTag.INSANITY.tag(this) + 
				" stacks of all enemies in range. For every " + 
				GlossaryTag.INSANITY.tag(this, insanityPerStack, true) + ", gain " + 
				GlossaryTag.STEALTH.tag(this, 1, false) + " " + DescUtil.duration(3, false) + ".");
	}
}
