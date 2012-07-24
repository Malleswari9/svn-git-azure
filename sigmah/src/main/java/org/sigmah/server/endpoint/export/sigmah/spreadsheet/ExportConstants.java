package org.sigmah.server.endpoint.export.sigmah.spreadsheet;


/*
 * Constants used only in exporting classes
 * 
 *  @author sherzod
 */
public class ExportConstants {
	
	private ExportConstants(){}
	
	/*
	 * Measures
	 */
 	public final static float TITLE_ROW_HEIGHT = 18.15f;
	public final static float EMPTY_ROW_HEIGHT = 12.15f;
	public final static float HEADER_ROW_HEIGHT = 20.15f;
	
	
	/*
	 * Colors
	 */
	//Table headers  GRAY 10 %
	public final static byte[] GRAY_10_RGB={(byte) 245,(byte) 245, (byte) 245}; //RGB
	public final static String GRAY_10_HEX= "#F5F5F5"; //Hexadecimal color code
	
	//Secondary or inner table headers LIGHT ORAGANGE
	public final static byte[] LIGHTORANGE_RGB={(byte) 251, (byte) 245,(byte) 217};  
	public final static String LIGHTORANGE_HEX= "#FBF5D9";
	
	//White
	public final static String WHITE_HEX= "#FFFFFF";
 
}
