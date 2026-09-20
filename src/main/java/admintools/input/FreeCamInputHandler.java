package admintools.input;

import arc.Core;
import arc.input.KeyCode;
import arc.math.geom.Vec2;
import mindustry.Vars;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.input.MobileInput;
import mindustry.input.PlaceMode;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

/**
 * Enhanced mobile input processor providing decoupled camera controls (FreeCam) for administration.
 * Prevents the player unit from following the camera during inspection and spectator sweeps.
 */
public class FreeCamInputHandler extends MobileInput {
    private boolean freeCam = false;
    private final Vec2 lastPlayerPos = new Vec2();

    public FreeCamInputHandler() {
        super();
    }

    public FreeCamInputHandler(MobileInput source) {
        super();
        if (source != null) {
            this.mode = source.mode;
            this.rotation = source.rotation;
            this.down = source.down;
            this.manualShooting = source.manualShooting;
            this.selectPlans.addAll(source.selectPlans);
            this.linePlans.addAll(source.linePlans);
            this.targetPos.set(source.targetPos);
            this.movement.set(source.movement);
            this.lastBlock = source.lastBlock;
            this.lastPlaced = source.lastPlaced;
        }
    }

    public boolean isFreeCam() {
        return freeCam;
    }

    public void setFreeCam(boolean enabled) {
        if (this.freeCam == enabled) return;
        this.freeCam = enabled;
        if (enabled && Vars.player != null) {
            lastPlayerPos.set(Vars.player.x, Vars.player.y);
        }
    }

    public void toggleFreeCam() {
        setFreeCam(!freeCam);
    }

    /**
     * Instantly centers the camera back to the player unit.
     */
    public void centerOnPlayer() {
        Player p = Vars.player;
        if (p == null) return;
        Unit u = p.unit();
        if (u != null) {
            Core.camera.position.set(u.x, u.y);
        } else {
            Core.camera.position.set(p.x, p.y);
        }
        spectating = null;
    }

    @Override
    public void update() {
        super.update();

        if (freeCam && Vars.player != null) {
            movement.setZero();
            targetPos.set(lastPlayerPos);
        }
    }

    @Override
    protected void updateMovement(Unit unit) {
        if (!freeCam) {
            super.updateMovement(unit);
            return;
        }

        if (unit == null) return;

        // Anchor unit so it does not walk towards the panned camera
        lastPlayerPos.set(unit.x, unit.y);
        targetPos.set(unit.x, unit.y);
        movement.setZero();

        // Smoothly bring velocity to zero without physics glitching
        float drag = unit.type != null ? unit.type.accel : 1f;
        unit.vel.approachDelta(Vec2.ZERO, unit.speed() * drag / 2f);
        unit.movePref(Vec2.ZERO);

        // Allow directional aiming / weapon tracking without walking towards camera
        if (manualShooting) {
            unit.aim(Core.input.mouseWorldX(), Core.input.mouseWorldY(), true);
        } else {
            unit.lookAt(unit.prefRotation());
        }

        unit.controlWeapons(manualShooting, manualShooting);
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        // While placing blocks, selecting schematics or drawing lines, preserve building gestures
        if (freeCam && (mode != PlaceMode.none || lineMode || schematicMode || selecting || droppingItem)) {
            return super.pan(x, y, deltaX, deltaY);
        }

        if (!freeCam) {
            return super.pan(x, y, deltaX, deltaY);
        }

        // Ignore gestures over active UI scene elements (windows, dock, dialogs)
        if (Core.scene != null && Core.scene.hasMouse(x, y)) {
            return false;
        }

        float scale = Core.camera.width / (float) Core.graphics.getWidth();
        Core.camera.position.x -= deltaX * scale;
        Core.camera.position.y -= deltaY * scale;
        spectating = null;

        // Clamp within map bounds + extended margin for edge visibility
        float margin = tilesize * 8f;
        Core.camera.position.clamp(
            -margin, -margin,
            world.unitWidth() + margin,
            world.unitHeight() + margin
        );

        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, KeyCode button) {
        if (freeCam) {
            shiftDeltaX = shiftDeltaY = 0f;
            return true;
        }
        return super.panStop(x, y, pointer, button);
    }

    @Override
    public void spectate(Unit unit) {
        if (freeCam && unit != null) {
            Core.camera.position.set(unit.x, unit.y);
        }
        super.spectate(unit);
    }
}
