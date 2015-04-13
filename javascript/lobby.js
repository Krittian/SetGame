var userinfo = "";
var currGameIds = []; //updated on requests to server
var currGameNames = [];
var uid = ""; //crazy long UUID string


function init() {
    console.log("init");
    $("#games").hide();
}
function requestGameListData() {
    $.ajax({
	type: "POST",
	url: "/lobbyDATA",
	dataType: "json",
	contentType: 'application/json; charset=UTF-8',
	data: JSON.stringify({gamelist_request: true}),
	    success: function (data, textStatus, jqXHR) {

	     }
     });
 }
function sendLoginData() {
    console.log("yoooo");
     var userdata = {
	 name: document.getElementById("name").value,
	 password: document.getElementById("password").value
     };
     $.ajax({
	 type: "POST",
	 url: "/lobbyDATA",
	 dataType: "json",
	 contentType: 'application/json; charset=UTF-8',
	 data: JSON.stringify({login: userdata}),
	 success: function (data, textStatus, jqXHR) {
	     if (data["authentication"] === true) {
		 $("#login").hide();
		 $("#games").show();
		 $("#submitted").text("Welcome back " + userdata["name"]+"!");
		 userinfo = data["userinfo"];
		 currGameIds = data["gameIds"];
		 currGameNames = data["gameNames"];
		 uid = data["uid"];
		 refreshGameTable(currGameIds,currGameNames);
	     } else {
		 $("#submitted").text("invalid username or password. try again.");
	     }
	}
    });
}

function addGame() {
    gameName = $("#newgamename").val();
    if (gameName == "") {
	$("#newgameerror").text("Please enter a name");
	return;
    }
    var newGameData = {
	uid: uid,
	gameName: gameName,
	request: "new_game"
    };

    $.ajax({
	type: "POST",
	url: "/lobbyDATA",
	dataType: "json",
	contentType: "application/json; charset=UTF-8",
	data: JSON.stringify({gameData: newGameData}),
	success: function (data, textStatus, jqXHR0 ) {
	    gameId = data["gameId"];
	    addToGameTable(gameId,gameName);
	}
    });
}

function addToGameTable(id,name) {
    $("#gamesTable").prepend("<tr data-id=" + id + "><td><a href=\'/game/?gid="+id+"&uid="+uid+"\'>" + name + "</a></td></tr>");
}

//idList - uuids of currently active games
//nameList - names corresponding to uuids
function refreshGameTable(idList, nameList) {
   $("#gamesTable").empty(); 
   for (i = 0; i < idList.length; i++) {
	addToGameTable(idList[i],nameList[i]);
   }
   if (idList.length == 0) { //there are no games right now..
	
   }

}


$(function() {
    init();
    $("#loginSubmit").click(function(event) {
	event.preventDefault();
	sendLoginData();
    });
});
