package poyashimitter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public abstract class AbstractTimelineScrollPane extends JScrollPane {
	JPanel timelinePanel;
	
	Set<Runnable> runnableSet=Collections.synchronizedSet(new HashSet<Runnable>());
	JScrollBar scrollBar=this.getVerticalScrollBar();
	boolean smoothScrollEnabled=true;
	
	protected AbstractTimelineScrollPane(){
		super();
		
		timelinePanel=new JPanel();
		//timelinePanel.setOpaque(false);
		//timelinePanel.setBackground(Color.white);
		timelinePanel.setLayout(new BoxLayout(timelinePanel,BoxLayout.Y_AXIS));
		
		getVerticalScrollBar().setUnitIncrement(20);//スクロール量調整
		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		setViewportView(timelinePanel);
	}
	
	public void addStatusPanel(final StatusPanel statusPanel){
		timelinePanel.add(statusPanel);
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				statusPanel.revalidate();
			}
		}).start();
		
	}
	
	public void addStatusPanel(final StatusPanel statusPanel,int index){
		timelinePanel.add(statusPanel,index);
		//statusPanel.revalidate();
		new Thread(new Runnable(){
			@Override
			public void run() {
				statusPanel.revalidate();
			}
		}).start();
		
		if(smoothScrollEnabled
				&& index==0
				&& (scrollBar.getValue()==scrollBar.getMinimum() || runnableSet.size()!=0)){
			synchronized(scrollBar){
				scrollBar.setValue(scrollBar.getValue()+statusPanel.getPreferredSize().height);
			}
			smoothScroll(statusPanel.getPreferredSize().height);
		}else if(index==0){
			synchronized(scrollBar){
				scrollBar.setValue(scrollBar.getValue()+statusPanel.getPreferredSize().height);
			}
		}
	}
	
	public JPanel getPanel(){
		return timelinePanel;
	}
	public void setPanel(JPanel panel){
		this.setViewport(null);
		timelinePanel=panel;
		
		this.setViewportView(panel);
		
	}
	
	void setSmoothScrollEnabled(boolean b){
		smoothScrollEnabled=b;
	}
	
	void smoothScroll(final int n){
		new Thread(new Runnable(){
			@Override
			public void run(){
				Runnable runnable=new Runnable(){
					@Override
					public void run() {
						synchronized(scrollBar){
							scrollBar.setValue(scrollBar.getValue()-1);
						}
					}
				};
				
				runnableSet.add(this);
				for(int i=0;i<n+1;i++){
					try {
						SwingUtilities.invokeLater(runnable);
						Thread.sleep(5);
					} catch (Exception e) {
						//e.printStackTrace();
					}
				}
				//不具合解消のため
				if(scrollBar.getValue()<=scrollBar.getMinimum()+3){
					try {
						SwingUtilities.invokeAndWait(new Runnable(){
							@Override
							public void run() {
								synchronized(scrollBar){
									scrollBar.setValue(scrollBar.getMinimum());
								}
							}
						});
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
				runnableSet.remove(this);
			}
		}).start();
	}
	
}
