package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class AvalonianAnchor extends Artifact {
	private ParticleContainer part = new ParticleContainer(Particle.CRIT).count(10).spread(0.5, 0.5).speed(0.1);
	public AvalonianAnchor() {
		super("avalonianAnchor", "Avalonian Anchor", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			if (p.getVelocity().getY() < 0) {
				BasicAttackEvent ev = (BasicAttackEvent) in;
				if (ev.isProjectile()) return TriggerResult.keep();
				Util.playSound(p, Sound.ENTITY_PLAYER_ATTACK_CRIT, false);
				part.spawn(ev.getTarget());
				ev.getMeta().addBuff(BuffType.GENERAL, new Buff(p.getUniqueId(), 0, 0.5), BuffOrigin.NORMAL, true);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RESPAWN_ANCHOR,
				"Landing a melee basic attack while falling increases its damage by <yellow>50%</yellow>");
	}
}
