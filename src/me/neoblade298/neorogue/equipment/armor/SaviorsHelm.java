package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class SaviorsHelm extends Equipment {
	private static final String ID = "SaviorsHelm";
	private static final TargetProperties tp = TargetProperties.radius(5, false);
	private int def, sanct;
	
	public SaviorsHelm(boolean isUpgraded) {
		super(ID, "Savior's Helm", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR, EquipmentProperties.none().add(PropertyType.RANGE, tp.range));
		def = isUpgraded ? 8 : 6;
		sanct = isUpgraded ? 60 : 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, def, StatTracker.defenseBuffAlly(buffId, this)));
		data.addTask(new BukkitRunnable() {
			public void run() {
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(data.getPlayer(), tp)) {
					FightInstance.applyStatus(ent, StatusType.SANCTIFIED, data, sanct, -1);
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0, 60));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_HELMET, "Decrease " + GlossaryTag.MAGICAL.tag(this) + " damage taken by <yellow>" + def + "</yellow>. Passively applies "
		+ GlossaryTag.SANCTIFIED.tag(this, sanct, true) + " to all nearby enemies every <white>3s</white>.");
	}
}
