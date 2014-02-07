var playground = {
	apiEndpoint: "http://localhost:8080/oauth-server/api/",
	obtainTokenEndpoint: "obtainToken",
	token: "",
	scopes: [],
	
	obtainToken: function(scopes, goodCallback) {
		var request = new XMLHttpRequest();
		request.open("POST", playground.obtainTokenEndpoint + "?scope="+scopes);
		request.onreadystatechange = function() {
			if(request.readyState == 4) {
				if(request.status == 200) {
					playground.token = JSON.parse(request.responseText).access_token;
					console.log("GOT Token: " + playground.token);
					goodCallback();
				} else {
					console.log("Could not get token ["+request.status+"]: " + request.responseText);
				}
			}
		};
		request.send(null);
	},
	
	obtainOptionsScopeToken: function(goodCallback) { 
		playground.obtainToken("options", goodCallback);
	},
	
	obtainAllOptionsScopeToken: function(goodCallback) {
		playground.obtainToken("allOptions", goodCallback);
	},
	
	obtainAllScopesTokens: function(goodCallback) {
		playground.obtainToken("options%20allOptions", goodCallback);
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