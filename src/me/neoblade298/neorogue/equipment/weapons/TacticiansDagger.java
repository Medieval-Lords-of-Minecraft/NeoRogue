package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class TacticiansDagger extends Equipment {
	private static final String ID = "TacticiansDagger";
	private int damage;
	
	public TacticiansDagger(boolean isUpgraded) {
		super(ID, "Tactician's Dagger", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(30, 1, 0.2, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		damage = isUpgraded ? 120 : 80;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone().withType(Material.WOODEN_SWORD);
		StandardPriorityAction timer = new StandardPriorityAction(ID);
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		timer.setAction((pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.GENERAL)) {
				return TriggerResult.keep();
			}
			inst.setIcon(icon);
			timer.setTime(System.currentTimeMillis());
			data.addTask(new BukkitRunnable() {
				public void run() {
					inst.setIcon(item);
				}
			}.runTaskLater(NeoRogue.inst(), 60));
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.DEAL_DAMAGE, timer);
		
		inst.setAction((pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			boolean hasBonus = timer.getTime() + 3000 < System.currentTimeMillis();
			weaponSwingAndDamage(data
					.getPlayer(), data, ev.getTarget(), properties.get(PropertyType.DAMAGE) + (hasBonus ? damage : 0));
			return TriggerResult.keep();
		});

		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SWORD, "Deal an additional " + GlossaryTag.PIERCING.tag(this, damage, true) + " if "
				+ "you haven't dealt " + GlossaryTag.GENERAL.tag(this) + " damage in <white>3</white> seconds.");
	}
}
