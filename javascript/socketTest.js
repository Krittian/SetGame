$(function() {
	console.log('init');
	var ws = new WebSocket('ws://199.98.20.121:8083/socketDATA');
	ws.onopen=function(){
		//send first ping
		ping();
	};

	ws.onclose = function(event) {
		console.log('Client notified socket has closed',event);
	};
	ws.onmessage=function(event){
		console.log("got:"+ev.data);

		ping();
	};
	function ping(){
		ws.send("ping");
	}
});
