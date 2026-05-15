package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
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
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class FerociousDraw extends Equipment {
	private static final String ID = "FerociousDraw";
	private int range, damage;

	public FerociousDraw(boolean isUpgraded) {
		super(ID, "Ferocious Draw", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0));
		range = 5;
		damage = isUpgraded ? 80 : 60;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			LivingEntity target = ev.getTarget();
			if (target == null) return TriggerResult.keep();
			if (data.getPlayer().getLocation().distance(target.getLocation()) > 5) return TriggerResult.keep();
			if (am.addCount(1) < 5) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata2, in2) -> {
				Player p2 = data.getPlayer();
				PreDealDamageEvent ev2 = (PreDealDamageEvent) in2;
				DamageMeta meta = ev2.getMeta();
				if (!meta.isBasicAttack()) return TriggerResult.keep();
				LivingEntity target2 = ev2.getTarget();
				if (target2 == null) return TriggerResult.keep();
				IProjectileInstance iProj = meta.getProjectile();
				if (iProj == null || !(iProj instanceof ProjectileInstance)) return TriggerResult.keep();
				if (p2.getLocation().distance(target2.getLocation()) <= range) {
					ProjectileInstance inst = (ProjectileInstance) iProj;
					meta.addDamageSlice(
							new DamageSlice(data, damage, DamageType.PIERCING, DamageStatTracker.of(id + slot, this)));
					inst.addPierce(1);
					Sounds.infect.play(p2, p2);
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_AXE,
				GlossaryTag.POWER.tag(this) + ". Activates after basic attacking " + DescUtil.white(5) + " enemies within " + DescUtil.white(5) + " blocks. Basic attack projectiles that hit an enemy within " + DescUtil.white(range) + " blocks "
						+ "pierce that enemy and deal an additional " + GlossaryTag.PIERCING.tag(this, damage, true)
						+ " damage.");
	}
}
