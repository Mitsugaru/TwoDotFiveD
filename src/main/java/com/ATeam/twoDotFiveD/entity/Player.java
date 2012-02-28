package com.ATeam.twoDotFiveD.entity;

import lib.lwjgl.glmodel.GL_Vector;

public class Player extends Entity{

    public Player( GL_Vector position, GL_Vector size, int iD, String image )
    {
        super( position, size, iD, image );
        // TODO add name, etc
    }
    public Player(){
        super();
        //TODO this is a dummy constructor to not break code.
    }
	//TODO add attributes for a Player entity
}
