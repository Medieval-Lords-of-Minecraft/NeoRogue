package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Quake extends Equipment {
	private static final String ID = "Quake";
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	private int concussed, damage;
	// Crisp dirt ring marking the exact edge of the AOE
	private static final Circle circ = new Circle(tp.range);
	private static final ParticleContainer ring = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DIRT.createBlockData()).count(1).spread(0, 0).speed(0).offsetY(0.1);
	// Light dust fill so the whole affected zone reads clearly
	private static final ParticleContainer ringFill = new ParticleContainer(Particle.CLOUD)
			.count(1).spread(0.1, 0).speed(0).offsetY(0.1);
	// Central ground burst to telegraph the cast (intentional heavy-impact FX)
	private static final ParticleContainer burst = new ParticleContainer(Particle.EXPLOSION_EMITTER)
			.count(2).spread(0.6, 0.1);
	private static final ParticleContainer debris = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DIRT.createBlockData()).count(60).spread(tp.range * 0.6, 0.2).speed(0.08).offsetY(0.3);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_WARDEN_ATTACK_IMPACT);
	private static final SoundContainer ground = new SoundContainer(Sound.BLOCK_ROOTED_DIRT_BREAK, 1.6F, 0.6F);
	
	public Quake(boolean isUpgraded) {
		super(ID, "Quake", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 20, 8, tp.range));
		
		concussed = isUpgraded ? 12 : 8;
		damage = isUpgraded ? 150 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pd, in) -> {
			Player p = data.getPlayer();
			sc.play(p, p);
			ground.play(p, p);
			circ.play(ring, p.getLocation(), LocalAxes.xz(), ringFill);
			burst.play(p, p);
			debris.play(p, p);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN, DamageStatTracker.of(id + slot, this)), ent);
				FightInstance.applyStatus(ent, StatusType.CONCUSSED, p, concussed, -1, this);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DIRT,
				"On cast, deal " + DescUtil.val(damage) + " " + GlossaryTag.EARTHEN.tag(this) + " damage to all "
						+ "nearby enemies and apply " + GlossaryTag.CONCUSSED.tag(this, concussed) + ".");
	}
}
