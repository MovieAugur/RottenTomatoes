import java.awt.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class starcast 
{
	public static Map<String, ArrayList<String>> starmap = new HashMap<String, ArrayList<String>>();
	public static Map<String, Float> map = new HashMap<String, Float>();
	public static Map<String,Float> starpower = new HashMap<String,Float>();
	
	public static String removeCharAt(String s, int pos)
	 {
	      return s.substring(0, pos) + s.substring(pos + 1);
	   }
	
	 public static void load_worth(String worth_path) throws Exception
	 {
		 String path = worth_path;
	    	String CurrentLine;
	    	String filename;
	    	String actor_name;
	    	float worth;
	    	String[] parts = {"1","2"};
	    	
	    	FileReader fr = new FileReader(path);
	    	BufferedReader br = new BufferedReader(fr);
	    	while ((CurrentLine = br.readLine()) != null) 
	    	{
	    		parts = CurrentLine.split("\t");
	    		actor_name = parts[0];
	    		worth = Float.parseFloat(removeCharAt(parts[1], 0));
	    		map.put(actor_name, worth);
	    		//System.out.println(actor_name +" :: "+ worth);
	    	}
	 }
	 
	 public static void get_cast(String path) throws Exception
	 {
		 	try {
				String CurrentLine;
				String actor_name;
				String movie_name = "";
				String[] parts = {"1","2"};
				ArrayList<String> cast = new ArrayList<String>();
				FileReader fr = new FileReader(path);
				BufferedReader br = new BufferedReader(fr);
				int count = 0;//get only top 10 actors
				while ((CurrentLine = br.readLine()) != null ) 
				{
					parts = CurrentLine.split("\t");
					if (parts.length!=2)
						continue;
					movie_name = parts[0];
					System.out.println(movie_name);
					actor_name = parts[1];
					cast.add(actor_name);
					//count++;
				}
				starmap.put(movie_name, cast);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("unable to read cast for movie" + path);
				e.printStackTrace();
			}
	 }
	 
	 public static  void read_folder(String path) throws Exception
	   {
		   File folder = new File(path);
		   File[] files = folder.listFiles();
		   //System.out.println("folder size "+files.length);	   
		   String file_path;
		   if (files==null)
			   {
			   		System.out.println("null in directory");
			   }
		   else
		   {
			   for (int i = 0;i<files.length;i++)
			   {
				   file_path = files[i].getAbsolutePath();
				   System.out.println(file_path);
				   get_cast(file_path);   
			   }
		   }
	   }

	public static void main(String[] args) throws Exception
	{
		
			read_folder(args[0]);//read cast
			load_worth(args[1]);//read star values
			String Filename = "starpower.txt";
			File inputFile = new File(Filename);
			FileWriter fileWriter;
			if (inputFile.exists())
			{
				inputFile.delete();
			}
			inputFile.createNewFile();
			fileWriter = new FileWriter(inputFile);
			BufferedWriter bw = new BufferedWriter(fileWriter);
			for (Map.Entry<String, ArrayList<String>> entry : starmap.entrySet()) 
			{
			    String key = entry.getKey();
			    ArrayList<String> value = entry.getValue();
			    float worth = 0;
			    for (int i =0;i<value.size();i++)
			    {
			    	if (map.containsKey(value.get(i)))
			    	{
			    		worth = worth + map.get(value.get(i));
			    	}
			    }
			    starpower.put(key, worth);
			    String data = key+"\t"+worth;
				bw.write(data);
				bw.newLine();
			    System.out.println(key+"\t"+worth);
			    //write data into a file
			    
			    //System.out.println(worth);
			    //System.out.println("***************************************************");	    
			}
			bw.close();
			System.out.println("starpower file generated.");

	}

}
