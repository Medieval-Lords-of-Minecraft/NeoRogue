package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Darkness extends Equipment {
	private static final String ID = "darkness";
	private int dark, insanity;
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private static final Circle circle = new Circle(tp.range);
	private static final SoundContainer sound = new SoundContainer(Sound.ENTITY_ALLAY_HURT),
			darkSound = new SoundContainer(Sound.ENTITY_ELDER_GUARDIAN_HURT);
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
			.count(3).spread(0.1, 0.1).dustOptions(new DustOptions(Color.BLACK, 1F));
	
	public Darkness(boolean isUpgraded) {
		super(ID, "Darkness", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 0, 12, 0));
		dark = isUpgraded ? 15 : 10;
		insanity = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		DarknessInstance inst = new DarknessInstance(data, this, slot, es);
		data.addTrigger(id, bind, inst);
		data.addTrigger(ID, Trigger.BASIC_ATTACK, (pdata, in) -> {
			if (!inst.basicAttack) return TriggerResult.keep();
			BasicAttackEvent ev = (BasicAttackEvent) in;
			FightInstance.applyStatus(ev.getTarget(), StatusType.INSANITY, data, insanity, -1);
			sound.play(p, p);
			return TriggerResult.keep();
		});
	}
	
	private class DarknessInstance extends EquipmentInstance {
		private Location loc;
		private boolean basicAttack = false;
		public DarknessInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			action = (pdata, in) -> {
				cast(pdata);
				return TriggerResult.keep();
			};
		}
		
		private void cast(PlayerFightData pdata) {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			basicAttack = true;
			loc = p.getLocation();
			pdata.addTask(new BukkitRunnable() {
				private int count;
				public void run() {
					if (++count > 5) {
						this.cancel();
						basicAttack = false;
					}
					
					circle.play(pc, loc, LocalAxes.xz(), null);
					darkSound.play(p, loc);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
						FightInstance.dealDamage(pdata, DamageType.DARK, dark, ent);
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 20L, 20L));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				"On cast, drop a bomb on the ground that deals " + GlossaryTag.DARK.tag(this, dark, true) + " damage per second for <white>5</white> seconds."
				+ " During this time, your basic attacks apply " + GlossaryTag.INSANITY.tag(this, insanity, true) + ".");
	}
}
