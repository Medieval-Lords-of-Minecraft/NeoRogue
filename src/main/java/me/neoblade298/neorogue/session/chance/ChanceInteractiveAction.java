package me.neoblade298.neorogue.session.chance;

import me.neoblade298.neorogue.player.PlayerSessionData;

// An interactive choice handler. Unlike ChanceAction, which resolves synchronously and
// immediately advances the stage, this opens a UI and is responsible for the FULL commit:
//   - on confirm: perform side effects, then call inst.advanceStage(...) + updateBoardLines()
//   - on cancel: reopen the previous chance inventory without advancing the stage
public interface ChanceInteractiveAction {
	public void open(ChanceInventory prev, PlayerSessionData data);
}
