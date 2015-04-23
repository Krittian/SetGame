console.log("yo");

//start game
//creates global set card converter array
//sends request to server for initial cards
//asks for unique game id
cardConverterArray = [];
requestTimer = null;
requestDelay = 100; //ms
gameDiv = null;

boardWidth = 7;
boardHeight = 3;
boardGrid = null;

uid = "";
name = "";
gid = "";

currSelectedList = [];
currSelectedCells = []; //corresponding list to html elems that you clicked on
stateNum = -1; //initialized to -1 so that when you start up game, you automattically get an update
hasUpdate = false; //this turns true when three cards are selected. At next request to server, it is set to false.

//used to map trinary number to card picture file locations
setCardMap = [["one","two","three"],["green","purple","red"],["diamond","oval","wavy"],["solid","clear","shaded"]];

$(function() {
    init();
});

function init() {
	console.log("sateNUm " + stateNum);
    gameDiv = $('#setboard');
    createGameBoard();
	uid = getCookie("uid");
	name = getCookie("name");
    getUrlParams();
    cardConverterArray = createCardArray();
    
    //initBoard
    deleteBoard();
    addCard("0000",3,3); 
    //addCard(72,6,2);
    //addCard(43,0,0);
    //addCard(56,4,1);
    //eventHandlers
    $('#setboard').on('click','img',function() {
	cell = $(this).parent();
        x = cell.data('x');
        y = cell.data('y');
        console.log("clicked x:" + x + " y:" + y);
	
	cardCode = cell.find('img').data('cardcode');
        console.log('code: ' + cardCode);
	cardIndex = currSelectedList.indexOf(cardCode);
	if (cardIndex == -1) {
		cell.css('background','yellow');
		currSelectedList.push(cardCode);
		currSelectedCells.push(cell);

		if (currSelectedList.length >= 3) {
			hasUpdate = true;
		}
        	console.log(currSelectedList);
	} else {
		cell.css('background','none');
		currSelectedList.splice(cardIndex,1); //delete that card
		currSelectedCells.splice(cardIndex,1);
	}	


	//removeCard(x,y);
    });

    requestTimer = window.setInterval(function () {
	
	//true when stateNum == -1 just to get state of game when a player first enters game
	if (hasUpdate || stateNum == -1) {
		requestObj = {uid:uid,gameId:gid,set:currSelectedList,hasUpdate:hasUpdate,stateNum:stateNum};
		//console.log("trying to send set");
		//console.log("stateNum" + stateNum);
		//console.log(JSON.stringify(requestObj));
		//console.log("udpate!");
		//console.log(requestObj);

		if (hasUpdate) { //but only undo selection when there's an update	
		    undoSetSelect();
		}
	} else {
		requestObj = {uid:uid,gameId:gid,hasUpdate:hasUpdate, stateNum:stateNum};
	}

	$.ajax({
		type: "POST",
		url: "/gameDATA",
		dataType: "json",
		contentType: 'application/json; charset=UTF-8',
		data: JSON.stringify(requestObj),
		    success: function (data, textStatus, jqXHR) {
			handleUpdate(data);
			//stateNum; //should actually read stateNum value from data
		     }
        });
    },requestDelay);    


	
    
}

	//
function leaveGame(){
	//TODO tell server you want to leave the game.
	requestObj = {uid:uid,gameId:gid,leaveGame:true};
	$.ajax({
		type: "POST",
		url: "/gameDATA",
		dataType: "json",
		contentType: 'application/json; charset=UTF-8',
		data: JSON.stringify(requestObj),
		    success: function (data, textStatus, jqXHR) {
			window.location.href = "/lobby";
	     }
        });
	
}

/*
window.onbeforeunload = function(){
//alert("closing");
return false;
}
*/



