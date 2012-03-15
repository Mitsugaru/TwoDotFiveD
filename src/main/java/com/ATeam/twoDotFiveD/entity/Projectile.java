package com.ATeam.twoDotFiveD.entity;

import lib.lwjgl.glmodel.GL_Vector;


public class Projectile extends dynamicEntity
{
    // Damage might need to be double, but for simple things can be int
    private int damage;

    private boolean splash;

    private boolean reflective;


    /**
     * 
     * @param position
     * @param size
     * @param ID
     * @param image
     * @param splash
     *            Whether or not this projectile does splash damage.
     * @param reflective
     *            If this projectile will 'bounce' off of reflective materials.
     */
    public Projectile(
        GL_Vector position,
        GL_Vector size,
        int ID,
        String image,
        boolean splash,
        boolean reflective )
    {
        super( position, size, ID, image );
    }


    /**
     * 
     * @param splash
     *            Whether this projectile causes splash damage
     */
    public void setSplash( boolean splash )
    {
        this.splash = splash;
    }


    /**
     * 
     * @param reflective
     *            Whether the object will 'bounce' off reflective objects
     */
    public void setReflective( boolean reflective )
    {
        this.reflective = reflective;
    }


    /**
     * 
     * @return The damage this object causes
     */
    public int getDamage()
    {
        return damage;
    }


    /**
     * 
     * @param damage
     *            The damage this object can cause
     */
    public void setDamage( int damage )
    {
        this.damage = damage;
    }


    /**
     * 
     * @return Whether this projectile causes splash damage.
     */
    public boolean isSplash()
    {
        return splash;
    }


    /**
     * 
     * @return Whether this projectile 'bounces' off of reflective wallss
     */
    public boolean isReflective()
    {
        return reflective;
    }

}
