package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class HastyShield extends Equipment {
	private static final String ID = "hastyShield";
	private int reduction, amount;
	
	public HastyShield(boolean isUpgraded) {
		super(ID, "Hasty Shield", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		reduction = 15;
		amount = isUpgraded ? 25 : 18;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, new HastyShieldInstance(p, this));
	}
	
	private class HastyShieldInstance implements TriggerAction {
		private Player p;
		private long nextUsable = 0L;
		private Equipment eq;
		public HastyShieldInstance(Player p, Equipment eq) {
			this.p = p;
			this.eq = eq;
		}
		
		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			if (p.getHandRaised() != EquipmentSlot.OFF_HAND || !p.isHandRaised()) return TriggerResult.keep();
			long now = System.currentTimeMillis();
			if (now <= nextUsable) return TriggerResult.keep();
			
			ReceivedDamageEvent ev = (ReceivedDamageEvent) inputs;
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, reduction, 0, StatTracker.damageBarriered(eq)));
			nextUsable = now + 5000L; // 5s
			p.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
			data.addMana(amount);
			data.addStamina(amount);
			return TriggerResult.keep();
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "When raised, reduce the next hit taken by <white>" + reduction + "</white>"
				+ " and grant <yellow>" + amount + " </yellow>mana and stamina. 5 second cooldown.");
	}
}
