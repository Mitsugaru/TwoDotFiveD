package com.ATeam.twoDotFiveD.event.player;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;

import com.ATeam.twoDotFiveD.entity.Entity;
import com.bulletphysics.linearmath.Transform;

public class PlayerMoveEvent extends PlayerEvent {
    public Transform transform;

    public PlayerMoveEvent(final Entity player) {
	super(Type.PLAYER_MOVE, player);
	this.transform = player.getWorldTransform(new Transform());
    }
    
    public PlayerMoveEvent(final Entity player, Transform transform)
    {
	super(Type.PLAYER_MOVE, player);
	this.transform = transform;
    }

    public void notify(PlayerListener listener) {
	listener.onPlayerMove(this);
    }
    
    public Transform getTransform()
    {
	return transform;
    }

    @Override
    public Map<String, Object> getData() {
	final Map<String, Object> data = new HashMap<String, Object>();
	data.put("class", name);
	data.put("player.ID", getPlayer().getID());
	final Matrix4f transformMatrix = transform
		.getMatrix(new Matrix4f());
	data.put("entity.rigidbody.motionstate.transform", new float[] {
		transformMatrix.m00, transformMatrix.m01, transformMatrix.m02,
		transformMatrix.m03, transformMatrix.m10, transformMatrix.m11,
		transformMatrix.m12, transformMatrix.m13, transformMatrix.m20,
		transformMatrix.m21, transformMatrix.m22, transformMatrix.m23,
		transformMatrix.m30, transformMatrix.m31, transformMatrix.m32,
		transformMatrix.m33 });
	return data;
    }

}
