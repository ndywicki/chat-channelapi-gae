package org.ndywicki.chat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ndywicki.chat.json.Event;
import org.ndywicki.chat.json.Message;
import org.ndywicki.chat.json.User;
import org.ndywicki.chat.util.JsonUtil;
import org.ndywicki.chat.util.MD5Util;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;


@SuppressWarnings("serial")
public class ChannelChatServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(ChannelChatServlet.class.getCanonicalName());
	
	private Map<String, User> users = Collections.synchronizedMap(new HashMap<String, User>());
	private List<Message> messages = Collections.synchronizedList(new ArrayList<Message>());
    private int HISTORY_MAX = 10;

	private static ChannelService channelService = ChannelServiceFactory.getChannelService();
	
	@Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);        
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    	String clientId = request.getParameter("clientId");
    	String json = request.getParameter("message");
    	    	
    	Event data = JsonUtil.JsonStrToClass(json, Event.class);
    	logger.log(Level.INFO, "Event {0}", data.getEvent());
    	
    	User me;
    	switch(data.getEvent()) {
    	case "login" :
    		me = (User) data;    		
    		me.setId(clientId);
    		me.setAvatar("https://gravatar.com/avatar/" + MD5Util.md5Hex(me.getMail()) + "?s=50");
    		//broadcast other users and messages to me
    		this.broadcast(me.getId());
    		//add me to users list
    		users.put(me.getId(), me);    		
    		//emit 'logged' event for me
    		Event emit = new Event();
    		emit.setEvent("logged");    		
    		channelService.sendMessage(new ChannelMessage(me.getId(), JsonUtil.ObjectToJson(emit)));
    		
    		//emit me with 'newusr' event for all
    		me.setEvent("newusr");
    		User user;
    		for (Map.Entry<String, User> entry : users.entrySet()) {
    			user = entry.getValue();
    			channelService.sendMessage(new ChannelMessage(user.getId(), JsonUtil.ObjectToJson(user)));
    		}
    		break;    	

    	case "newmsg" :
    		Message message = (Message) data;
    		me = users.get(clientId);
    		message.setUser(me);	    		
    		Calendar calendar = Calendar.getInstance();
    		calendar.setTime(new Date());
    		message.setH(calendar.get(Calendar.HOUR_OF_DAY));
    		message.setM(calendar.get(Calendar.MINUTE));	    		
    		message.setEvent("newmsg");
    		messages.add(message);
    		if (messages.size() > HISTORY_MAX) {
    			messages.remove(0);
    		}
    		// Emit message for all
    		for (Map.Entry<String, User> entry : users.entrySet()) {
    			channelService.sendMessage(new ChannelMessage(entry.getKey(), JsonUtil.ObjectToJson(message)));
    		}
    		break;
    	}

    }
    
    private void broadcast(String clientId) {
    	User user;
        for (Map.Entry<String, User> entry : users.entrySet()) {
        	user = entry.getValue();
        	user.setEvent("newusr");        	
        	channelService.sendMessage(new ChannelMessage(clientId, JsonUtil.ObjectToJson(user)));
        }
        for (Message message : messages) {
        	channelService.sendMessage(new ChannelMessage(clientId, JsonUtil.ObjectToJson(message)));
        }
    }
}
