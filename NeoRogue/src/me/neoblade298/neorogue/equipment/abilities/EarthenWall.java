package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.LocalAxes;
import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EarthenWall extends Equipment {
	private static ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static ParticleContainer earth = new ParticleContainer(Particle.BLOCK_CRACK);
	private int duration = 20;
	private int stacksNeeded;
	
	static {
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		earth.count(1).spread(0.5, 0.5).blockData(Material.COARSE_DIRT.createBlockData());
	}
	
	public EarthenWall(boolean isUpgraded) {
		super("earthenWall", "Earthen Wall", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(100, 20, 10, 0));
		stacksNeeded = isUpgraded ? 6 : 9;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		EarthenWallInstance inst = new EarthenWallInstance(this, p);
		
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			if (((String) in[1]).equals("CONCUSSED")) {
				inst.addStacks((Integer) in[2]);
			}
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, bind, inst);
	}
	
	private class EarthenWallInstance extends EquipmentInstance {
		private int stacks = 0;
		public EarthenWallInstance(Equipment eq, Player p) {
			super(eq);
			action = (pdata, in) -> {
				Util.playSound(p, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1F, 1F, false);
				pc.spawn(p);
				HashMap<BuffType, Buff> wall = new HashMap<BuffType, Buff>();
				wall.put(BuffType.GENERAL, new Buff(p.getUniqueId(), 0, 0));
				pdata.getInstance().addUserBarrier(pdata,
						Barrier.stationary(p, 2, 3, 3, p.getLocation().add(0, 1.5, 0), LocalAxes.usingGroundedEyeLocation(p), wall, earth), duration);
				return TriggerResult.keep();
			};
		}
		
		public void addStacks(int amount) {
			this.stacks += amount;
		}
		
		@Override
		public boolean canTrigger(Player p, PlayerFightData data) {
			if (super.canTrigger(p, data)) {
				if (stacks < stacksNeeded) {
					Util.displayError(p, "Not enough stacks of concussed!");
					return false;
				}
				return true;
			}
			return false;
		}
		
		@Override
		public TriggerResult trigger(PlayerFightData data, Object[] inputs) {
			stacks -= stacksNeeded;
			return super.trigger(data, inputs);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COARSE_DIRT,
				"Can be cast once for every " + stacksNeeded + " stacks of concussed you apply."
				+ " Raises a wall size 3x3 that blocks projectiles for " + duration + " seconds.");
	}
}
