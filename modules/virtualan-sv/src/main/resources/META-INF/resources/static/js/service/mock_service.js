'use strict';

myApp.factory('MockService', ['$http', '$q', function($http, $q){

    var REST_SERVICE_URI = 'virtualservices';
    var REST_SERVICE_URI_MESSAGE = 'virtualservices/message';

    var REST_SERVICE_URI_LOAD = 'virtualservices/load';
    var REST_SERVICE_URI_SWAGGER = 'api-catalogs';
    var REST_SERVICE_URI_LOAD_TOPIC = 'virtualservices/load-topics';

    var factory = {
        loadAllMockRequest: loadAllMockRequest,
        loadAllTopics: loadAllTopics,
        createMockMsgRequest: createMockMsgRequest,
        fetchAllMsgMockRequest: fetchAllMsgMockRequest,
        fetchAllMockRequest: fetchAllMockRequest,
        createMockRequest: createMockRequest,
        updateMockRequest:updateMockRequest,
        deleteMsgMockRequest:deleteMsgMockRequest,
        deleteMockRequest:deleteMockRequest,
        loadCatalogFiles:loadCatalogFiles,
        loadCatalogNames:loadCatalogNames,
        readApplicationName:readApplicationName
    };

    return factory;

    
    function loadCatalogFiles(name) {
        var deferred = $q.defer();
        $http.get(REST_SERVICE_URI_SWAGGER+"/"+name)
            .then(
            function (response) {
                deferred.resolve(response.data);
            },
            function(errResponse){
                console.error('Error while loading catalogs');
                deferred.reject(errResponse);
            }
        );
        return deferred.promise;
    }

    function loadCatalogNames() {
        var deferred = $q.defer();
        $http.get(REST_SERVICE_URI_SWAGGER)
            .then(
            function (response) {
                deferred.resolve(response.data);
            },
            function(errResponse){
                console.error('Error while loading catalogs');
                deferred.reject(errResponse);
            }
        );
        return deferred.promise;
    }

    function loadAllMockRequest() {
        var deferred = $q.defer();
        $http.get(REST_SERVICE_URI_LOAD)
            .then(
            function (response) {
                deferred.resolve(response.data);
            },
            function(errResponse){
                console.error('Error while fetching loadAllMockRequest');
                deferred.reject(errResponse);
            }
        );
        return deferred.promise;
    }


    function loadAllTopics() {
            var deferred = $q.defer();
            $http.get(REST_SERVICE_URI_LOAD_TOPIC)
                .then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function(errResponse){
                    deferred.reject(errResponse);
                }
            );
            return deferred.promise;
        }
    
    function readApplicationName() {
        var deferred = $q.defer();
        $http.get(REST_SERVICE_URI+'/app-name')
            .then(
            function (response) {
            	 deferred.resolve(response.data);
            },
            function(errResponse){
                console.error('Error while fetching MockRequests');
                deferred.reject(errResponse);
            }
        );
        return deferred.promise;
    }


    function fetchAllMsgMockRequest() {
        var deferred = $q.defer();
        $http.get(REST_SERVICE_URI_MESSAGE)//+'?operationId='+operationId+'&resource='+resource)
            .then(
            function (response) {
                deferred.resolve(response.data);
            },
            function(errResponse){
                console.error('Error while fetching MockRequests');
                deferred.reject(errResponse);
            }
        );
        return deferred.promise;
    }

    function fetchAllMockRequest() {
        var deferred = $q.defer();
        $http.get(REST_SERVICE_URI)//+'?operationId='+operationId+'&resource='+resource)
            .then(
            function (response) {
                deferred.resolve(response.data);
            },
            function(errResponse){
                console.error('Error while fetching MockRequests');
                deferred.reject(errResponse);
            }
        );
        return deferred.promise;
    }

    function createMockRequest(mockLoadRequest) {
        var deferred = $q.defer();
        $http.post(REST_SERVICE_URI, mockLoadRequest)
            .then(
            function (response) {
                deferred.resolve(response.data);
            },
            function(errResponse){
                console.error('Error while creating MockRequest');
                deferred.reject(errResponse);
                return errResponse;
            }
        );
        return deferred.promise;
    }

    function createMockMsgRequest(mockLoadRequest) {
        var deferred = $q.defer();
        $http.post(REST_SERVICE_URI_MESSAGE, mockLoadRequest)
            .then(
            function (response) {
                deferred.resolve(response.data);
            },
            function(errResponse){
                console.error('Error while creating MockRequest');
                deferred.reject(errResponse);
                return errResponse;
            }
        );
        return deferred.promise;
    }


    function updateMockRequest(mockLoadRequest, id) {
        var deferred = $q.defer();
        $http.put(REST_SERVICE_URI+"/"+id, mockLoadRequest)
            .then(
            function (response) {
                deferred.resolve(response.data);
            },
            function(errResponse){
                console.error('Error while updating MockRequest');
                deferred.reject(errResponse);
            }
        );
        return deferred.promise;
    }


    function deleteMsgMockRequest(id) {
        var deferred = $q.defer();
        $http.delete(REST_SERVICE_URI_MESSAGE+"/"+id)
            .then(
            function (response) {
                deferred.resolve(response.data);
            },
            function(errResponse){
                console.error('Error while deleting msg MockRequest');
                deferred.reject(errResponse);
            }
        );
        return deferred.promise;
    }

    function deleteMockRequest(id) {
        var deferred = $q.defer();
        $http.delete(REST_SERVICE_URI+"/"+id)
            .then(
            function (response) {
                deferred.resolve(response.data);
            },
            function(errResponse){
                console.error('Error while deleting MockRequest');
                deferred.reject(errResponse);
            }
        );
        return deferred.promise;
    }

}]);


