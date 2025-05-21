package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class SerratedRazor extends Equipment {
	private static final String ID = "serratedRazor";
	private static int base = 40;
	private int bonus;
	
	public SerratedRazor(boolean isUpgraded) {
		super(ID, "Serrated Razor", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(base, 3, 0, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		bonus = isUpgraded ? 80 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, new RazorInstance(data, this, slot, es));
	}

	private class RazorInstance extends EquipmentInstance {
		private int count = 0;

		public RazorInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			action = (data2, in) -> {
				LeftClickHitEvent ev = (LeftClickHitEvent) in;
				if (++count >= 3) {
					FightData fd = FightInstance.getFightData(ev.getTarget());
					boolean canBonus = fd.hasStatus(StatusType.POISON) || fd.hasStatus(StatusType.INSANITY);
					DamageMeta dm = new DamageMeta(data, base + (canBonus ? bonus : 0), DamageType.PIERCING);
					weaponSwingAndDamage(p, data, ev.getTarget(), dm);
					data.setBasicAttackCooldown(EquipSlot.HOTBAR, 3000L);
					Sounds.extinguish.play(p, p);
					count = 0;
					this.setCooldown(3);
				}
				else {
					weaponSwingAndDamage(p, data, ev.getTarget());
				}
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WOODEN_HOE, "Every <white>3</white> basic attacks, deal an additional <yellow>" + bonus + "</yellow>"
				+ " damage if the target has " + GlossaryTag.POISON.tag(this) + " or " + GlossaryTag.INSANITY.tag(this) + ", and "
						+ "your attack cooldown is set to <white>3</white> seconds.");
	}
}
