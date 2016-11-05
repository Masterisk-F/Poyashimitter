package poyashimitter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import twitter4j.auth.AccessToken;


/*
oauth
	accessToken
	accessTokenSecret
	
position	//フォーマットは"x,y"
	mainWindow
	userWindow
	mediaWindow
	settingWindow
	//ウィンドウサイズはそれぞれ.size.~ (ex. position.size.mainWindow)
	
color	//これらを格納したColorSettingsクラスを作ったほうが良い？
	
	foreground	//font color
		main
		frame
		RT
		myself
		mention
		quoted
		tweetTextArea
	
	backGround
		main
		//frame
		RT
		myself
		mention
		quoted
		tweetTextArea
	frameBackground
		main
		RT
		myself
		mention
		quoted
		//tweetTextArea
	
	button
	
	
timelineSize(home,reply両方)
	loadStatusNumber//はじめに読み込むstatusの数
	
	mediaSize	//メディアのサムネイルサイズ
	iconSize	//ユーザーアイコンのサイズ
	RTIconSize	//RT"した"人のアイコンサイズ
	
	userStreamEnabled
	mediaPosition	//メディアのStatusPanel内での位置。"right"or"bottom"
	
font
	main	(tweet本文、userWindowのbioなど)
	tweetTextArea
	userName
	RTUserName
	
	//フォントサイズはそれぞれ.size (ex. font.tweet.size)
	//フォーマットは"(フォント名),(boldなどstyle定数),(サイズ)"
tab
	tabNumber//タブの数
	tab_1
	tab_2
	...
		tab_nの値は
		"home"
		"reply"
		"search_(word)",(検索)
		"user_(userID)",(特定ユーザーのツイート)
		"list_(ID)",(リスト)
		"userOfList_(id)"(リストに登録されたユーザー)
	
	
	
 */

public class Settings {
	
	protected static final String CONSUMER_KEY="52UiiTOApaJOpEMS5dBPF3lU6";
	protected static final String CONSUMER_SECRET="PXei5Mz1b0RwcLbUhJxKbZ63CbmT2dYaRP1K7AgyyqrWZDmhMn";
	
	static Properties properties;
	
	public class position{
		private static final String position="position";
		
		public static final String mainWindow=position+".mainWindow";
		public static final String userWindow=".userWindow";
		public static final String mediaWindow=".mediaWindow";
		public static final String settingWindow=".settingWindow";
		public class size{
			private static final String size=position+".size";
			
			public static final String mainWindow=size+".mainWindow";
			public static final String userWindow=size+".userWindow";
			public static final String mediaWindow=size+".mediaWindow";
			public static final String settingWindow=size+".settingWindow";
		}
	}
	public class color{
		private static final String color="color";
		
		public class foreground{
			private static final String foreground=color+".foreground";
			
			public static final String main=foreground+".main";
			public static final String frame=foreground+".frame";
			public static final String RT=foreground+".RT";
			public static final String myself=foreground+".myself";
			public static final String mention=foreground+".mention";
			public static final String quoted=foreground+".quoted";
			public static final String tweetTextArea=foreground+".tweetTextArea";
		}
		public class background{
			private static final String background=color+".background";
			
			public static final String main=background+".main";
			//public static final String frame=background+".frame";
			public static final String RT=background+".RT";
			public static final String myself=background+".myself";
			public static final String mention=background+".mention";
			public static final String quoted=background+".quoted";
			public static final String tweetTextArea=background+".tweetTextArea";
		}
		public class frameBackground{
			private static final String frameBackground=color+".frameBackground";
			
			public static final String main=frameBackground+".main";
			//public static final String frame=frameBackground+".frame";
			public static final String RT=frameBackground+".RT";
			public static final String myself=frameBackground+".myself";
			public static final String mention=frameBackground+".mention";
			public static final String quoted=frameBackground+".quoted";
			public static final String tweetTextArea=frameBackground+".tweetTextArea";
		}
		public static final String button=color+".button";
		
	}
	public class timeline{
		private static final String timeline="timeline";
		
		public static final String loadStatusNumber=timeline+".loadStatusNumber";//はじめに読み込むstatusの数
		public static final String mediaSize=timeline+".mediaSize";	//メディアのサムネイルサイズ
		public static final String iconSize=timeline+".iconSize";	//ユーザーアイコンのサイズ
		public static final String RTIconSize=timeline+".RTIconSize";	//RT"した"人のアイコンサイズ
		
