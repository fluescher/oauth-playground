var playground = {
	apiEndpoint: "http://localhost:8080/oauth-server/api/",
	token: "",
	
	obtainToken: function(goodCallback) {
		var request = new XMLHttpRequest();
		request.open("POST", "http://localhost:8080/oauth-server/webapp/obtainToken");
		request.onreadystatechange = function() {
			if(request.readyState == 4) {
				if(request.status == 200) {
					console.log("GOT Token: " + request.responseText);
					playground.token = request.responseText;
					goodCallback();
				} else {
					console.log("Could not get token ["+request.status+"]: " + request.responseText);
				}
			}
		};
		request.send(null);
	},
	
	getOptions: function(finishedCallback) {
		playground.doAuthRequest("GET", playground.apiEndpoint + "options", finishedCallback);
	},
	
	getAllOptions: function(finishedCallback) {
		playground.doAuthRequest("GET", playground.apiEndpoint + "allOptions", finishedCallback);
	},
	
	doAuthRequest: function(method, target, finishedCallback) {
		var request = new XMLHttpRequest();
		request.open(method, target);
		request.setRequestHeader("Authorization", "Bearer " + playground.token);
		request.onreadystatechange = function() {
			if(request.readyState == 4) {
				finishedCallback(request.responseText);
			}
		};
		request.send(null);
	}
};