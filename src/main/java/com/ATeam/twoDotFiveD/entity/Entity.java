package com.ATeam.twoDotFiveD.entity;

import lib.lwjgl.glmodel.GL_Vector;


public class Entity {
    private GL_Vector position;
    private GL_Vector size;
    private int ID;
    private String image;

    public Entity( GL_Vector position, GL_Vector size, int iD, String image )
    {
        this.position=position;
        this.size=size;
        this.ID=iD;
        this.image=image;
    }
	public Entity(){
	    //TODO kill this later because it breaks stuff.
	}
}
