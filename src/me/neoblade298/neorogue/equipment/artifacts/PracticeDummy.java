package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class PracticeDummy extends Artifact {
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORKS_SPARK).count(10).speed(0.1).spread(0.5, 0.5);
	private int num;
	private double damageMult;

	public PracticeDummy() {
		super("practiceDummy", "Practice Dummy", Rarity.RARE, EquipmentClass.CLASSLESS);

		num = 15;
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
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			BasicAttackEvent ev = (BasicAttackEvent) inputs;
			Equipment eq = ev.getWeapon();
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
				ev.getMeta().addBuff(BuffType.GENERAL, new Buff(p.getUniqueId(), 0, 0.5), BuffOrigin.NORMAL, true);
				return TriggerResult.keep();
			}
			return TriggerResult.remove();
		}
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		int pct = (int) (damageMult * 100);
		item = createItem(Material.TARGET, 
				"Landing <white>" + num + "</white> basic attacks in a row with the same weapon will empower that weapon to do <white>" + pct + "%</white> bonus "
				+ "damage on basic attacks until you use a different weapon.");
	}
}
