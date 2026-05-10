package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class FerociousDraw extends Equipment {
	private static final String ID = "FerociousDraw";
	private int range, damage;

	public FerociousDraw(boolean isUpgraded) {
		super(ID, "Ferocious Draw", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(25, 15, 0, 0));
		range = 5;
		damage = isUpgraded ? 80 : 60;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata2, in2) -> {
				Player p = data.getPlayer();
				PreDealDamageEvent ev = (PreDealDamageEvent) in2;
				DamageMeta meta = ev.getMeta();
				if (!meta.isBasicAttack()) return TriggerResult.keep();
				LivingEntity target = ev.getTarget();
				if (target == null) return TriggerResult.keep();
				IProjectileInstance iProj = meta.getProjectile();
				if (iProj == null || !(iProj instanceof ProjectileInstance)) return TriggerResult.keep();
				if (p.getLocation().distance(target.getLocation()) <= range) {
					ProjectileInstance inst = (ProjectileInstance) iProj;
					meta.addDamageSlice(
							new DamageSlice(data, damage, DamageType.PIERCING, DamageStatTracker.of(id + slot, this)));
					inst.addPierce(1);
					Sounds.infect.play(p, p);
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_AXE,
				GlossaryTag.POWER.tag(this) + ". Basic attack projectiles that hit an enemy within " + DescUtil.white(range) + " blocks "
						+ "pierce that enemy and deal an additional " + GlossaryTag.PIERCING.tag(this, damage, true)
						+ " damage.");
	}
}
