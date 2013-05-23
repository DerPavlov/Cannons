package at.pavlov.cannons.container;

public class DesignFileName
{
	
	private String ymlString;
	private String schematicString;
	
	public DesignFileName(String _yml, String _schematic)
	{
		this.ymlString = _yml;
		this.schematicString = _schematic;
	}
	
	public String getYmlString()
	{
		return ymlString;
	}
    public void setYmlString(String ymlString)
    {
        this.ymlString = ymlString;
    }
	public String getSchematicString()
	{
		return schematicString;
	}
	public void setSchematicString(String schematicString)
	{
		this.schematicString = schematicString;
	}

}
