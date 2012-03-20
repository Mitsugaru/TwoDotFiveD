package com.bulletphysics.demos.bsp;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;

import javax.vecmath.Vector3f;

import org.bukkit.configuration.file.YamlConfiguration;

import com.ATeam.twoDotFiveD.debug.Logging;
import com.bulletphysics.util.ObjectArrayList;

public abstract class BspYamlConverter
{
	public void convertBspYaml(InputStream in) throws IOException
	{
		final YamlConfiguration config = YamlConfiguration
				.loadConfiguration(in);
		for (String rootPath : config.getKeys(false))
		{
			final ObjectArrayList<Vector3f> vertices = new ObjectArrayList<Vector3f>();
			final List<Object> list = config.getList(rootPath + ".vertices");
			Vector3f acceleration = null;
			if (list != null)
			{
				float mass = 0f;
				try
				{
					mass = Float.parseFloat(config.getString(
							rootPath + ".mass", "0f"));
				}
				catch (NumberFormatException e)
				{
					Logging.log.log(Level.SEVERE, "Failed to parse mass for: "
							+ rootPath, e);
				}
				try
				{
					String a = config.getString(rootPath + ".acceleration");
					if(a != null)
					{
						String[] ba = ((String) a).split(" ");
						acceleration = new Vector3f(Float.parseFloat(ba[0]), Float.parseFloat(ba[1]), Float.parseFloat(ba[2]));
					}
				}
				catch (Exception e)
				{
					acceleration = null;
				}
				for (Object o : list)
				{
					try
					{
						String[] b = ((String) o).split(" ");
						vertices.add(new Vector3f(Float.parseFloat(b[0]), Float
								.parseFloat(b[1]), Float.parseFloat(b[2])));
					}
					catch (Exception e)
					{
						Logging.log.log(
								Level.SEVERE,
								"Failed to parse the following line: "
										+ o.toString(), e);
					}
				}
				if (!vertices.isEmpty())
				{
					addConvexVerticesCollider(vertices, mass, acceleration);
				}
			}
			else
			{
				Logging.log.log(Level.SEVERE, "NullPointerException! Could not find vertices list for: " + rootPath);
			}
		}
		in.close();
	}
	
	public abstract void addConvexVerticesCollider(
			ObjectArrayList<Vector3f> vertices, float mass, Vector3f acceleration);
}