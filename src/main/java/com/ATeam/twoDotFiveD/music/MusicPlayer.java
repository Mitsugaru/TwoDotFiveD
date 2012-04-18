package com.ATeam.twoDotFiveD.music;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.ATeam.twoDotFiveD.TwoDotFiveDBsp;
import com.ATeam.twoDotFiveD.entity.Entity;
import com.ATeam.twoDotFiveD.event.EventDispatcher;
import com.ATeam.twoDotFiveD.event.Event.Type;
import com.ATeam.twoDotFiveD.event.block.BlockCollisionEvent;
import com.ATeam.twoDotFiveD.event.block.BlockCollisionResolvedEvent;
import com.ATeam.twoDotFiveD.event.block.BlockCreateEvent;
import com.ATeam.twoDotFiveD.event.block.BlockDestroyedEvent;
import com.ATeam.twoDotFiveD.event.block.BlockListener;
import com.ATeam.twoDotFiveD.event.block.BlockMoveEvent;
import com.ATeam.twoDotFiveD.event.block.BlockPhysicsChangeEvent;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.dynamics.RigidBody;

import lib.lwjgl.glsound.*;

public class MusicPlayer
{
	Runnable					runn	= new MusicPlayerRun();
	EventDispatcher				dispatcher;
	
	Scanner						in		= new Scanner(System.in);
	static ArrayList<String>	sound	= new ArrayList<String>();
	// static ArrayList<String> music=new ArrayList<String>();//later when
	// focusing on levels
	static MusicPlayerRun[]		ths;
	
	/*public static void main(String[] args)
	{
		
		Scanner in = new Scanner(System.in);
		SoundScape.create();
		boolean loop = true;
		
		getSound();
		
		ths = new MusicPlayerRun[sound.size()];
		int num = 0;// default
		String toDo;
		
		while (loop)
		{
			num = in.nextInt();// what noise
			toDo = in.next();// play, pause, stop
			
			if (toDo.equals("play"))
			{
				ths[num] = new MusicPlayerRun();
				ths[num].start();// start a thread
				ths[num].setSong(sound.get(num));
			}
			if (toDo.equals("pause"))
			{
				ths[num].pause();
			}
			if (toDo.equals("stop"))
			{
				ths[num].setStop("quit");
			}
			if (toDo.equals("vol"))
			{
				changeVol(ths[num]);
			}
			if (toDo.equals("pit"))
			{
				changePitch(ths[num]);
			}
			if (num == 999)
			{
				loop = false;
			}
			toDo = null;
			num = -1;
			// changeVol(ths[num]);
		}
		
	}*/
	
	public MusicPlayer(EventDispatcher dispatcher)
	{
		this.dispatcher = dispatcher;
		BlockListener blockListener = new BlockListener() {
			
			@Override
			public void onBlockDestroyed(BlockDestroyedEvent blockDestroyedEvent)
			{
				// TODO Auto-generated method stub
				super.onBlockDestroyed(blockDestroyedEvent);
			}
			
			@Override
			public void onBlockCreate(BlockCreateEvent blockCreateEvent)
			{
				// TODO Auto-generated method stub
				super.onBlockCreate(blockCreateEvent);
			}
			
			@Override
			public void onBlockMove(BlockMoveEvent blockMoveEvent)
			{
				// TODO Auto-generated method stub
				super.onBlockMove(blockMoveEvent);
			}
			
			@Override
			public void onBlockPhysicsChange(
					BlockPhysicsChangeEvent blockPhysicsChangeEvent)
			{
				// TODO Auto-generated method stub
				super.onBlockPhysicsChange(blockPhysicsChangeEvent);
			}
			
			@Override
			public void onBlockCollision(BlockCollisionEvent blockCollisionEvent)
			{
				final PersistentManifold pm = blockCollisionEvent
						.getPersistentManifold();
				if (pm.getBody0() instanceof RigidBody
						&& pm.getBody1() instanceof RigidBody)
				{
					final Entity entityA = TwoDotFiveDBsp.entityList
							.get((RigidBody) pm.getBody0());
					final Entity entityB = TwoDotFiveDBsp.entityList
							.get((RigidBody) pm.getBody1());
					if (entityA != null && entityB != null)
					{
						//System.out.println("A: " + entityA.getID() + " B: " + entityB.getID());
						if (entityA.getID().contains("Box")
								|| entityB.getID().contains("Box"))
						{
							MusicPlayerRun mpr = new MusicPlayerRun();
							// System.out.println(getClass().getResource("hit.wav").getFile());
							mpr.setSong("com/ATeam/twoDotFiveD/music/hit.wav");
						}
						//TODO other sounds for other types
					}
				}
			}
			
			@Override
			public void onBlockCollisionResolved(
					BlockCollisionResolvedEvent blockCollisionResolvedEvent)
			{
				
			}
			
		};
		// Register listeners to events;
		dispatcher.registerListener(Type.BLOCK_COLLISION, blockListener);
	}
	
	public void createThread(int i)
	{// problem
		ths[i] = new MusicPlayerRun();
		ths[i].start();// start a thread
		ths[i].setSong(sound.get(i));
		System.out.println(sound.get(i));
	}
	
	public static void getSound()
	{
		try
		{
			FileReader reader = new FileReader("Sounds.txt");
			Scanner scanner = new Scanner(reader);
			while (scanner.hasNextLine())
			{
				sound.add(scanner.nextLine());
			}
			scanner.close();
		}
		catch (IOException e)
		{
		}
		
	}
	
	public static void changeVol(MusicPlayerRun ths2)
	{
		float vol = 1;
		try
		{
			FileReader reader = new FileReader("Sounds.txt");
			Scanner scanner = new Scanner(reader);
			vol = scanner.nextFloat();
			System.out.println(vol);
			scanner.close();
		}
		catch (IOException e)
		{
		}
		ths2.changeVolume(vol);
	}
	
	public static void changePitch(MusicPlayerRun ths2)
	{
		float pit = 1;// 1 is default value
		try
		{
			FileReader reader = new FileReader("Sounds.txt");
			Scanner scanner = new Scanner(reader);
			pit = scanner.nextFloat();
			scanner.close();
		}
		catch (IOException e)
		{
		}
		ths2.changePitch(pit);
		System.out.println(pit);
	}
	
}
