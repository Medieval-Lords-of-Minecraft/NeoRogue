package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class PracticeDummy extends Artifact {
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORKS_SPARK).count(10).speed(0.1).spread(0.5, 0.5);
	private int num;
	private double damageMult;

	public PracticeDummy() {
		super("practice Dummy", "Practice Dummy", Rarity.RARE, EquipmentClass.CLASSLESS);

		num = 25;
		damageMult = 0.4;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.BASIC_ATTACK, new PracticeDummyInstance());
	}
	
	public class PracticeDummyInstance implements TriggerAction {
		private int count = 0;
		private String weapon = null;

		@Override
		public TriggerResult trigger(PlayerFightData data, Object[] inputs) {
			Equipment eq = (Equipment) inputs[4];
			DamageType type = eq.getProperties().getType();
			double amount = (double) inputs[1];
			if (eq.getId().equals(weapon)) {
				count = 0;
				return TriggerResult.keep();
			}
			
			Player p = data.getPlayer();
			count++;
			if (count == num) {
				Util.playSound(data.getPlayer(), Sound.ENTITY_ARROW_HIT_PLAYER, false);
				part.spawn(p);
				Util.msg(p, "<red>Practice Dummy</red> was activated");
			}
			if (count > num) {
				DamageMeta meta = new DamageMeta(amount * damageMult, type, true);
				FightInstance.dealDamage(p, meta, (Damageable) inputs[0]); 
				return TriggerResult.keep();
			}
			return TriggerResult.remove();
		}
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		int pct = (int) (damageMult * 100);
		item = createItem(Material.TARGET, 
				"Landing <yellow>" + num + "</yellow> basic attacks in a row with the same weapon will empower that weapon to do <yellow>" + pct + "%</yellow> bonus "
				+ "damage on basic attacks until you use a different weapon.");
	}
}
