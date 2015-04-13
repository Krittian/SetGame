var userinfo = "";
var currGameIds = []; //updated on requests to server
var currGameNames = [];
var uid = ""; //crazy long UUID string

$(function() {
    init();
});

function init() {
    console.log("init");
	$("#loginSubmit").click(function(event) {
		event.preventDefault();
		name = document.getElementById("name").value;
		password = document.getElementById("password").value;
		sendLoginData(name,password);
    });
	$("#signupSubmit").click(function(event) {
		event.preventDefault();
		sendSignupData();
    });
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
 
 function sendSignupData() {
    console.log("signing UP");
     var userdata = {
	 name: document.getElementById("signupname").value,
	 password: document.getElementById("signuppassword").value
     };
     $.ajax({
	 type: "POST",
	 url: "/lobbyDATA",
	 dataType: "json",
	 contentType: 'application/json; charset=UTF-8',
	 data: JSON.stringify({user_signup_request: userdata}),
	 success: function (data, textStatus, jqXHR) {
	     if (data["authentication"] === true) {
		 $("#signup").hide();
		 $("#signupSubmitted").text(userdata["name"] +", you signed up successfully!");
		 sendLoginData(userdata["name"],userdata["password"]);
	     } else {
		 $("#signupSubmitted").text("There was a problem...  Make sure your name/password combo is correct.");
	     }
	}
    });
}
 
function sendLoginData(name, password) {
    console.log("yoooo");
     var userdata = {
	 name: name,
	 password: password
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
		 $("#signup").hide();
		 $("#games").show();
		 $("#submitted").text("Welcome back " + userdata["name"]+"!");
		 userinfo = data["userinfo"];
		 currGameIds = data["gameIds"];
		 currGameNames = data["gameNames"];
		 currGamePlayerCounts  = data["gamePlayerCounts"];
		 uid = data["uid"];
		 console.log(data);
		 refreshGameTable(currGameIds,currGameNames, currGamePlayerCounts);
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

function addToGameTable(id,name,count) {
    $("#gamesTable").prepend("<tr data-id=" + id + "><td><a href=\'/game/?gid="+id+"&uid="+uid+"\'>" + name + "</a> Number of Players: " + count + "</td></tr>");
}

//idList - uuids of currently active games
//nameList - names corresponding to uuids
function refreshGameTable(idList, nameList,countList) {
   $("#gamesTable").empty(); 
   for (i = 0; i < idList.length; i++) {
	addToGameTable(idList[i],nameList[i],countList[i]);
   }
   if (idList.length == 0) { //there are no games right now..
	
   }

}


