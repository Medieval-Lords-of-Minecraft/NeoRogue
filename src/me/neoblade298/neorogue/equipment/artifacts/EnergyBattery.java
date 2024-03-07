package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class EnergyBattery extends Artifact {
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORKS_SPARK).count(10).speed(0.1).spread(0.5, 0.5);
	private int num;

	public EnergyBattery() {
		super("energyBattery", "Energy Battery", Rarity.RARE, EquipmentClass.CLASSLESS);

		num = 2;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.PRE_CAST_USABLE, new CharmOfGallusInstance());
	}
	
	public class CharmOfGallusInstance implements TriggerAction {
		private int count = 0;

		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			if (count < num) {
				count++;
				CastUsableEvent ev = (CastUsableEvent) inputs;
				Player p = data.getPlayer();
				Sounds.success.play(p, p);
				part.play(p, p);
				EquipmentInstance eqi = ev.getInstance();
				eqi.setTempManaCost(0);
				eqi.setTempStaminaCost(0);
				eqi.setCooldown(-1);
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
		item = createItem(Material.COPPER_BULB, 
				"Your first <white>" + num + "</white> skills are free to cast and have no cooldown.");
	}
}