		//専用のメソッドで取り出す（戻り値の型が違うため）
		private static final String mediaPosition=timeline+".mediaPosition";	//メディアのStatusPanel内での位置。"right"or"bottom"
		private static final String userStreamEnabled=timeline+".userStreamEnabled";//true or false
		
	}
	public class font{
		private static final String font="font";
		
		public static final String main=font+".main";	//(tweet本文、userWindowのbioなど)
		public static final String tweetTextArea=font+".tweetTextArea";
		public static final String userName=font+".userName";
		public static final String RTUserName=font+".RTUserName";
		
	}
	public class tab{
		private static final String tab="tab";
		
		public static final String tabNumber=tab+".tabNumber";//タブの数
		
	}
	
	
	public static class TabDescriptor{
		
		static public final String home="home";
		static public final String reply="reply";
		static public final String search="search";//(検索)
		static public final String user="user";//(特定ユーザーのツイート)
		static public final String list="list";//(リスト)
		static public final String userOfList="userOfList";
		
		
		String type;
		String string;//word,id,listid,listid
		
		TabDescriptor(String str){
			String[] s=str.split("_");
			type=s[0];
			if(s.length>=2)
				string=s[1];
		}
		public TabDescriptor(String type,String string){
			this.type=type;
			this.string=string;
		}
		
		public String getType(){
			return type;
		}
		public String getWord(){
			return string;
		}
	}
	
