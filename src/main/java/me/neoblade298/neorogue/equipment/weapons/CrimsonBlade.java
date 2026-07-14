package me.neoblade298.neorogue.equipment.weapons;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CrimsonBlade extends Equipment {
	private static final String ID = "CrimsonBlade";
	private static final int DURATION = 30;
	private int heal;
	
	public CrimsonBlade(boolean isUpgraded) {
		super(ID, "Crimson Blade", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 40 : 35, 1, 0.4, DamageType.SLASHING, Sounds.attackSweep));
		properties.addUpgrades(PropertyType.DAMAGE);
		heal = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, new CrimsonBladeInstance(id, es, slot));
	}
	
	private class CrimsonBladeInstance extends PriorityAction {
		private int count;
		private long start;
		private boolean deactivated;
		private EquipSlot es;
		private int slot;
		public CrimsonBladeInstance(String id, EquipSlot es, int slot) {
			super(id);
			this.es = es;
			this.slot = slot;
			start = System.currentTimeMillis();
			action = (pdata, inputs) -> {
				LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
				Player p = pdata.getPlayer();
				weaponSwingAndDamage(p, pdata, ev.getTarget());
				if (System.currentTimeMillis() - start >= DURATION * 1000L) {
					if (!deactivated) {
						deactivated = true;
						Util.msgRaw(p, Component.text("").append(hoverable).append(Component.text(" was deactivated", NamedTextColor.GRAY)));
						replaceWithWoodenSword(p);
					}
					return TriggerResult.keep();
				}
				if (++count >= 5) {
					Sounds.enchant.play(p, p);
					FightInstance.giveHeal(p, heal, CrimsonBlade.this, p);
					count = 0;
				}
				return TriggerResult.keep();
			};
		}

		private void replaceWithWoodenSword(Player p) {
			System.out.println("Replacing slot " + slot + " with wooden sword");
			// withType() returns a NEW ItemStack (carrying over meta); it does not mutate in place
			p.getInventory().setItem(slot, item.withType(Material.WOODEN_SWORD));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SWORD, "For the first " + DescUtil.white(DURATION + "s") + " of a fight, every 5 basic attacks with this weapon heals you for " + DescUtil.yellow(heal) + ".");
	}
}
