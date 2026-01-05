package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ConstantFlux extends Equipment {
	private static final String ID = "ConstantFlux";
	private static final int MAX_STACKS = 5;
	
	private int damagePerStack;
    private int thres = 2;

	public ConstantFlux(boolean isUpgraded) {
		super(ID, "Constant Flux", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 10, 0));
		damagePerStack = isUpgraded ? 15 : 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta stacks = new ActionMeta();
		ConstantFluxInstance inst = new ConstantFluxInstance(data, this, slot, es, stacks);
		data.addTrigger(id, bind, inst);
	}

	private class ConstantFluxInstance extends EquipmentInstance {
		public ConstantFluxInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es, ActionMeta stacks) {
			super(data, eq, slot, es);
			
			action = (pdata, in) -> {
				Player p = data.getPlayer();
				
				// Dash forward
				data.dash();
				Sounds.teleport.play(p, p);
				
				// Increase stack counter (max 5)
				if (stacks.getCount() < MAX_STACKS) {
					stacks.addCount(1);
					
					// Add damage buff for physical damage
					data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), 
						Buff.increase(data, damagePerStack, StatTracker.damageBuffAlly(id + slot, eq)));
				}
				
				return TriggerResult.keep();
			};
		}
		
		@Override
		public boolean canTrigger(Player p, PlayerFightData data, Object in) {
			if (!data.hasStatus(StatusType.STEALTH)) {
				return false;
			}
			
			int stealthStacks = data.getStatus(StatusType.STEALTH).getStacks();
			if (stealthStacks < thres) {
				return false;
			}
			
			return super.canTrigger(p, data, in);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER,
				"While you have at least " + GlossaryTag.STEALTH.tag(this, thres, false) + ", cast to " + 
				GlossaryTag.DASH.tag(this) + " forward and increase your " + GlossaryTag.PHYSICAL.tag(this) + 
				" damage by <yellow>" + damagePerStack + "</yellow>, stacking up to <white>" + MAX_STACKS + "x</white>.");
	}
}
