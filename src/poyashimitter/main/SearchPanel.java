package poyashimitter.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import poyashimitter.AbstractTimelineScrollPane;
import poyashimitter.StatusPanel;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class SearchPanel extends JPanel {
	Twitter twitter;
	
	JPanel wordInputPanel;
	
	AbstractTimelineScrollPane resultPane;
	
	boolean userStreamEnabled=false;
	Runnable runningInstance;//使う？
	
	JComponent parent;
	
	public SearchPanel(Twitter twitter,JComponent parent){
		super();
		
		this.twitter=twitter;
		this.parent=parent;
		this.setLayout(new BorderLayout());
		
		wordInputPanel=getWordInputPanel();
		resultPane=new SearchPane();
		
		add(wordInputPanel,BorderLayout.NORTH);
		add(resultPane,BorderLayout.CENTER);
	}
	
	public void setUserStreamEnabled(boolean b){
		userStreamEnabled=b;
	}
	
	void search(String string){
		QueryResult result=null;
		try {
			Query query=new Query(string);
			
			result=twitter.search(query);
			
		} catch (TwitterException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		if(result!=null){
			final List<Status> resultList=result.getTweets();
			new Thread(new Runnable(){
				@Override
				public void run() {
					remove(resultPane);
					resultPane=new SearchPane();
					add(resultPane);
					resultPane.revalidate();
					revalidate();
					parent.revalidate();
					for(Status status:resultList){
						StatusPanel sp=new StatusPanel(status,twitter);
						resultPane.addStatusPanel(sp);
					}
				}
			}).start();
		}
		
		
		if(userStreamEnabled){
			//streaming処理
		}
	}
	
	JPanel getWordInputPanel(){
		JPanel returnPanel=new JPanel();
		returnPanel.setLayout(new BorderLayout());
		
		final JTextArea inputArea=new JTextArea();
		//inputArea.setLineWrap(true);
		inputArea.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
		returnPanel.add(inputArea,BorderLayout.CENTER);
		inputArea.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e){
				if(twitter!=null
						&& e.getKeyCode()==KeyEvent.VK_ENTER){
					search(inputArea.getText());
				}
			}
		});
		//inputAreaで、Enterで改行禁止にする
		InputMap ims=inputArea.getInputMap(JTextArea.WHEN_FOCUSED);
		KeyStroke enter=KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0);
		ims.put(enter,"none");
		
		JButton searchButton=new JButton("検索");
		searchButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				search(inputArea.getText());
			}
		});
		returnPanel.add(searchButton,BorderLayout.EAST);
		
		return returnPanel;
	}
}

class SearchPane extends AbstractTimelineScrollPane{
	
	public SearchPane(){
		super();
		this.setPreferredSize(new Dimension(Short.MAX_VALUE,Short.MAX_VALUE));
	}
}
