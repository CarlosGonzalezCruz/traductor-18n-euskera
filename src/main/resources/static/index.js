
(function() {

    let stompClient;

    function init() {
        stompClient = new StompJs.Client({
            brokerURL: '/progress',
            onConnect: () => {
                console.log("Cliente conectado");
                stompClient.subscribe('/topic/updates', updateProgressWindow);
            },
            onDisconnect: () => {
                console.log("Cliente desconectado");
            }
      });

      $("#mainForm").on("submit", () => {
            startProgressWindow();
      });
    }
    
    
    function startProgressWindow() {
        stompClient.activate();
        $("#progress-window").modal("show");
        $("#progress-window .progress-bar").attr("aria-valuenow", 0);
        $("#progress-current").text("---");
        $("#progress-total").text("---");
    }


    function stopProgressWindow() {
        stompClient.deactivate();
        $("#progress-window").modal("hide");
    }


    function updateProgressWindow(message) {
        let data = JSON.parse(message.body);
        console.log(data.current + "/" + data.total);
        let progressRatio = data.current / data.total * 100;

        $("#progress-window .progress-bar").attr("aria-valuenow", progressRatio).css("width", progressRatio + "%");
        $("#progress-current").text(data.current);
        $("#progress-total").text(data.total);

        if(data.done) {
            stopProgressWindow();
        }
    }
    
    
    init();

})();