var userinfo = "";
var currGameIds = []; //updated on requests to server
var currGameNames = [];
var uid = ""; //crazy long UUID string
requestTimer = null;
requestDelay = 1000; //ms

$(function () {
    init();
});


function signInWithCookies() {
    uid1 = getCookie("uid");
    if (uid1 != "") {//try to login using uid cookie
        var userdata = {
            uid: uid1
        };
        $.ajax({
            type: "POST",
            url: "/lobbyDATA",
            dataType: "json",
            contentType: 'application/json; charset=UTF-8',
            data: JSON.stringify({uid_signin: userdata}),
            success: function (data, textStatus, jqXHR) {
                goodCookieOrLogin(data);
            }
        });
    }
}

function displaySignup() {
    $("#signup").show();
    $("#login").hide();
    $("#displaySignup").hide();//	text("BOB");//
    $("#displayLogin").show();
}

function displayLogin() {
    $("#signup").hide();
    $("#login").show();
    $("#displaySignup").show();
    $("#displayLogin").hide();
}

function init() {
    $("#signup").hide();
    $("#logout").hide();
    $("#displayLogin").hide();
    $("#games").hide();


    $("#loginSubmit").click(function (event) {
        event.preventDefault();
        name = document.getElementById("name").value.replace(/ /g, "_");
        password = document.getElementById("password").value;
        sendLoginData(name, password);
    });
    $("#signupSubmit").click(function (event) {
        event.preventDefault();
        sendSignupData();
    });


    signInWithCookies();
}

function setCookie(cname, cvalue) {
    // var d = new Date();
    //d.setTime(d.getTime() + (exdays*24*60*60*1000));
    //var expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + "; ";// + expires;
}

function deleteCookie(name) {
    document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}

function logout() {
    $("#displaySignup").hide();
    $("#logout").hide();
    deleteCookie("uid");
    deleteCookie("name");
    location.reload();
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ')
            c = c.substring(1);
        if (c.indexOf(name) == 0)
            return c.substring(name.length, c.length);
    }
    return "";
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
                $(".signupSubmitted").text(userdata["name"] + ", you signed up successfully!");
                sendLoginData(userdata["name"], userdata["password"]);
            } else {
                $(".signupSubmitted").text("There was a problem... Try another username.");
            }
        }
    });
}

function goodCookieOrLogin(data) {
    $("#login").hide();
    $("#logout").show();
    $("#displaySignup").hide();
    $("#signup").hide();
    $("#games").show();
    name = getCookie("name");
    $(".submitted").text("Welcome back " + name + "!");
    userinfo = data["userinfo"];
    uid = data["uid"];
    setCookie("uid", uid);
    setCookie("name", name);
    updateGames(data)
}

function updateGames(data) {
    currGameIds = data["gameIds"];
    currGameNames = data["gameNames"];
    currGamePlayerCounts = data["gamePlayerCounts"];
    refreshGameTable(currGameIds, currGameNames, currGamePlayerCounts);
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
                setCookie("name", userdata["name"]);
                goodCookieOrLogin(data);
            } else {
                $(".submitted").text("invalid username or password. try again.");
            }
        }
    });
}

function addGame() {
    gameName = $("#newgamename").val().replace(/ /g, "_");
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
        success: function (data, textStatus, jqXHR0) {
            // go stright to the game
            gameId = data["gameId"];
            //addToGameTable(gameId,gameName);
            window.location.href = getGameLink(gameId);
        }
    });
}

function getGameLink(id) {
    return '/game/?gid=' + id;
}

function addToGameTable(id, name, count) {
    if (name == "BetaMidrash") $("#gamesTable").append("<tr><td><a href='https://play.google.com/store/apps/details?id=com.torahsummary.betamidrash'>BetaMidrash</a></td><td>613</td></tr>");
    else $("#gamesTable").append("<tr data-id=" + id + "><td><a href=\'" + getGameLink(id) + "\'>" + name.replace(/_/g, " ") + "</a></td><td>" + count + "</td></tr>");
}

//idList - uuids of currently active games
//nameList - names corresponding to uuids
function refreshGameTable(idList, nameList, countList) {
    $("#gamesTable").empty();
    $('#gamesTable').append("<tr><td>Game Name</td><td>Num Players</td></tr>");
    for (i = 0; i < idList.length; i++) {
        addToGameTable(idList[i], nameList[i], countList[i]);
    }
    if (idList.length == 0) { //there are no games right now..
	$('#gamesTable').append("<tr><td colspan=2>(No games...Enter a name above and start the fun!)</td></tr>");
    }

}


requestTimer = window.setInterval(function () {
    var requestObj = {
        uid: uid,
        request: "gamelist_request"
    }
    $.ajax({
        type: "POST",
        url: "/lobbyDATA",
        dataType: "json",
        contentType: 'application/json; charset=UTF-8',
        data: JSON.stringify({gameData: requestObj}),
        success: function (data, textStatus, jqXHR) {
            updateGames(data);
        }
    });
}, requestDelay); 
