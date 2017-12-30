package org.eml.MMAX2.annotation.scheme;

public class UIMATypeMapping 
{
	private String descriptorPath="";
	private String className="";	
	
	public UIMATypeMapping (String _descriptorPath, String _className)
	{
		descriptorPath = _descriptorPath;
		className = _className;
	}
	
	public UIMATypeMapping (String _completePath)
	{		
		int hashPos = _completePath.indexOf("#");
		if (hashPos != -1)
		{
			descriptorPath = _completePath.substring(0,hashPos);
			className = _completePath.substring(hashPos+1);
		}
		else
		{
			descriptorPath = _completePath;
		}
	}
		
	public String getDescriptorPath()
	{
		return descriptorPath; 
	}

	public String getClassName()
	{
		return className;
	}
	
	public String toString()
	{
		return "UIMA descriptor path: "+descriptorPath + "\nUIMA class name: "+className;
	}
}
