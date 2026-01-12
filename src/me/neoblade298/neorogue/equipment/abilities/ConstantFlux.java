package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
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
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 2, 0));
		damagePerStack = isUpgraded ? 15 : 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta stacks = new ActionMeta();
		String taskId = id + "-timer-" + slot;
		ItemStack noStackIcon = item.clone();
		ItemStack stackIcon = item.clone().withType(Material.LIGHT_BLUE_DYE);
		ConstantFluxInstance inst = new ConstantFluxInstance(data, this, slot, es, stacks, taskId, noStackIcon, stackIcon);
		data.addTrigger(id, bind, inst);
	}

	private class ConstantFluxInstance extends EquipmentInstance {
		public ConstantFluxInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es, ActionMeta stacks, String taskId, ItemStack noStackIcon, ItemStack stackIcon) {
			super(data, eq, slot, es);
			
			action = (pdata, in) -> {
				Player p = data.getPlayer();
				
				// Dash forward
				data.dash();
				Sounds.teleport.play(p, p);
				
				// Cancel previous timer and start new one
				data.removeAndCancelTask(taskId);
				data.addTask(taskId, new BukkitRunnable() {
					public void run() {
						// Remove all stacks after 5 seconds of not casting
						for (int i = 0; i < stacks.getCount(); i++) {
							data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), 
								Buff.increase(data, -damagePerStack, StatTracker.damageBuffAlly(id + slot, eq)));
						}
						stacks.setCount(0);
						setIcon(noStackIcon);
					}
				}.runTaskLater(NeoRogue.inst(), 100L)); // 5 seconds = 100 ticks
				
				// Increase stack counter (max 5)
				if (stacks.getCount() < MAX_STACKS) {
					stacks.addCount(1);
					
					// Add damage buff for physical damage
					data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), 
						Buff.increase(data, damagePerStack, StatTracker.damageBuffAlly(id + slot, eq)));
					
					// Update icon to show stack count
					ItemStack currentIcon = stackIcon.clone();
					currentIcon.setAmount(stacks.getCount());
					setIcon(currentIcon);
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
				"Passive. While you have at least " + GlossaryTag.STEALTH.tag(this, thres, false) + ", cast to " + 
				GlossaryTag.DASH.tag(this) + " forward and increase your " + GlossaryTag.PHYSICAL.tag(this) + 
				" damage by <yellow>" + damagePerStack + "</yellow>, stacking up to <white>" + MAX_STACKS + "x</white>. " +
				"Not casting this ability for <white>5s</white> removes all stacks.");
	}
}
