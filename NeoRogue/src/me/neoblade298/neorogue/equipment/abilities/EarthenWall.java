package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleUtil;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.HotbarCompatibleInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.Buff;
import me.neoblade298.neorogue.session.fights.BuffType;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class EarthenWall extends Ability {
	public EarthenWall(boolean isUpgraded) {
		super("earthenWall", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		setBaseProperties(20, 100, 0, 10);
		display = "Earthen Wall";
		item = createItem(this, Material.COARSE_DIRT, null,
				"Can be cast once for every 10 stacks of concussed you apply."
				+ " Raises a wall size 3x3 that blocks projectiles for 10 seconds.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		EarthenWallInstance inst = new EarthenWallInstance(this, p);
		
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			if (((String) in[1]).equals("CONCUSSED")) {
				inst.addStacks((Integer) in[2]);
			}
			return false;
		});
		
		data.addTrigger(id, bind, inst);
	}
	
	private class EarthenWallInstance extends HotbarCompatibleInstance {
		private Player p;
		private int stacks = 0;
		public EarthenWallInstance(Ability a, Player p) {
			super(a);
			this.p = p;
		}
		
		public void addStacks(int amount) {
			this.stacks += amount;
		}
		
		@Override
		public boolean canTrigger(Player p, PlayerFightData data) {
			if (super.canTrigger(p, data)) {
				if (stacks < 10) {
					Util.displayError(p, "Not enough stacks of concussed!");
					return false;
				}
				return true;
			}
			return false;
		}
		
		@Override
		public boolean trigger(PlayerFightData data, Object[] inputs) {
			stacks -= 10;
			return super.trigger(data, inputs);
		}
		
		@Override
		public boolean run(PlayerFightData data, Object[] inputs) {
			Util.playSound(p, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1F, 1F, false);
			ParticleUtil.spawnParticle(p, false, p.getLocation(), Particle.CLOUD, 50, 0.5, 0.5,
					0.5, 0.2);
			
			data.getInstance().addUserBarrier(data, new Barrier(null, 2, 3, 3, 0, new HashMap<BuffType, Buff>(), true), 10);
			return false;
		}
	}
}
