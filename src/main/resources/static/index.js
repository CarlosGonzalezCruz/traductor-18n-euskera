
(function() {

    let stompClient;
    let cancelRequested;

    function init() {
        stompClient = new StompJs.Client({
            brokerURL: '/progress',
            onConnect: () => {
                stompClient.subscribe('/topic/updates', updateProgressWindow);
                stompClient.subscribe('/topic/errors', processErrorMessage);
            },
            onDisconnect: () => {

            }
        });

        stompClient.activate();
        cancelRequested = false;

        $("#main-form").on("submit", function(e) {
            e.preventDefault();
            startProgressWindow();
            submitForm();
        });

        $("#file-upload-field").on("change", function() {
            if($(this)[0].files.length == 0) {
                $("#button-submit").prop("disabled", true);
            } else {
                $("#button-submit").prop("disabled", false);
            }
        });

        $("#progress-button-request-cancel").on("click", function() {
            requestCancel();
        });
    }


    async function submitForm() {
        try {
            cancelRequested = false;
            let formData = new FormData($("#main-form")[0]);
            let response = await fetch("", {
                method: "POST",
                body: formData
            });

            if(cancelRequested) {
                return;
            }

            let contentDisposition = response.headers.get('content-disposition');
            let filename = contentDisposition.match("filename=(.*?)(?:;|$)")[1];
            let fileBlob = await response.blob();
            let fileURL = window.URL.createObjectURL(fileBlob);
    
            // Simulamos el activar un enlace de descarga para poder descargar el archivo
            // correctamente con el nombre que indica el servidor
            $("#auxiliar-download-link")[0].href = fileURL;
            $("#auxiliar-download-link")[0].download = filename;
            $("#auxiliar-download-link")[0].click();
        
        } catch(e) {
            console.warn("No se descargará ningún archivo porque ha ocurrido un problema.");
        }
    }
    
    
    function startProgressWindow() {
        $("#progress-window").modal("show");
        $("#progress-window .progress-bar").attr("aria-valuenow", 0).css("width", "0%");
        $("#progress-estimated-time").addClass("d-none");
        $("#progress-container").addClass("hidden-by-opacity");
        $("#progress-current").text("-");
        $("#progress-total").text("-");

        $("#text-error-content").parent().addClass("d-none");
    }


    function stopProgressWindow() {
        let closeWindowInterval = setInterval(() => {
            $("#progress-container").addClass("hidden-by-opacity");
            if($("#progress-window").is(":visible")) {
                $("#progress-window").modal("hide");
                setTimeout(() => {
                    clearInterval(closeWindowInterval);   
                }, 500);
            }
        }, 250);
    }


    function updateProgressWindow(message) {
        let data = JSON.parse(message.body);
        let progressRatio = data.current / data.total * 100;
        
        $("#progress-window .progress-bar").attr("aria-valuenow", progressRatio).css("width", progressRatio + "%");
        $("#progress-current").text(data.current);
        $("#progress-total").text(data.total);

        if(data.remainingTimeNS) {
            $("#progress-estimated-time").text(formatTimeToDisplay(data.remainingTimeNS));
            $("#progress-estimated-time").removeClass("d-none");
        } else {
            $("#progress-estimated-time").addClass("d-none");
        }

        $("#progress-container").removeClass("hidden-by-opacity");

        if(data.done) {
            stopProgressWindow();
        }
    }


    function requestCancel() {
        cancelRequested = true;
        stompClient.publish({destination: "/app/cancel"});
    }


    function processErrorMessage(message) {
        let data = JSON.parse(message.body);

        $("#text-error-content").text(data.errorMessage);
        $("#text-error-content").parent().removeClass("d-none");

        stopProgressWindow();
    }


    function formatTimeToDisplay(timeNS) {
        let timeInSeconds = Math.ceil(timeNS * 10**-9);
        let timeInMinutes = Math.floor(timeInSeconds / 60);
        let timeInHours = Math.floor(timeInMinutes / 60);

        timeInSeconds -= timeInMinutes * 60;
        timeInMinutes -= timeInHours * 60;

        return timeInHours.toString().padStart(2, '0') + ":"
                + timeInMinutes.toString().padStart(2, '0') + ":"
                + timeInSeconds.toString().padStart(2, '0')
    }
    
    
    init();

})();