//4D loop
//creates strings with four letters. letters represent properties of each card
//[0] = shape
//[1] = color
//[2] = number
//[3] = fill
function createCardArray() {
    tempCardConvArray = [];
    count = 0;
    for (var s = 0; s < 3; s++) {function getUrlParameter(sParam)
{
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++) 
    {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam) 
        {
            return sParameterName[1];
        }
    }
} 
        for (var c = 0; c < 3; c++) {
            for (var n = 0; n < 3; n++) {
                for (var f = 0; f < 3; f++) {
                    tempCardConvArray[count] = (""+s)+(""+c)+(""+n)+(""+f); //beautiful, untyped language!
                    count++;
                }
            }
        }
    }
    return tempCardConvArray;
}

function createGameBoard() {
    gameTable = $("#setboard");
    for (i=0; i<3; i++) {
	tempRow = $('<tr></tr>');
	for (j=0; j<7; j++) {
	    tempRow.append("<td class=setCell data-x=" + j + " data-y=" + i + "></td>");
	}
	gameTable.append(tempRow);
    }
}

function createUserBoard(players,scores) {
    userTable = $("#userboard");
	table = "<tr><th colspan='2'><b>" + gid.replace(/_/g," ") + "</b></th><tr><tr><th>Players</th><th>Scores</th></tr>";
	for (i=0;i<players.length; i++) {
	    table = table + "<tr><td>" + players[i] + "</td><td>" + scores[i] + "</td></tr>";
	}
	//table += "</table>"
	console.log(table);
	userTable.html (table);
}


function getUrlParams() {
//	uid = getUrlParam('uid');using cookie instead
	gid = getUrlParam('gid');
}

function getUrlParam(sParam)
{
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++) 
    {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam) 
        {
            return sParameterName[1];
        }
    }
} 

function addCard(trinCardCode,x,y) {
    decCardCode = parseInt(trinCardCode,3); //trinary
    removeCard(x,y);
    //$("td[data-x=" + x + "][data-y=" + y + "]").append('<div class=setCard data-cardcode=' + cardCode + '>'+ cardCode + '</div>');
    picPath = "http://ee.cooper.edu/~herzbe" + "/setCards";
    for (j = 0; j < 4; j++) {
        paramChoices = setCardMap[j];
        currTrigit = parseInt(trinCardCode.charAt(j));
        picPath = picPath + "/" + paramChoices[currTrigit];   
    }
    picPath += "/card.PNG";
    //console.log(picPath);
    $("td[data-x=" + x + "][data-y=" + y + "]").append('<img data-cardcode=' + decCardCode + ' src='+picPath+' ></img>');
    boardGrid[x][y] = decCardCode;
}

function removeCard(x,y) {
    $("td[data-x=" + x + "][data-y=" + y + "]").empty();
    boardGrid[x][y] = -1;
}

function deleteBoard() {
    boardGrid = new Array(boardWidth);
    for (i = 0; i < boardWidth; i++) {
        boardGrid[i] = new Array(boardHeight);
        for (j = 0; j < boardHeight; j++) {
            boardGrid[i][j] = -1;
	    //console.log('removdfdfdf');
	    removeCard(i,j);
        }
    }
}

function handleUpdate(data) {
	if (data["hasUpdate"]) {
		deleteBoard();
		stateNum = data["stateNum"];
		if(stateNum == undefined)
			stateNum = -1;
		cards = data["cards"];
		players = data["players"];
		playerScores= data["playerScores"];

		console.log("players: " + players);
		console.log("playerScores: " + playerScores);
		createUserBoard(players,playerScores);
		if (cards.length < 12) {
			console.log("END GAME STATE");
			//remove all cards before adding them
		}
		for (i = 0; i < cards.length; i++) {
			x = i%boardWidth;
			y = Math.floor(i/boardWidth);			
			//console.log("adding: " + x + " " + y + " cardCode: " + cards[i]);
			addCard(cards[i],x,y);
		}
	}
}

//when you have selected 3 cards and an update has been requested, deselect cards
function undoSetSelect() {
	hasUpdate = false;
	currSelectedList = [];
	for (i = 0; i < currSelectedCells.length; i++) {
		currSelectedCells[i].css('background','none');
	}
	
}



function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1);
        if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
    }
    return "";
}


