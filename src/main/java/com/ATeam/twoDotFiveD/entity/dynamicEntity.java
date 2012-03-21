package com.ATeam.twoDotFiveD.entity;

import lib.lwjgl.glmodel.GL_Vector;

/**
 * Replaced with Entity, which contains a RigidBody object.
 *
 */
@Deprecated
public class dynamicEntity extends Entity
{
    // Is a GL_Vector good for direction, or should something else be used?
    private GL_Vector direction;
    
    /**
     * Create Identity
     * 
     * @param position
     * @param size
     * @param ID
     * @param image
     */
    public dynamicEntity(
        GL_Vector position,
        GL_Vector size,
        int ID,
        String image )
    {
        //super( position, size, ID, image );
    	super(null, null);
    }
    //this can be removed later
    public dynamicEntity(){
        super(null, null);
    }

        /**
         * Gives direction
         * @return
         */
    public GL_Vector getDirection()
    {
        return direction;
    }


    /**
     * 
     * @param direction
     */
    public void setDirection( GL_Vector direction )
    {
        this.direction = direction;
    }

}
