package poyashimitter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class UserTimelinePane extends AbstractTimelineScrollPane {
	Twitter twitter;
	User user;
	Paging paging;
	
	public UserTimelinePane(User user,Twitter twitter){
		super();
		this.user=user;
		this.twitter=twitter;
		paging=new Paging();
		setStatuses();
	}
	public UserTimelinePane(User user,Twitter twitter,int count){
		super();
		this.user=user;
		this.twitter=twitter;
		paging=new Paging();
		paging.setCount(count);
		setStatuses();
	}
	public UserTimelinePane(User user,Twitter twitter,Paging paging){
		super();
		this.user=user;
		this.twitter=twitter;
		this.paging=paging;
		setStatuses();
	}
	
	public void setStatuses(){
		
		new SwingWorker<List<Status>,Status>(){
			@Override
			protected List<Status> doInBackground() throws Exception {
				List<Status> list=null;
				try {
					list=twitter.getUserTimeline(user.getId(),paging);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				for(Status s:list){
					publish(s);
					Thread.sleep(100);
				}
				
				
				return list;
			}
			/*
			@Override
			protected void process(List<Status> chunks){
				for(Status s:chunks){
					addStatusPanel(new StatusPanel(s,twitter));
					getVerticalScrollBar().setValue(getVerticalScrollBar().getMinimum());
				}
			}
			*/
			@Override
			protected void done(){
				try {
					List<Status> list=get();
					for(Status s:list){
						getPanel().add(new StatusPanel(s,twitter));
						//addStatusPanel(new StatusPanel(s,twitter));
					}
					//getPanel().getComponent(0).revalidate();
					revalidate();
					
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				
				revalidate();
				getVerticalScrollBar().setValue(getVerticalScrollBar().getMinimum());
			}
		}.execute();
		
		
		/*
		new Thread(new Runnable(){
			@Override
			public void run() {
				List<Status> list=null;
				try {
					list=twitter.getUserTimeline(user.getId(),paging);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				if(list!=null){
					for(Status s:list){
						addStatusPanel(new StatusPanel(s,twitter));
					}
				}
			}
		}).start();
		*/
	}
	
	
}
