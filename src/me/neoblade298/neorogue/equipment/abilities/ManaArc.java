package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class ManaArc extends Equipment {
	private static final String ID = "ManaArc";
	private int mana, damage, elec;
	private static final ParticleContainer pc = new ParticleContainer(Particle.FIREWORK);

	public ManaArc(boolean isUpgraded) {
		super(ID, "Mana Arc", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 0, 25, 0));
		mana = isUpgraded ? 5 : 6;
		damage = isUpgraded ? 90 : 60;
		elec = isUpgraded ? 45 : 30;
	}

	@Override
	public void setupReforges() {

	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			activate(p, data, am, slot);
			return TriggerResult.keep();
		}, (pl, pdata, in) -> {
			return !am.getBool(); // Only allow casting if it's not already active
		}));
	}

	private void activate(Player p, PlayerFightData data, ActionMeta am, int slot) {
		ManaArcProjectile proj = new ManaArcProjectile(data, slot, this);
		Sounds.equip.play(p, p);
		am.setBool(true);
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (pdata.getMana() <= mana) {
				Sounds.flap.play(p, p);
				am.setBool(false);
				return TriggerResult.remove();
			}
			pdata.addMana(-mana);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			if (!am.getBool())
				return TriggerResult.keep();

			DealDamageEvent ev = (DealDamageEvent) in;
			if (ev.getMeta().isSecondary())
				return TriggerResult.keep();
			LivingEntity trg = ev.getTarget();
			Vector dir = trg.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
			proj.start(data, p.getLocation().add(0, 1, 0), dir);
			return TriggerResult.keep();
		});
	}

	private class ManaArcProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private int slot;
		private Equipment eq;

		public ManaArcProjectile(PlayerFightData data, int slot, Equipment eq) {
			super(0.5, 12, 1);
			this.data = data;
			this.p = data.getPlayer();
			this.slot = slot;
			this.eq = eq;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Sounds.firework.play(p, proj.getLocation());
			FightInstance.applyStatus(hit.getEntity(), StatusType.ELECTRIFIED, data, elec, -1);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.addDamageSlice(
					new DamageSlice(data, damage, DamageType.LIGHTNING, DamageStatTracker.of(ID + slot, eq)));
			proj.getMeta().isSecondary(true);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHT_BLUE_BANNER,
				"On cast, lose " + DescUtil.yellow(mana) + " mana per second. Until you run out of mana, "
						+ "dealing damage fires a projectile at the target, dealing "
						+ GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage and applying "
						+ GlossaryTag.ELECTRIFIED.tag(this, elec, true) + ".");
	}
}
