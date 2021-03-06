package org.ndywicki.chat;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

/**
 * This servlet creates the channel with the server and gets the token.
 * 
 * @author
 */
@SuppressWarnings("serial")
public class TokenServlet extends HttpServlet {

  private static final Logger logger = Logger.getLogger(TokenServlet.class.getCanonicalName());

  private static ChannelService channelService = ChannelServiceFactory.getChannelService();

	/**
	 * Get the token for connecting & listening on the channel
	 */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
	  throws ServletException, IOException {
    String clientId = request.getParameter("clientId");
    if (clientId != null && !"".equals(clientId)) {
      String token = createChannel(clientId);
      writeIntoChannel(response,token);
    }
  }

	/**
	 * Creates the Channel token for the user 
	 * @param clientId The User whom the token is created for  
	 * @return The token string is returned
	 */
  public String createChannel(String clientId){
    try{
      logger.log(Level.INFO, "Creating a channel for {0}",clientId);
      return channelService.createChannel(clientId);
    } catch(ChannelFailureException channelFailureException){
      logger.log(Level.WARNING, "Error creating the channel");
      return null;
    } catch(Exception otherException){
      logger.log(Level.WARNING, "Unknown exception while creating channel");
      return null;
    }
  }

	/**
	 * Writes the token in the response text 
	 * 
	 * @param response The response object to write the response text 
	 * @param token The token which needs to be written in the response
	 * @throws IOException
	 */
  public void writeIntoChannel(HttpServletResponse response, String token){
    try{
      logger.log(Level.INFO, "Writing the token {0} to the output",token);
      response.getWriter().print(token);
	} catch(IOException ioException){
      logger.log(Level.WARNING, "Exception while writing output ");
    } catch(Exception exception){
      logger.log(Level.WARNING, "Unknow exception while writing output ");
    }
  }
}