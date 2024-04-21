package httpserver.utils;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.server.Service;
import seb.dal.ServiceNotFoundException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RequestHandler implements Runnable{
    private final Socket clientSocket;
    private final Router router;
    private final PrintWriter printWriter;
    private final BufferedReader bufferedReader;

    public RequestHandler(Socket clientSocket, Router router) throws IOException {
        this.clientSocket = clientSocket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        this.printWriter = new PrintWriter(this.clientSocket.getOutputStream(), true);
        this.router = router;
    }

    @Override
    public void run() {
        try {
            Response response;
            Request request = new RequestBuilder().buildRequest(this.bufferedReader);

            if (request.getPathname() == null) {
                response = new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "[]"
                );
            } else {
                Service service;
                try {
                    service = this.router.resolve(request.getServiceRoute());
                    response = service.handleRequest(request);
                } catch (ServiceNotFoundException e) {
                    response = new Response(
                            HttpStatus.NOT_FOUND,
                            ContentType.JSON,
                            "{\"error\": \"NotFound\"}"
                    );
                }


            }
            printWriter.write(response.get());
        } catch (IOException e) {
            System.err.println(Thread.currentThread().getName() + " Error: " + e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (printWriter != null) {
                    printWriter.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
