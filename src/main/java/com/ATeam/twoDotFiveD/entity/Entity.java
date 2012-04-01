package com.ATeam.twoDotFiveD.entity;

import java.io.Serializable;

import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.RigidBody;

public class Entity implements Serializable
{
	//TODO need local owner ID to differentiate which client made what Entity
	private String		ID				= "UNKNOWN";
	private RigidBody	rigidBody;
	private String[]	description = {"NONE"};
	private boolean		frozen			= false;
	// Old state, for when we "freeze" a RigidBody by setting the following to
	// zero
	private Vector3f	gravity			= new Vector3f(0f, 0f, 0f);
	private Vector3f	linearVelocity	= new Vector3f(0f, 0f, 0f);
	private Vector3f	angularVeloctiy	= new Vector3f(0f, 0f, 0f);
	private float angularFactor = 0f;
	private int cooldown = 0;
	
	// TODO implement
	// Might need an array of images, images per face? IDK
	private String		image;
	
	public Entity(String ID, RigidBody rigidBody, String image, String[] description)
	{
		this.ID = ID;
		this.rigidBody = rigidBody;
		this.image = image;
		if(description != null)
		{
			this.description = description;
		}
	}
	
	public Entity(String ID, RigidBody rigidBody, String[] description)
	{
		this.ID = ID;
		this.rigidBody = rigidBody;
		if(description != null)
		{
			this.description = description;
		}
	}
	
	public Entity(String ID, RigidBody rigidBody, String image)
	{
		this.ID = ID;
		this.rigidBody = rigidBody;
		this.image = image;
	}
	
	public Entity(String ID, RigidBody rigidBody)
	{
		this.ID = ID;
		this.rigidBody = rigidBody;
	}
	
	public void freeze()
	{
		if (!frozen && cooldown <= 0)
		{
			// Save state
			linearVelocity = rigidBody.getLinearVelocity(new Vector3f());
			angularVeloctiy = rigidBody.getAngularVelocity(new Vector3f());
			angularFactor = rigidBody.getAngularFactor();
			// Freeze object movement
			rigidBody.setLinearVelocity(new Vector3f(0f, 0f, 0f));
			rigidBody.setGravity(new Vector3f(0f, 0f, 0f));
			rigidBody.setAngularVelocity(new Vector3f(0f, 0f, 0f));
			rigidBody.setAngularFactor(0f);
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
		if(frozen)
		{
			//Resume movement
			rigidBody.setLinearVelocity(linearVelocity);
			rigidBody.setAngularVelocity(angularVeloctiy);
			rigidBody.setAngularFactor(angularFactor);
			rigidBody.setGravity(gravity);
			rigidBody.activate();
			frozen = false;
			cooldown = 5;
		}
	}
	
	public boolean isFrozen()
	{
		return frozen;
	}
	
	public void setGravity(Vector3f gravity)
	{
		this.gravity = gravity;
	}
	
	public Vector3f getGravity()
	{
		return gravity;
	}
	
	public String getID()
	{
		return ID;
	}
	
	public RigidBody getRigidBody()
	{
		return rigidBody;
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
