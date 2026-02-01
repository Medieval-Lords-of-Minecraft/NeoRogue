package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Groundbreaker extends Equipment {
	private static final String ID = "Groundbreaker";
	private static final TargetProperties tp = TargetProperties.radius(2, true);
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
		explode = new ParticleContainer(Particle.EXPLOSION).spread(tp.range / 2, 0);
	private static final Circle circ = new Circle(tp.range);
	private int conc;
	
	public Groundbreaker(boolean isUpgraded) {
		super(ID, "Groundbreaker", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 200 : 160, 0.5, 0.4, DamageType.BLUNT, new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5F)));
		properties.addUpgrades(PropertyType.DAMAGE);
		conc = isUpgraded ? 80 : 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_BLOCK, (pdata, in) -> {
			if (!data.canBasicAttack())
				return TriggerResult.keep();
			PlayerInteractEvent ev = (PlayerInteractEvent) in;
			Block b = ev.getClickedBlock();
			Location loc = b.getLocation().add(0.5, 0, 0.5);
			circ.play(pc, loc, LocalAxes.xz(), null);
			Player p = data.getPlayer();
			weaponSwing(p, data);
			data.addTask(new BukkitRunnable() {
				public void run() {
					circ.play(pc, loc, LocalAxes.xz(), explode);
					Sounds.explode.play(p, loc);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
						weaponDamage(p, data, ent);
						FightInstance.applyStatus(ent, StatusType.CONCUSSED, data, conc, -1);
					}
				}
			}.runTaskLater(NeoRogue.inst(), 20));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MACE, "Only works on blocks. Left clicking a block will deal damage and apply " + 
		GlossaryTag.CONCUSSED.tag(this, conc, true) + " to enemies near the block after <white>1s</white>.");
	}
}
