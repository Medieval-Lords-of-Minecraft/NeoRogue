package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.weapons.BasicBow;
import me.neoblade298.neorogue.equipment.weapons.HuntersBow;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class KeenSenses extends Equipment {
	private static final String ID = "KeenSenses";
	private int shields, damage;
	
	public KeenSenses(boolean isUpgraded) {
		super(ID, "Keen Senses", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
				damage = isUpgraded ? 30 : 20;
				shields = 5;
	}

	@Override
	public void setupReforges() {
		addReforge(LayTrap.get(), SpikeTrap.get());
		addReforge(FocusedShot.get(), SunderingShot.get(), GetCentered.get());
		addReforge(BasicBow.get(), HuntersBow.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Player p = data.getPlayer();
		String buffId = UUID.randomUUID().toString();
		data.addPermanentShield(p.getUniqueId(), shields);
		data.applyStatus(StatusType.FOCUS, data, 1, -1);
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			if (!ev.getMeta().hasOrigin(DamageOrigin.TRAP)) return TriggerResult.keep();
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, 0, damage * 0.01, StatTracker.damageBuffAlly(buffId, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PISTON,
				"Passive. Start fights with " + GlossaryTag.SHIELDS.tag(this, shields, false) + 
				" and " + GlossaryTag.FOCUS.tag(this, 1, false) + ". Damage from " + GlossaryTag.TRAP.tagPlural(this) +
				" is increased by " + DescUtil.yellow(damage + "%") + ".");
	}
}
