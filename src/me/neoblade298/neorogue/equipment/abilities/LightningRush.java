package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class LightningRush extends Equipment {
	private static final String ID = "lightningRush";
	private int damage, elec;
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.YELLOW, 1F)).count(50).spread(1, 2).offsetY(1);
	
	public LightningRush(boolean isUpgraded) {
		super(ID, "Lightning Rush", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, 
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 10, 15, 0));
		damage = isUpgraded ? 90 : 60;
		elec = isUpgraded ? 40 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				"For <white>3</white> seconds, your basic attacks"
				+ " grant speed <white>1</white> [<white>1s</white>],"
				+ " deals an additional " + GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage, applies "
				+ GlossaryTag.ELECTRIFIED.tag(this, elec, true) + ", and extends the duration"
				+ " of the skill by <white>1</white> second. Once per enemy.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		LightningRushInstance inst = new LightningRushInstance(data, this, slot, es);
		data.addTrigger(id, bind, inst);
		data.addTrigger(ID, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (!inst.active) return TriggerResult.keep();
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			LivingEntity trg = ev.getTarget();
			UUID uuid = trg.getUniqueId();
			if (inst.hit.contains(uuid)) return TriggerResult.keep();
			Sounds.extinguish.play(p, trg);
			pc.play(p, trg);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 0));
			inst.hit.add(uuid);
			ev.getMeta().addDamageSlice(new DamageSlice(pdata, damage, DamageType.LIGHTNING));
			FightInstance.applyStatus(trg, StatusType.ELECTRIFIED, pdata, elec, -1);
			inst.timer++;
			return TriggerResult.keep();
		});
	}
	
	private class LightningRushInstance extends EquipmentInstance {
		private HashSet<UUID> hit = new HashSet<UUID>();
		private boolean active = false;
		private int timer;
		
		public LightningRushInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			action = (pdata, in) -> {
				active = true;
				timer = 3;
				hit.clear();
				Sounds.blazeDeath.play(p, p);
				pdata.addTask(new BukkitRunnable() {
					public void run() {
						if (--timer <= 0) {
							active = false;
							this.cancel();
						}
					}
				}.runTaskTimer(NeoRogue.inst(), 20L, 20L));
				return TriggerResult.keep();
			};
		}

	}
}
