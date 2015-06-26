var app = angular.module('webCrawler', [ 'ngRoute' ]);

app.config([ '$routeProvider', function($routeProvider) {
    $routeProvider.when("/", {
	templateUrl : "partials/home.html",
	controller : "PageCtrl"
    })
} ]);

app.controller('PageCtrl', function($scope, $http) {
    $scope.baseUrl = "github.com/jdkn74/empty-repo";
    var crawl = function(crawlUrl) {
	$("#links").empty();
	$("#staticAssets").empty();
	$("#siteToString").html("");
	$http.post(crawlUrl, {
	    "baseUrl" : $scope.baseUrl,
	    "maxSites" : $scope.maxSites
	}).success(
		function(data, status, headers, config) {
		    if (data.links !== undefined) {
			var links = new Graph();
			data.links.map(function(d) {
			    links.addEdge(d.fromHref, d.toHref, {
				"directed" : true
			    });
			});
			new Graph.Layout.Spring(links).layout();
			new Graph.Renderer.Raphael('links', links, 1200, Math
				.max(data.links.length / 2, 400)).draw();

			var staticAssets = new Graph();
			data.staticAssets.map(function(d) {
			    staticAssets.addEdge(d.fromHref, d.toHref, {
				"directed" : true
			    });
			});
			new Graph.Layout.Spring(staticAssets).layout();
			new Graph.Renderer.Raphael('staticAssets',
				staticAssets, 1200, Math.max(
					data.staticAssets.length / 2, 400))
				.draw();
		    } else {
			$("#siteToString").html(data);
		    }
		}).error(function(data, status, headers, config) {
	    alert('error!' + data);
	});
    }

    $scope.crawlInMemory = function() {
	crawl('crawl/in-memory');
    }
    $scope.crawlFileStore = function() {
	crawl('crawl/persistent');
    }
});