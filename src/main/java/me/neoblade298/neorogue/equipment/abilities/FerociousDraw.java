package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class FerociousDraw extends Equipment implements Power {
	private static final String ID = "FerociousDraw";
	private int range, damage;

	public FerociousDraw(boolean isUpgraded) {
		super(ID, "Ferocious Draw", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER, EquipmentType.ABILITY,
				EquipmentProperties.none());
		range = 5;
		damage = isUpgraded ? 65 : 50;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			LivingEntity target = ev.getTarget();
			if (target == null) return TriggerResult.keep();
			if (data.getPlayer().getLocation().distance(target.getLocation()) > 5) return TriggerResult.keep();
			if (am.addCount(1) < 5) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata2, in2) -> {
			Player p2 = data.getPlayer();
			PreDealDamageEvent ev2 = (PreDealDamageEvent) in2;
			DamageMeta meta = ev2.getMeta();
			if (!meta.isBasicAttack()) return TriggerResult.keep();
			LivingEntity target2 = ev2.getTarget();
			if (target2 == null) return TriggerResult.keep();
			ProjectileInstance inst = meta.getProjectile();
			if (inst == null) return TriggerResult.keep();
			if (p2.getLocation().distance(target2.getLocation()) <= range) {
				meta.addDamageSlice(
						new DamageSlice(data, damage, DamageType.PIERCING, DamageStatTracker.of(id + slot, this)));
				inst.addPierce(1);
				Sounds.infect.play(p2, p2);
			}
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_AXE,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after basic attacking " + DescUtil.val(5) + " enemies within " + DescUtil.val(5) + " blocks. Basic attack projectiles that hit an enemy within " + DescUtil.val(range) + " blocks "
						+ "pierce that enemy and deal an additional " + GlossaryTag.PIERCING.tag(this, damage)
						+ " damage.");
	}
}
