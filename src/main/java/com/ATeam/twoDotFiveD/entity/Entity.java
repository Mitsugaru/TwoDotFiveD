package com.ATeam.twoDotFiveD.entity;

import java.io.Serializable;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MotionState;

public class Entity extends RigidBody implements Serializable
{
	/**
	 * 
	 */
	private static final long	serialVersionUID		= 2363764575766376861L;
	// TODO need local owner ID to differentiate which client made what Entity
	private String				ID						= "UNKNOWN";
	private String[]			description				= { "NONE" };
	private boolean				frozen					= false;
	// Old state, for when we "freeze" a RigidBody by setting the following to
	// zero
	private Vector3f			entityGravity			= new Vector3f(0f, 0f,
																0f);
	private Vector3f			entityLinearVelocity	= new Vector3f(0f, 0f,
																0f);
	private Vector3f			entityAngularVeloctiy	= new Vector3f(0f, 0f,
																0f);
	private float				entityAngularFactor		= 0f;
	private int					cooldown				= 0;
	
	// TODO implement
	// Might need an array of images, images per face? IDK
	private String				image;
	
	public Entity(float mass, MotionState state, CollisionShape shape,
			Vector3f localInertia, String ID, String image, String[] description)
	{
		super(mass, state, shape, localInertia);
		this.ID = ID;
		if (image != null)
		{
			this.image = image;
		}
		if (description != null)
		{
			this.description = description;
		}
	}
	
	public Entity(RigidBodyConstructionInfo info, String ID, String image,
			String[] description)
	{
		super(info);
		this.ID = ID;
		this.image = image;
		this.description = description;
	}
	
	public void freeze()
	{
		if (!frozen && cooldown <= 0)
		{
			// Save state
			entityLinearVelocity = this.getLinearVelocity(new Vector3f());
			entityAngularVeloctiy = this.getAngularVelocity(new Vector3f());
			entityAngularFactor = this.getAngularFactor();
			// Freeze object movement
			this.setLinearVelocity(new Vector3f(0f, 0f, 0f));
			this.setGravity(new Vector3f(0f, 0f, 0f));
			this.setAngularVelocity(new Vector3f(0f, 0f, 0f));
			this.setAngularFactor(0f);
			frozen = true;
			cooldown = 5;
		}
		else
		{
			cooldown--;
		}
	}
	
	public void unfreeze()
	{
		if (frozen)
		{
			// Resume movement
			this.setLinearVelocity(entityLinearVelocity);
			this.setAngularVelocity(entityAngularVeloctiy);
			this.setAngularFactor(entityAngularFactor);
			this.setGravity(entityGravity);
			this.activate();
			frozen = false;
			cooldown = 5;
		}
	}
	
	public boolean isFrozen()
	{
		return frozen;
	}
	
	public void setFrozen(boolean f)
	{
		frozen = f;
	}
	
	public void setEntityGravity(Vector3f gravity)
	{
		this.entityGravity = gravity;
		this.setGravity(gravity);
	}
	
	public Vector3f getEntityGravity()
	{
		return entityGravity;
	}
	
	public String getID()
	{
		return ID;
	}
	
	public String getImage()
	{
		return image;
	}
	
	public String[] getDescription()
	{
		return description;
	}
}
