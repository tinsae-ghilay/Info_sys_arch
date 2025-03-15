#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <cstring>
#include <thread>
#include <vector>
#include <netinet/in.h>
#include <sys/socket.h>
#include <unistd.h>
#include <fcntl.h>
#include <arpa/inet.h>  // Required for inet_ntoa


// Size of the data we can receive at a time
const int SIZE_OF_CHUNK = 2048;
// Server IP (0.0.0.0 means listening on all available network interfaces)
const std::string HOST = "0.0.0.0";
// Port 8080, which is common if port 80 is restricted
const int PORT = 8080;

// Function to determine MIME type
std::string getMimeType(const std::string& filePath) {
    if (filePath.size() >= 5 && filePath.substr(filePath.size() - 5) == ".html") {
        return "text/html";
    }
    if (filePath.size() >= 4 && filePath.substr(filePath.size() - 4) == ".htm") {
        return "text/html";
    }
    if (filePath.size() >= 4 && filePath.substr(filePath.size() - 4) == ".jpg") {
        return "image/jpeg";
    }
    if (filePath.size() >= 5 && filePath.substr(filePath.size() - 5) == ".jpeg") {
        return "image/jpeg";
    }
    if (filePath.size() >= 4 && filePath.substr(filePath.size() - 4) == ".png") {
        return "image/png";
    }
    if (filePath.size() >= 4 && filePath.substr(filePath.size() - 4) == ".gif") {
        return "image/gif";
    }
    if (filePath.size() >= 4 && filePath.substr(filePath.size() - 4) == ".css") {
        return "text/css";
    }
    if (filePath.size() >= 3 && filePath.substr(filePath.size() - 3) == ".js") {
        return "application/javascript";
    }
    if (filePath.size() >= 5 && filePath.substr(filePath.size() - 5) == ".json") {
        return "application/json";
    }
    return "application/octet-stream"; // Default MIME type
}


// Function to parse the HTTP request and extract the file path
std::tuple<std::string, std::string, std::string> parseRequest(const std::string& data) {
    try {
        std::istringstream stream(data);
        std::string line;
        std::getline(stream, line);

        // Split the first line into parts
        std::istringstream lineStream(line);
        std::string method, path, version;
        lineStream >> method >> path >> version;

        // Clean up the path (remove the leading "/")
        if (path == "/") {
            return {"200", "OK", "index.html"};
        }

        // Try to open the requested file
        std::ifstream file(path.substr(1), std::ios::binary);
        if (file.is_open()) {
            return {"200", "OK", path.substr(1)};
        }
        // if we cannot open the file
        // it's not there. so we return 404
        return {"404", "Not found", "404.html"};
    } catch (...) {
        return {"500", "Bad request", "500.html"};
    }
}

// Function to handle the client connection
void serve(int clientSocket, const sockaddr_in& address) {
    std::cout << "Access requested from " << inet_ntoa(address.sin_addr) << std::endl;

    std::string data;
    char buffer[SIZE_OF_CHUNK];

    while (true) {
        ssize_t bytesRead = recv(clientSocket, buffer, SIZE_OF_CHUNK, 0);
        if (bytesRead <= 0) break;
        data.append(buffer, bytesRead);
        if (bytesRead < SIZE_OF_CHUNK) break;
    }

    // Parse the request
    auto [code, message, filePath] = parseRequest(data);

    // Send HTTP response headers
    std::string response = "HTTP/1.1 " + code + " " + message + "\r\nAccept-Ranges: bytes\r\n";
    send(clientSocket, response.c_str(), response.length(), 0);

    // Get content type
    std::string contentType = getMimeType(filePath);
    response = "Content-Type: " + contentType + "\r\n\r\n";
    send(clientSocket, response.c_str(), response.length(), 0);

    // Read and send the file in chunks
    std::ifstream file(filePath, std::ios::binary);
    if (file.is_open()) {
        while (file) {
            file.read(buffer, SIZE_OF_CHUNK);
            ssize_t bytesRead = file.gcount();
            send(clientSocket, buffer, bytesRead, 0);
            if (bytesRead < SIZE_OF_CHUNK) break;
        }
        file.close();
    }

    // Close the client socket
    close(clientSocket);
    std::cout << "Done serving " << inet_ntoa(address.sin_addr) << std::endl;
}

// Main server function
void startServer() {
    int serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (serverSocket < 0) {
        std::cerr << "Error opening socket." << std::endl;
        return;
    }

    sockaddr_in serverAddress;
    serverAddress.sin_family = AF_INET;
    serverAddress.sin_addr.s_addr = INADDR_ANY;
    serverAddress.sin_port = htons(PORT);

    if (bind(serverSocket, (struct sockaddr*)&serverAddress, sizeof(serverAddress)) < 0) {
        std::cerr << "Binding failed." << std::endl;
        return;
    }

    listen(serverSocket, 5);

    try {
        while (true) {
            sockaddr_in clientAddress{};
            socklen_t clientLength = sizeof(clientAddress);
            int clientSocket = accept(serverSocket, (struct sockaddr*)&clientAddress, &clientLength);
            if (clientSocket < 0) {
                std::cerr << "Error accepting connection." << std::endl;
                continue;
            }

            std::thread(serve, clientSocket, clientAddress).detach();
        }
    } catch (const std::exception& e) {
        std::cerr << "Server interrupted: " << e.what() << std::endl;
    }

    close(serverSocket);
}

// Main function
int main() {
    startServer();
    return 0;
}
