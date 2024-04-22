window.exports = window.exports || {};

(function() {

    let stompClient;

    function init() {
        stompClient = new StompJs.Client({
            brokerURL: 'ws://' + window.location.host + '/progress',
            onConnect: () => {
                console.log("Cliente conectado");
                stompClient.subscribe('/topic/updates', updateCards);
                stompClient.subscribe('/topic/requests', m => renderAllRequests(JSON.parse(m.body)));
                
                $("#disconnectedErrorMessage").modal("hide");
                getCurrentRequests();
            },
            onDisconnect: () => {
                console.log("Cliente desconectado");
            },
            reconnectDelay: 1000
        });

        stompClient.onWebSocketClose = function(e) {
            $("#disconnectedErrorMessage").modal("show");
        }

        stompClient.activate();

        $("#main-form").on("submit", function(e) {
            e.preventDefault();
            submitForm();
            
        });

        $("#file-upload-field").on("change", function() {
            if($(this)[0].files.length == 0) {
                $("#button-submit").prop("disabled", true);
            } else {
                $("#button-submit").prop("disabled", false);
            }
        });

        $("#button-clear-completed").on("click", async function() {
            await fetch("/request/completed", {
                method: "DELETE"
            });
        });
    }


    async function submitForm() {
        $("#text-error-content").parent().addClass("d-none");

        let formData = new FormData($("#main-form")[0]);

        let response = await fetch("/request", {
            method: "POST",
            body: formData
        });

        if(!response.ok) {
            let parsedResponse = await response.json();
            renderErrorMessage(parsedResponse.errorMessage);
        } else {
            console.log(await response.text());
            $("#file-upload-field").val(null).trigger("change");
            setTimeout(() => $("#file-request-container > div").last()[0]
                .scrollIntoView({ behavior: "smooth", block: "end" }),
                200);
        }
    }


    function updateCards(message) {
        let data = JSON.parse(message.body);

        let requestCard = window.exports.FileRequestCardsPerId[data.requestId];
        if(requestCard) {
            requestCard.update(data);
        }
    }


    async function getCurrentRequests() {
        let response = await fetch("/request/all")
        let requests = await response.json();
        renderAllRequests(requests);
    }


    function renderAllRequests(requests) {
        
        if(requests.length == 0) {
            $("#file-request-empty-filler").removeClass("d-none");
            $("#file-request-container").addClass("d-none");
        } else {
            $("#file-request-empty-filler").addClass("d-none");
            $("#file-request-container").removeClass("d-none");
        }

        let container = $("#file-request-container");
        let template = $("#file-request-card-template");

        container.children().not("#file-request-card-template").remove();
        window.exports.FileRequestCardsPerId = {};

        for(let request of requests) {
            window.exports.FileRequestCard.create(container, template, request);
        }
    }


    function renderErrorMessage(content) {
        $("#text-error-content").text(content);
        $("#text-error-content").parent().removeClass("d-none");
    }
        
    
    init();

})();