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
			final String localScaling = config.getString(rootPath
					+ ".localscaling");
			/**
			 * Get usual stuff
			 */
			float mass = 0f;
			try
			{
				mass = Float.parseFloat(config.getString(rootPath + ".mass",
						"0f"));
				if (mass < 0)
				{
					Logging.log
							.log(Level.WARNING,
									"Mass for '"
											+ rootPath
											+ "' is negative => Defaulting to static object. Fix.");
					mass = 0f;
				}
			}
			catch (NumberFormatException e)
			{
				Logging.log.log(Level.SEVERE, "Failed to parse mass for: "
						+ rootPath, e);
			}
			Vector3f acceleration = null;
			try
			{
				String a = config.getString(rootPath + ".acceleration");
				if (a != null)
				{
					String[] ba = ((String) a).split(" ");
					acceleration = new Vector3f(Float.parseFloat(ba[0]),
							Float.parseFloat(ba[1]), Float.parseFloat(ba[2]));
				}
			}
			catch (NumberFormatException e)
			{
				Logging.log.log(Level.SEVERE,
						"Failed to parse acceleration for: " + rootPath, e);
				acceleration = null;
			}
			catch (NullPointerException e)
			{
				// Ignore, as probably missing
				acceleration = null;
			}
			catch(ArrayIndexOutOfBoundsException a)
			{
				Logging.log.log(Level.WARNING,"Bad parse for: "+ rootPath);
			}
			String[] description = null;
			try
			{
				description = config.getList(rootPath + ".description")
						.toArray(new String[0]);
			}
			catch (NullPointerException e)
			{
				// Ignore, as it probably has no description
			}
			String image = config.getString(rootPath + ".image");
			if (list != null)
			{
				for (Object o : list)
				{
					try
					{
						String[] b = ((String) o).split(" ");
						vertices.add(new Vector3f(Float.parseFloat(b[0]) * 5f, Float
								.parseFloat(b[1])* 5f, Float.parseFloat(b[2])* 5f));
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
					// Add physics object to dynamic world
					addConvexVerticesCollider(rootPath, vertices, mass,
							acceleration, image, description);
				}
				else
				{
					Logging.log.log(Level.WARNING, "Object at path '"
							+ rootPath + "' has no vertices.");
				}
			}
			else if (localScaling != null)
			{
				//TODO basic shapes that are not of convex hull shape
				final String shapeType = config.getString(rootPath + ".shape");
				if(shapeType == null)
				{
					Logging.log.log(Level.WARNING, "Object at path '"
							+ rootPath + "' has no shape type defined.");
				}
				else
				{
					try
					{
						final String[] l = localScaling.split(" ");
						final Vector3f scale = new Vector3f(Float.parseFloat(l[0]), Float
								.parseFloat(l[1]), Float.parseFloat(l[2]));
						final String origin = config.getString(rootPath + ".origin");
						Vector3f originTransform = new Vector3f(0f, 0f, 0f);
						if(origin != null)
						{
							final String[] t = origin.split(" ");
							originTransform = new Vector3f(Float.parseFloat(t[0]), Float
								.parseFloat(t[1]), Float.parseFloat(t[2]));
						}
						addShapeCollider(rootPath, shapeType, scale, originTransform, mass, acceleration, image, description);
					}
					catch(ArrayIndexOutOfBoundsException a)
					{
						Logging.log.log(Level.WARNING,"Bad parse for: "+ rootPath);
					}
					catch(NumberFormatException n)
					{
						Logging.log.log(Level.WARNING,"Bad parse for: "+ rootPath);
					}
				}
			}
			else
			{
				Logging.log.log(Level.WARNING,
						"NullPointerException! Could not find vertices or localscaling for: "
								+ rootPath);
			}
		}
		in.close();
	}
	
	public abstract void addShapeCollider(String name, String type,
			Vector3f localscaling, Vector3f transform, float mass, Vector3f acceleration,
			String image, String[] description);
	
	public abstract void addConvexVerticesCollider(String name,
			ObjectArrayList<Vector3f> vertices, float mass,
			Vector3f acceleration, String image, String[] description);
}