package poyashimitter.main;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import poyashimitter.AbstractTimelineScrollPane;
import poyashimitter.StatusPanel;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;

public class HomeTimelinePane extends AbstractTimelineScrollPane {
	Twitter twitter;
	
	boolean userStreamEnabled=true;
	
	TwitterStream twitterStream;
	Paging paging;
	
	boolean streaming=false;//userStream実行中true
	
	
	JPanel tweetSendPanel;
	public HomeTimelinePane(final Twitter twitter){
		super();
		this.twitter=twitter;
		
		
		paging=new Paging();
		paging.setCount(40);
	}
	
	public void setCount(int count){
		paging.setCount(count);
	}
	
	public void setUserStreamEnabled(boolean b){
		userStreamEnabled=b;
	}
	public void setStatuses(){
		List<Status> statuses=null;
		try {
			statuses=twitter.getHomeTimeline(paging);
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		if(statuses!=null){
			for(int i=0;i<statuses.size();i++){
				try {
					final Status s=statuses.get(i);
					SwingUtilities.invokeAndWait(new Runnable(){
						@Override
						public void run() {
							addStatusPanel(new StatusPanel(s,twitter));
						}
					});
					
					if(i<=10)
						Thread.sleep(100);
					
					SwingUtilities.invokeAndWait(new Runnable(){
						@Override
						public void run() {
							getVerticalScrollBar().setValue(getVerticalScrollBar().getMinimum());
							//getPanel().revalidate();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
			/*
			for(Status s:statuses){
				addStatusPanel(new StatusPanel(s,twitter));
				
			}
			*/
		}
		//getVerticalScrollBar().setValue(getVerticalScrollBar().getMinimum());
	}
	
	public StatusListener getStatusListener(){
		return new StatusAdapter(){
			@Override
			public void onStatus(Status status){
				addStatusPanel(new StatusPanel(status,twitter),0);
			}
		};
	}
	
}
