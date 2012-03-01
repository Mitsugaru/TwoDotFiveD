package com.ATeam.twoDotFiveD.entity;

import lib.lwjgl.glmodel.GL_Vector;


public class Player extends dynamicEntity
{

    private String name;


    /**
     * 
     * @return players name
     */
    public String getName()
    {
        return name;
    }


    /**
     * 
     * @param name
     *            Name to set
     */
    public void setName( String name )
    {
        this.name = name;
    }


    /**
     * Constructor
     * 
     * @param position
     * @param size
     * @param iD
     * @param image
     * @param name
     */
    public Player(
        GL_Vector position,
        GL_Vector size,
        int iD,
        String image,
        String name )
    {
        super( position, size, iD, image );
        // TODO add name, etc
    }


    public Player()
    {
        super();
        // TODO this is a dummy constructor to not break code.
    }
    // TODO add attributes for a Player entity
}
