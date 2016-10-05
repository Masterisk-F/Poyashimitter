package poyashimitter.main;
import java.util.List;

import poyashimitter.AbstractTimelineScrollPane;
import poyashimitter.StatusPanel;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.UserMentionEntity;

public class MentionTimelinePane extends AbstractTimelineScrollPane {
	Twitter twitter;
	TwitterStream twitterStream;
	Paging paging;
	
	
	boolean userStreamEnabled=true;
	boolean streaming=false;//userStream実行中true
	
	public MentionTimelinePane(Twitter twitter){
		this(twitter,40);
	}
	
	public MentionTimelinePane(final Twitter twitter,int count){
		super();
		this.twitter=twitter;
		paging=new Paging();
		paging.setCount(count);
		
		
	}
	public void setUserStreamEnabled(boolean b){
		userStreamEnabled=b;
	}
	public void setCount(int n){
		paging.setCount(n);
	}
	public void setStatuses(){
		List<Status> mentions=null;
		try {
			mentions=twitter.getMentionsTimeline(paging);
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		if(mentions!=null){
			for(Status status:mentions){
				addStatusPanel(new StatusPanel(status,twitter));
			}
		}
	}
	
	
	public StatusListener getStatusListener(){
		return new StatusAdapter(){
			@Override
			public void onStatus(Status status){
				//リプライ先が自分かどうか確認
				try {
					if(containUser(status.getUserMentionEntities(),twitter.getId())){
						addStatusPanel(new StatusPanel(status,twitter),0);
					}
				} catch (IllegalStateException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} catch (TwitterException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				
			}
			
			boolean containUser(UserMentionEntity[] entity,long id){
				if(entity==null)	return false;
				for(UserMentionEntity e:entity){
					if(e.getId()==id)	return true;
				}
				return false;
			}
		};
	}
	
	/*
	public void startUserStream(){
		if(!streaming && userStreamEnabled){
			twitterStream.user();
			streaming=true;
		}
		
	}
	
	public void stopUserStream(){
		if(streaming){
			twitterStream.shutdown();//???
			streaming=false;
		}
	}
	*/
}
