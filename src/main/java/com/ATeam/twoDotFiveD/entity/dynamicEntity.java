package com.ATeam.twoDotFiveD.entity;

import lib.lwjgl.glmodel.GL_Vector;


public class dynamicEntity extends Entity
{
    public dynamicEntity(
        GL_Vector position,
        GL_Vector size,
        int ID,
        String image )
    {
        super( position, size, ID, image );
    }

    public GL_Vector getDirection()
    {
        return direction;
    }

    public void setDirection( GL_Vector direction )
    {
        this.direction = direction;
    }

    // Is a GL_Vector good for direction, or should something else be used?
    private GL_Vector direction;
}
