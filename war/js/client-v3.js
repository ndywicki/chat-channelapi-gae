(function($){	
	"use strict";

	var msgtpl = $('#msgtpl').html();	
	var lastmsg = false;
	$('#msgtpl').remove();	
	var clientId;
	
	function requestToken(){		
		var getTokenURI = '/gettoken?clientId=' + clientId ;
		var httpRequest = makeRequest(getTokenURI,false);
//		httpRequest.onreadystatechange = function(){
//			if (httpRequest.readyState === 4) {
				if (httpRequest.status === 200) {
					openChannel(httpRequest.responseText);
				}else {
					alert('There was a problem with the request.');
				}
//			}
//		}
	};

	function openChannel (token) {
		var channel = new goog.appengine.Channel(token);
		var socket = channel.open();
		socket.onopen = onSocketOpen;
		socket.onmessage = onSocketMessage;
		socket.onerror = onSocketError;
		socket.onclose = onSocketClose;
	};

	function onSocketError (error){
//		alert("Error is <br/>"+error.description+" <br /> and HTML code"+error.code);
	};

	function onSocketOpen() {
		// socket opened
//		alert("Socket Connection opened");
	};

	function onSocketClose() {
//		alert("Socket Connection closed");
	};

	function onSocketMessage(message) {
//		alert("onSocketMessage message:"+message.data);
		
		try {
            var json = jQuery.parseJSON(message.data);
        } catch (e) {
            console.log('This doesn\'t look like a valid JSON: ', message.data);
            return;
        }
        switch(json.event) {
        	case 'logged': logged(); break;
        	case 'newusr': newusr(json); break;
        	case 'disusr': disusr(json); break;
        	case 'newmsg': newmsg(json); break;
        }
	};	
	
	/**
     * Gestion des users
     */
    function logged(){
    	$('#login').fadeOut();
    	$('message').focus();
    };
    
    function newusr(user){
		$('#users').append('<img src="' + user.avatar + '" id="' + user.id + '">')
	};
	
	function disusr(user){
		$('#' + user.id).remove();
	};
	
	/**
	 * Nouveau de message
	 */
	function newmsg(message){
		if(lastmsg != message.user.id) {
			$('#messages').append('<div class="sep"</div>');
			lastmsg = message.user.id;
		}
		$('#messages').append('<div class=message>' + Mustache.render(msgtpl, message) + '</div>');
		$('#messages').animate({scrollTop : $('#messages').prop('scrollHeight') }, 100);
	};
	
	/**
     * Envoi du login
     */
    $('#loginform').submit(function(event){
		event.preventDefault();
		if ($('#username').val() == ''){
			alert('Vous devez entrer un pseudo !');
		} else {
			
			var username = $('#username').val();
			var mail = $('#mail').val();
			clientId = username + "-" + mail;
			requestToken();
			
			var message = jQuery.stringifyJSON({
				type		: 'user',
				event		: 'login',
				username	: $('#username').val(),
				mail		: $('#mail').val()
			});
			
			var sendMessageURI = '/chat?message=' + message + '&clientId=' + clientId ;
			var httpRequest = makeRequest(sendMessageURI, true);		
		}
		return false;
	});
    
    /**
	* Envoi de message
	*/
	$('#form').submit(function(event){
		event.preventDefault();
		var message = jQuery.stringifyJSON({
			type		: 'message',			
			event		: 'newmsg',
			message		: $('#message').val()
		});
		var sendMessageURI = '/chat?message=' + message + '&clientId=' + clientId ;
		var httpRequest = makeRequest(sendMessageURI, true);
		
		$('#message').val('');
		$('#message').focus();
	});
	
	
	//general ajax function for all requests 
	function makeRequest(url, async) {
		var httpRequest;
		if (window.XMLHttpRequest) {
			// Mozilla, Safari, ...
			httpRequest = new XMLHttpRequest();
		} else if (window.ActiveXObject) {
			// IE
			try {
				httpRequest = new ActiveXObject("Msxml2.XMLHTTP");
			} 
			catch (e) {
				try {
					httpRequest = new ActiveXObject("Microsoft.XMLHTTP");
				} 
				catch (e) {}
			}
		}

		if (!httpRequest) {
			alert('Giving up :( Cannot create an XMLHTTP instance');
			return false;
		}
		httpRequest.open('POST', url,async);
		httpRequest.send();
		return httpRequest;
	}
})(jQuery);