	static{
		properties=new Properties();
		
		InputStream in=null;
		try {
			in=new BufferedInputStream(new FileInputStream("mytwitter4j.xml"));
			properties.loadFromXML(in);
			
			
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (InvalidPropertiesFormatException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
			
			
		}
		
	}
	public static boolean save(){//保存できればtrue
		
		//ソートする？？
		Map<Object,Object>map=new TreeMap<Object,Object>(properties);
		properties=new Properties();
		properties.putAll(map);
		
		OutputStream out=null;
		try {
			out=new BufferedOutputStream(new FileOutputStream("mytwitter4j.xml"));
			properties.storeToXML(out,"");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}finally{
			if(out!=null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return true;
	}
	
	public static AccessToken getAccessToken(){
		String token=properties.getProperty("oauth.accessToken");
		String tokenSecret=properties.getProperty("oauth.accessTokenSecret");
		
		AccessToken at=null;
		if(token!=null && tokenSecret!=null){
			at=new AccessToken(token,tokenSecret);
		}
		
		return at;
	}
	public static void setAccessToken(AccessToken token){
		properties.setProperty("oauth.accessToken", token.getToken());
		properties.setProperty("oauth.accessTokenSecret",token.getTokenSecret());
	}
	
	
	/*static Color getBackgroundColor(){
		String s=properties.getProperty("color.background");
		if(s==null)
			return Color.white;
		else
			return Color.decode(s);//sは#ffffffの形式
	}
	static void setBackgroundColor(Color color){
		String s="#"+Integer.toHexString(color.getRGB());
		
		properties.setProperty("color.background",s);
		
	}*/
	
	public static Color getColor(String key){
		if(!key.startsWith(color.color))
			return null;
		
		String s=properties.getProperty(key);
		if(s==null){
			Color c;
			if(key.startsWith(color.foreground.foreground))
				c=Color.black;
			else if(key.startsWith(color.frameBackground.frameBackground))
				c=Color.gray;
			else
				c=Color.white;
			setColor(key,c);
			System.out.println(c);
			return c;
		}
		try {
			return Color.decode(s);
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			/*
			Color c;
			if(key.startsWith(color.foreground.foreground)){
				c=Color.black;
			}else{
				c=Color.white;
			}
			setColor(key,c);
			return c;
			*/
		}
		return null;
	}
	public static boolean setColor(String key,Color col){
		if(!key.startsWith(color.color))
			return false;
		
		//properties.setProperty(key,String.format("%06d",Integer.toHexString(col.getRGB())) );
		properties.setProperty(key,"#"+Integer.toHexString(col.getRGB()&0x00ffffff) );
		
		return true;
		
	}
	
	public static Point getPosition(String key){
		if(!key.startsWith(position.position))
			return null;
		
		String s=properties.getProperty(key);
		if(s==null){
			Point p=new Point(50,50);
			setPosition(key,p);
			return p;
		}
		String[] pos=s.split(",");
		return new Point(Integer.decode(pos[0]),Integer.decode(pos[1]));
		
	}
	public static boolean setPosition(String key,Point point){
		if(!key.startsWith(position.position))
			return false;
		
		properties.setProperty(key,point.x+","+point.y);
		
		return true;
	}
	public static Dimension getSize(String key){
		if(!key.startsWith(position.size.size))
			return null;
		
		String s=properties.getProperty(key);
		
		if(s==null){
			Dimension d;
			
			if(key.equals(position.size.mainWindow)){
				d=new Dimension(600,750);
			}else if(key.equals(position.size.mediaWindow)
					|| key.equals(position.size.userWindow)){
				d=new Dimension(500,600);
			}else{
				d=new Dimension(500,500);
			}
			setSize(key,d);
			return d;
		}
		
		String[] pos=s.split(",");
		return new Dimension(Integer.decode(pos[0]),Integer.decode(pos[1]));
		
	}
	public static boolean setSize(String key,Dimension dimension){
		if(!key.startsWith(position.size.size))
			return false;
		
		properties.setProperty(key,dimension.width+","+dimension.height);
		
		return true;
	}
	
	public static int getTimelineOption(String key){
		if(!key.startsWith(timeline.timeline))
			return -1;
		
		String s=properties.getProperty(key);
		if(s==null){
			if(key.equals(timeline.iconSize)){
				s="40";
			}else if(key.equals(timeline.loadStatusNumber)){
				s="40";
			}else if(key.equals(timeline.mediaSize)){
				s="100";
			}else if(key.equals(timeline.RTIconSize)){
				s="20";
			}
			setTimelineOption(key,Integer.decode(s));
		}
		return Integer.decode(s);
		
	}
	public static boolean setTimelineOption(String key,int n){
		if(!key.startsWith(timeline.timeline))
			return false;
		
		properties.setProperty(key,String.valueOf(n));
		
		return true;
	}
	public static boolean getUserStreamEnabled(){
		String s=properties.getProperty(timeline.userStreamEnabled);
		if(s==null){
			setUserStreamEnabled(true);
			return true;
		}
		return s.equals("true");
	}
	public static boolean setUserStreamEnabled(boolean b){
		properties.setProperty(timeline.userStreamEnabled,String.valueOf(b));
		
		return true;
	}
	public static String getMediaPosition(){
		String s=properties.getProperty(timeline.mediaPosition);
		if(s==null){
			setMediaPosition("right");
			return "right";
		}
		return s;
	}
	public static boolean setMediaPosition(String pos){
		if(!pos.equals("right") && !pos.equals("bottom"))
			return false;
		
		properties.setProperty(timeline.mediaPosition,pos);
		return true;
	}
	public static Font getFont(String key){
		if(!key.startsWith(font.font))
			return null;
		
		String s=properties.getProperty(key);
		if(s==null){
			Font f=new Font(Font.SANS_SERIF,Font.PLAIN,12);
			setFont(key,f);
			return f;
		}
		
		String[] list=s.split(",");
		return new Font(list[0],Integer.decode(list[1]),Integer.decode(list[2]));
	}
	public static boolean setFont(String key,Font f){
		if(!key.startsWith(font.font))
			return false;
		
		properties.setProperty(key,f.getFontName()+","+f.getStyle()+","+f.getSize());
		return true;
	}
	public static int getTabNumber(){
		String s=properties.getProperty(tab.tabNumber);
		if(s==null){
			TabDescriptor[] array=new TabDescriptor[3];
			array[0]=new TabDescriptor(TabDescriptor.home,null);
			array[1]=new TabDescriptor(TabDescriptor.reply,null);
			array[2]=new TabDescriptor(TabDescriptor.search,"");
			setTabs(array);
			return 3;
		}
		return Integer.decode(s);
	}
	
	public static TabDescriptor getTab(int num){
		String s=properties.getProperty(tab.tab+num);
		if(s==null)
			return null;
		return new TabDescriptor(s);
	}
	public static boolean setTabs(TabDescriptor[] desc){
		if(desc==null)
			return false;
		
		properties.setProperty(tab.tabNumber,String.valueOf(desc.length));
		for(int i=0;i<desc.length;i++){
			properties.setProperty(tab.tab+i,desc[i].getType()+desc[i].getWord());
		}
		int i=desc.length;
		while(properties.containsKey(tab.tab+i)){
			properties.remove(tab.tab+i);
			i++;
		}
		
		return true;
		
	}
	
}
