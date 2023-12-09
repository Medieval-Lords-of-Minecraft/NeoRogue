package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.HotbarCompatibleInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.BuffType;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class BattleCry extends Ability {
	private ParticleContainer pc = new ParticleContainer(Particle.REDSTONE);
	
	public BattleCry(boolean isUpgraded) {
		super("battleCry", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		display = "Battle Cry";
		cooldown = 30;
		staminaCost = 25;
		int strength = isUpgraded ? 20 : 14;
		item = createItem(this, Material.REDSTONE, null,
				"On cast, give yourself <yellow>" + strength + " </yellow>bonus physical damage for <yellow>10</yellow> seconds.");
		
		pc.count(50).offset(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addHotbarTrigger(id, slot, bind, new BattleCryInstance(this, p));
	}
	
	private class BattleCryInstance extends HotbarCompatibleInstance {
		private Player p;
		public BattleCryInstance(Ability a, Player p) {
			super(a);
			this.p = p;
			this.cooldown = a.getCooldown();
		}
		
		@Override
		public boolean run(PlayerFightData data, Object[] inputs) {
			Util.playSound(p, Sound.ENTITY_BLAZE_DEATH, 1F, 1F, false);
			pc.spawn(p);
			data.addBuff(p.getUniqueId(), id, true, false, BuffType.PHYSICAL, isUpgraded ? 20 : 14, 10);
			return true;
		}
	}
}
