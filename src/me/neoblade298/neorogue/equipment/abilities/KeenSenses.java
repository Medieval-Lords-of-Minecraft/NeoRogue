package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.weapons.BasicBow;
import me.neoblade298.neorogue.equipment.weapons.HuntersBow;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;

public class KeenSenses extends Equipment {
	private static final String ID = "keenSenses";
	private int shields, damage;
	
	public KeenSenses(boolean isUpgraded) {
		super(ID, "Keen Senses", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
				damage = isUpgraded ? 30 : 20;
				shields = 5;
	}

	@Override
	public void setupReforges() {
		addSelfReforge(SpikeTrap.get());
		addReforge(LayTrap.get(), SpikeTrap.get());
		addReforge(BasicElementMastery.get(), FrostTrap.get());
		addReforge(FocusedShot.get(), SunderingShot.get(), GetCentered.get());
		addReforge(BasicBow.get(), HuntersBow.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addPermanentShield(p.getUniqueId(), shields);
		data.applyStatus(StatusType.FOCUS, data, 1, -1);
		data.addTrigger(id, Trigger.PRE_DEALT_DAMAGE, (pdata, in) -> {
			PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
			if (!ev.getMeta().hasOrigin(DamageOrigin.TRAP)) return TriggerResult.keep();
			ev.getMeta().addBuff(BuffType.GENERAL, new Buff(data, 0, damage * 0.01), BuffOrigin.NORMAL, true);
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
