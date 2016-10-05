package poyashimitter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class StringFormatter {
	
	public static String getDisplayHTMLString(Status status){
		String text=status.getText();
		text="<html><body>"
				+text
				.replaceAll("<","&lt;")
				.replaceAll(">","&gt;")
				.replaceAll("\\r\\n|[\\n\\r\\u2028\\u2029\\u0085]","<br>")
			+"</body></html>";
		
		ArrayList<URLEntity> list=new ArrayList<URLEntity>(
				status.getURLEntities().length+status.getExtendedMediaEntities().length);
		list.addAll(Arrays.asList(status.getURLEntities()));
		list.addAll(Arrays.asList(status.getExtendedMediaEntities()));
		for(URLEntity url : list){
			String dispUrl=url.getDisplayURL();
			/*
			dispUrl=dispUrl.substring(0,Math.min(dispUrl.length(),22));
			if(dispUrl.length()==22)
				dispUrl+="..";
			*/
			text=text.replaceAll(url.getURL(),
					"<a href=\""+url.getExpandedURL()+"\">"+dispUrl+"</a>");//どのURLを表示させるかは要検討
			
			//System.out.println(url.getDisplayURL()+"\n"+url.getURL());
		}
		
		for(UserMentionEntity user:status.getUserMentionEntities()){
			text=text.replaceAll("@"+user.getScreenName(),
					"<a href=\"poyash://user/"+user.getId()+"."+user.getScreenName()+"\">"
					+"@"+user.getScreenName()+"</a>");
			//System.out.println(user.getName()+"\n"+user.getScreenName());
			
		}
		
		for(HashtagEntity hash:status.getHashtagEntities()){
			text=text.replaceAll("(^|\\s)"+"#"+hash.getText()+"(\\s|$)",
					"\n<a href=\"poyash://hashtag/"+hash.getText()+"\">"
					+"#"+hash.getText()+"</a>\n");
			
			//System.out.println(text+"\n"+"#"+hash.getText()+"\n");
		}
		
		
		return text;
	}
	/*
	public static String getDisplayHTMLString(String text,URLEntity[] list){
		return getDisplayHTMLString(text,Arrays.asList(list));
	}
	public static String getDisplayHTMLString(String text,List<URLEntity> list){
		if(text==null)	return null;
		String str="<html><body>"
				+text
					.replaceAll("<","&lt;")
					.replaceAll(">","&gt;")
					.replaceAll("\\r\\n|[\\n\\r\\u2028\\u2029\\u0085]","<br>")
				+" </body></html>";
		
		for(URLEntity url : list){
			String dispUrl=url.getDisplayURL();
			dispUrl=dispUrl.substring(0,Math.min(dispUrl.length(),22));
			if(dispUrl.length()==22)
				dispUrl+="..";
			
			str=str.replaceAll(url.getURL(),
					" <a href=\""+url.getExpandedURL()+"\">"+dispUrl+"</a>");//どのURLを表示させるかは要検討
			
		}
		
		return str;
	}
	*/
	/*
	public static String getDisplayHTMLString2(String text,URLEntity[] url,MediaEntity[] media){
		return getDisplayHTMLString2(text,Arrays.asList(url),Arrays.asList(media));
	}
	public static String getDisplayHTMLString2(String text,List<URLEntity> url,List<MediaEntity> media){
		if(text==null)	return null;
		String txtStr=text
					.replaceAll("<","&lt;")
					.replaceAll(">","&gt;")
					.replaceAll("\\r\\n|[\\n\\r\\u2028\\u2029\\u0085]","<br>");
		for(URLEntity en : url){
			String dispUrl=en.getDisplayURL();
			dispUrl=dispUrl.substring(0,Math.min(dispUrl.length(),22));
			if(dispUrl.length()==22)
				dispUrl+="..";
			
			txtStr=txtStr.replaceAll(en.getURL(),
					" <a href=\""+en.getExpandedURL()+"\">"+dispUrl+"</a> ");//どのURLを表示させるかは要検討
			
		}
		
		if(media.size()!=0){
			String[] mediaStr=new String[media.size()];
			int mediaSize=Settings.getTimelineOption(Settings.timeline.mediaSize);
			for(int i=0;i<media.size();i++){
				File tmp=null;
				try {
					tmp=File.createTempFile("poyashimitter",".png");
					tmp.deleteOnExit();
					
					URL imgURL=new URL(media.get(i).getMediaURLHttps()+":small");
					Image img=ImageIO.read(imgURL);
					if(img.getWidth(null)>img.getHeight(null)){
						img=img.getScaledInstance(mediaSize,-1, Image.SCALE_SMOOTH);
					}else{
						img=img.getScaledInstance(-1,mediaSize, Image.SCALE_SMOOTH);
					}
					BufferedImage saveImg=new BufferedImage(
							img.getWidth(null),img.getHeight(null),BufferedImage.TYPE_INT_ARGB_PRE);
					Graphics g=saveImg.getGraphics();
					//g.setColor(Color.black);
					//g.fillRect(0,0, 100, 100);
					g.drawImage(img, 0, 0, null);
					
					ImageIO.write(saveImg, "png",tmp);
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				System.out.println(tmp.getAbsolutePath());
				mediaStr[i]=
						"<a href=\""+"poyash://media/"+i+"."+media.get(i).getId()+"\">"
						//"<a href=\""+"http://poyashimitter.com/"+i+"\">"
						//+"<img src=\""+media.get(i).getMediaURLHttps()+":small"+"\" width=100>"
						+"<img src=\""+"file:///"+tmp.getAbsolutePath()+"\">"
						+"</a>";
				
			}
			String mediaString="";
			if(Settings.getMediaPosition().equals("right")){
				mediaString+="<table><tr>";
				for(int i=0;i<mediaStr.length;i++){
					if(i==2)
						mediaString+="</tr><tr>";
					
					mediaString+="<td>"+mediaStr[i]+"</td>";
				}
				mediaString+="</tr></table>";
			}else{
				
			}
			txtStr="<html><body>"+"<table width=100%><td>"+txtStr+"</td><td width=100>"+mediaString+"</td></table>"+"</body></html>";
		}else{
			txtStr="<html><body>"+txtStr+"</body></html>";
		}
		
		return txtStr;
	}
	*/
	public static String getTimeString(Date date){
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		
		Calendar curr=Calendar.getInstance();	//現在時刻
		
		long diff=curr.getTimeInMillis()-cal.getTimeInMillis();
		
		String str="";
		if(diff<1000*60*60*24){//24時間以内なら
			int hour=cal.get(Calendar.HOUR_OF_DAY);
			int min=cal.get(Calendar.MINUTE);
			int sec=cal.get(Calendar.SECOND);
			
			if(hour<10)	str+="0";
			str+=hour+":";
			if(min<10)	str+="0";
			str+=min+":";
			if(sec<10)	str+="0";
			str+=sec;
		}else if(diff<1000*60*60*24*365){//1年以内なら
			int mon=cal.get(Calendar.MONTH)+1;
			int day=cal.get(Calendar.DATE);
			int hour=cal.get(Calendar.HOUR_OF_DAY);
			int min=cal.get(Calendar.MINUTE);
			int sec=cal.get(Calendar.SECOND);
			
			str+=mon+"/"+day+" ";
			
			if(hour<10)	str+="0";
			str+=hour+":";
			if(min<10)	str+="0";
			str+=min+":";
			if(sec<10)	str+="0";
			str+=sec;
		}else{
			long year=cal.get(Calendar.YEAR);
			int mon=cal.get(Calendar.MONTH)+1;
			int day=cal.get(Calendar.DATE);
			str=year+"/"+mon+"/"+day;
		}
		return str;
		
	}
}
