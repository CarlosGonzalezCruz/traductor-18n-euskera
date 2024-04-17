window.exports = window.exports || {};

(function() {

    window.exports.FileRequestCardsPerId = {};

    window.exports.FileRequestCard = class {

        constructor(element, requestData) {
            this.id = requestData.id;
            this.element = element;
            this.requestData = requestData;
        }

        
        static create($fileRequestCardContainer, $fileRequestCardTemplate, requestData) {
            let $newCard = $fileRequestCardTemplate.clone();
            $newCard.removeClass("d-none");
            $newCard.attr("request-id", requestData.id);
            $fileRequestCardContainer.append($newCard);
            $newCard.removeAttr("id");

            $newCard.find("*[field='sourceName']").text(requestData.sourceName);
            $newCard.find("*[field='status']").text(requestData.status);
            
            let ret = new this($newCard[0], requestData);
            window.exports.FileRequestCardsPerId[ret.id] = ret;
            ret.handler();

            if(requestData.status == "ERROR") {
                $newCard.find("*[field='progress-error']").removeClass("d-none");
                $newCard.find("*[field='progress-error-content']").text(requestData.errorMessage);
            } else {
                ret.update({
                    current: 0,
                    total: 0,
                    requestStatus: requestData.status
                });
            }

            return ret;
        }


        update(data) {
            let progressRatio = data.current / data.total * 100;

            if(data.requestStatus == "PENDING") {
                $(this.element).find("*[role='progress-container']").removeClass("d-none");
                $(this.element).find("*[role='progress-done']").addClass("d-none");
                $(this.element).find("*[role='progress-wait']").removeClass("d-none");
                $(this.element).find("*[role='progress-values']").addClass("d-none");
                $(this.element).find("*[role='progress-loading-spinner']").addClass("d-none");
            
            } else if(data.requestStatus == "IN_PROGRESS") {
                $(this.element).find("*[role='progress-container']").removeClass("d-none");
                $(this.element).find("*[role='progress-done']").addClass("d-none");
                $(this.element).find("*[role='progress-wait']").addClass("d-none");
                $(this.element).find("*[role='progress-values']").removeClass("d-none");
                $(this.element).find("*[role='progress-loading-spinner']").removeClass("d-none");

                $(this.element).find("*[role='progress-container'] .progress-bar").css("width", progressRatio + "%");
                $(this.element).find("*[role='progress-current']").text(data.current);
                $(this.element).find("*[role='progress-total']").text(data.total);

                let remainingTimeFormatted = formatTimeToDisplay(data.remainingTimeNS);

                if(remainingTimeFormatted) {
                    $(this.element).find("*[role='progress-estimated-time']").text(remainingTimeFormatted);
                }
            
            } else if(data.requestStatus == "DONE") {
                $(this.element).find("*[role='progress-container']").addClass("d-none");
                $(this.element).find("*[role='progress-done']").removeClass("d-none");

            } else if(data.requestStatus == "CANCELLED") {
                $(this.element).find("*[role='progress-container']").removeClass("d-none");
                $(this.element).find("*[role='progress-done']").removeClass("d-none");

            }
        }


        handler() {
            $(this.element).find("*[role='downloadSource']").on("click", async () => {
                await downloadFile("/request/source?requestId=" + this.id);
            });

            $(this.element).find("*[role='downloadTranslated']").on("click", async () => {
                await downloadFile("/request/translated?requestId=" + this.id);
            });

            $(this.element).find("*[role='cancel']").on("click", async () => {
                await fetch("/request?requestId=" + this.id, {
                    method: "DELETE"
                });
            });
        }

    };


    async function downloadFile(url) {
        let response = await fetch(url);
        
        let contentDisposition = response.headers.get('content-disposition');
        let filename = contentDisposition.match("filename=(.*?)(?:;|$)")[1];
        let fileBlob = await response.blob();
        let fileURL = window.URL.createObjectURL(fileBlob);

        // Simulamos el activar un enlace de descarga para poder descargar el archivo
        // correctamente con el nombre que indica el servidor
        $("#auxiliar-download-link")[0].href = fileURL;
        $("#auxiliar-download-link")[0].download = filename;
        $("#auxiliar-download-link")[0].click();
    }


    function formatTimeToDisplay(timeNS) {
        if(timeNS == null) {
            return null;
        }

        let timeInSeconds = Math.ceil(timeNS * 10**-9);
        let timeInMinutes = Math.floor(timeInSeconds / 60);
        let timeInHours = Math.floor(timeInMinutes / 60);

        timeInSeconds -= timeInMinutes * 60;
        timeInMinutes -= timeInHours * 60;

        return timeInHours.toString().padStart(2, '0') + ":"
                + timeInMinutes.toString().padStart(2, '0') + ":"
                + timeInSeconds.toString().padStart(2, '0')
    }

})();