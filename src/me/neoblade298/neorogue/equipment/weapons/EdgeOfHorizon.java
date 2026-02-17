package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class EdgeOfHorizon extends Bow {
	private static final String ID = "EdgeOfHorizon";
	private static final int MAX_FOCUS_STACKS = 10;
	private static final double BASE_NON_BASIC_BUFF = 0.30;
	private static final double NON_BASIC_BUFF_PER_FOCUS = 0.10;
	private int basicDamagePerFocus;
	
	public EdgeOfHorizon(boolean isUpgraded) {
		super(ID, "Edge of Horizon", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(60, 1, 0, 12, 0, 0));
		basicDamagePerFocus = isUpgraded ? 15 : 10;
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		// Standard bow shooting
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			if (!canShoot(data)) return TriggerResult.keep();
			useBow(data);

			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			ProjectileGroup proj = new ProjectileGroup(new BowProjectile(data, ev.getEntity().getVelocity(), this, id + slot));
			proj.start(data);
			return TriggerResult.keep();
		});
		
		// Increase basic attack projectile range by 4
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!ev.isBasicAttack()) return TriggerResult.keep();
			if (!(ev.getInstances().getFirst() instanceof ProjectileInstance)) return TriggerResult.keep();
			for (IProjectileInstance inst : ev.getInstances()) {
				((ProjectileInstance) inst).addMaxRange(4);
			}
			return TriggerResult.keep();
		});
		
		// Damage buffs based on focus stacks
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			int focusStacks = Math.min(MAX_FOCUS_STACKS, data.getStatus(StatusType.FOCUS).getStacks());
			
			if (ev.getMeta().isBasicAttack()) {
				// Basic attack: flat damage increase per focus stack
				if (focusStacks > 0) {
					ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
							Buff.increase(data, basicDamagePerFocus * focusStacks, StatTracker.damageBuffAlly(id + slot, this)));
				}
			} else {
				// Non-basic attack: 30% base + 10% per focus stack
				double totalMult = BASE_NON_BASIC_BUFF + (NON_BASIC_BUFF_PER_FOCUS * focusStacks);
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
						Buff.multiplier(data, totalMult, BuffStatTracker.damageBuffAlly(id + slot + 1, this)));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW,
				"Passive. Increases basic projectile range by <white>4</white> and non-basic damage by <white>30%</white>. " +
				"Every stack of " + GlossaryTag.FOCUS.tag(this) + " increases basic attack damage by " +
				DescUtil.yellow(basicDamagePerFocus) + " and non-basic damage by <white>10%</white>, up to <white>10</white> stacks.");
	}
}
