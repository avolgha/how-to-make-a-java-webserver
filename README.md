<h1 align="center">How To Make A Java-WebServer</h1>
<h6 align="center">Tutorial made at 10. June 2021</h6>

## » Table of contents

>* [First Words](#-first-words)
>* [What you need?](#-what-you-need)
>* [Project Structure](#-structure)
>* [First class, main method and other things](#-the-first-class-main-method-and-other-things)
>* [The mystical part of creating a server](#-the-mystical-part-of-creating-a-server)
>* [How to handle the request](#-how-to-handle-the-request)
>* [How to write data to the user](#-how-to-write-data-to-the-user)
>* [Other things](#-other-things)
>* [The code](#-the-code)
>* [Contact](#-contact)

## » First Words
>Hey, <br>
If you want to use this code in your project, I would like to mention that this is no Professional Code and there are many better ways to implement HTTP for Java, like [Spring](https://spring.io/). <br>
This code is for self learning and not for using, because I don't think that this is much extendable or good usable code for big projects or companies. <br>
This code is for new users of the Java-Language that want to learn how the implementation of HTTP in Java works, to maybe use it for creating better and bigger frameworks. <br>
~ Marius

## » What you need?

The nice thing at Java is that you need: nothing! <br>
You only need a working JDK.

><b>Info:</b> I test this on [JDK 14.0.2](https://www.oracle.com/java/technologies/javase/14-0-2-relnotes.html). 
The tutorial might not work on newer or older versions of Java, but it should work

And it would be good if you have an IDE like [IntelliJ IDEA](https://www.jetbrains.com/idea/), [Eclipse](https://www.eclipse.org/), [Apache NetBeans](https://netbeans.apache.org/) or anything else that adds help for code editing and smart suggestions to your life <i>(I would say)</i>

Later I use a JSON library to validate raw json strings. You can find the library [here](https://repo1.maven.org/maven2/org/json/json/20210307/)

## » Structure

I think the best way to structure the project is that:

>* Project Dir
>    * www
>        * index.html
>        * 404.html
>        * [...]
>    * WebServer.java
    
If you have another structure you want to use: Do! (but I'm using this one...)

## » The first class, main method and other things

So as you seen in the [Structure](#-structure), I like to put my web server code in a class named `WebServer.java`, soothers can easily see, what the class does. <br>

Then you have to create the `main` method. Your class should look like this:
````java
package some.nice.name;

public class WebServer {
    public static void main(String[] args) {
    }
}
````

Now we specify some basic fields above the `main()` method.
````java
private static final String REGEX_URL_SPLIT = "/";
private static final int PORT = 8080;
private static final boolean verbose = true;
````

>The `REGEX_URL_SPLIT` we use later for splitting the request path, so we get all text of the request path as one array

>The `PORT` is, as it says, the port we want our server to listen on. If you don't want to specify the port everytime you connect to the server, you should set it to `80`.<br><br>
<b>Warning:</b> If you are running any other service like `nginx` or `Apache HTTP`, this could come to conflicts because they want also to use this port. Then please choose another port for any of these services or for this server

>The `verbose` is used to specify if any extra logging should be made.

As next step you can implement `java.lang.Runnable` in your `WebServer` class and implement the `run()` method

Now you create a local final field with type of `java.net.Socket` with the name `socket`. Then you create a constructor where you require this Socket as parameter and give it back to the local final field.

Your code should look like this now:
````java
package some.nice.name;

public class WebServer implements Runnable {
    private static final String REGEX_URL_SPLIT = "/";
    private static final int PORT = 8080;
    private static final boolean verbose = true;

    private final Socket socket;

    public WebServer(Socket socket) {
        this.socket = socket;
    }
    
    public static void main(String[] args) {
    }

    @Override
    public void run() {}
}
````

## » The mystical part of creating a server

So go back to the `main()` method. Now create a try-catch block with a `IOException`-catch-block

In the try-block, you now create an instance of a `java.net.ServerSocket`: 
````java
ServerSocket serverSocket = new ServerSocket(WebServer.PORT);
````
and you can optionally add a logging that the server is online now:
````java
System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
````

Then we need an infinite-loop. 
> My ways to create infinite loops are these:
>
>````java
>while (true) {
>}
>```` 
>````java 
>for(;;) {
>}
>```` 
>There are also more ways, but these I use

Now create an instance of the `WebServer` class with your ServerSocket as parameter:
````java
WebServer server = new WebServer(serverSocket.accept());
````

Then add an optional logging and start the web server:
````java
if (verbose) {
    System.out.println("Connection opened. (" + new Date() + ")");
}

new Thread(server).start();
````

The `main()` method should now look like this:
````java
try {
    ServerSocket serverSocket = new ServerSocket(WebServer.PORT);
    System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

    while (true) {
        WebServer server = new WebServer(serverSocket.accept());

        if (verbose) {
            System.out.println("Connection opened. (" + new Date() + ")");
        }

        new Thread(server).start();
    }
} catch (IOException e) {
    System.err.println("Server Connection error : " + e.getMessage());
}
````

## » How to handle the request? 

Through the fact, that we implemented the `Runnable`-Interface in our class, we can use the `run()` method to interact with the user.

In the `run` method you can now create a try-catch-finally-block that is looking like this:
````java
try (BufferedReader       requestReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
     PrintWriter          headerWriter  = new PrintWriter(socket.getOutputStream());
     BufferedOutputStream contentWriter = new BufferedOutputStream(socket.getOutputStream())) {
} catch (IOException exception) {
    System.err.println("Server error : " + exception);
} finally {
    if (verbose) {
        System.out.println("Connection closed.\n");
    }
}
````

You maybe wonder about the syntax I use here. It is called `try-with-resources Statement`. <br>
I don't want to explain it here but if you want to know more about that, read the documentation that is linked here
>An official documentation page of this you can find [here](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html)

But I want to explain the arguments in the `try`-block to you:
* `requestReader` <br>
    ==> Every time you call the server, a request will be created where developers read things like your IP, request headers, auth states, and the path you query from
* `headerWriter` <br>
    ==> As every request, also every response has headers. There are things like the status of you request, the type of the response (html/json/etc) and other cool things
* `contentWriter` <br>
    ==> With the content writer you can write content to the user that will be displayed in their Web Browser, Terminal, or wherever they call this server
  
Now we can check the request and read the HTTP Method and the requested path from there:
````java
StringTokenizer parse = new StringTokenizer(requestReader.readLine());
String method = parse.nextToken().toUpperCase();
String requested = parse.nextToken().toLowerCase();

if (!method.equals("GET")) {
    if (verbose) {
        System.out.println("501 Not implemented : " + method + " method.");
    }

    sendJson(headerWriter, contentWriter, 501, "{\"error\":\"Method not implemented. Please use GET instead\"}");
} else {
    String[] urlSplit = requested.split(WebServer.REGEX_URL_SPLIT);
}
````

And you might have wondered about the `sendJson` method. We will create this soon in the next section

But what are we doing in the code?

Basically we read the request and parse it into a `StringTokenizer`. Then we read the `method` and the requested path (`requested`) from there.

In the `if` we check if the user calls the server with `HTTP GET`. If that isn't the case, we send the user a deny-message in json. Otherwise, we split the requested path at `REGEX_URL_SPLIT` (`/`) and put it into an array.

After the array, we can write now the code where we send the data to the user. But wait for that until the next section!

After this code the class should look like this:
````java
public class WebServer implements Runnable {
    static final String REGEX_URL_SPLIT = "/";

    static final int PORT = 8080;

    static final boolean verbose = true;

    private final Socket socket;

    public WebServer(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(WebServer.PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

            while (true) {
                WebServer server = new WebServer(serverSocket.accept());

                if (verbose) {
                    System.out.println("Connection opened. (" + new Date() + ")");
                }

                new Thread(server).start();
            }
        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try (BufferedReader       requestReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter          headerWriter  = new PrintWriter(socket.getOutputStream());
             BufferedOutputStream contentWriter = new BufferedOutputStream(socket.getOutputStream())) {

            StringTokenizer parse = new StringTokenizer(requestReader.readLine());
            String method = parse.nextToken().toUpperCase();
            String requested = parse.nextToken().toLowerCase();

            if (!method.equals("GET")) {
                if (verbose) {
                    System.out.println("501 Not implemented : " + method + " method.");
                }

                sendJson(headerWriter, contentWriter, 501, "{\"error\":\"Method not implemented. Please use GET instead\"}");
            } else {
                String[] urlSplit = requested.split(WebServer.REGEX_URL_SPLIT);
            }
        } catch (IOException exception) {
            System.err.println("Server error : " + exception);
        } finally {
            if (verbose) {
                System.out.println("Connection closed.\n");
            }
        }
    }
}
````

## » How to write data to the user?

To write data to the user, we have our `headerWriter` and our `contentWriter`. 

HTTP headers let the client, and the server pass additional information with an HTTP request or response. A complete documentation for you can find [here](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers) 

><b>Good to know</b>
> 
>The data we send over the `contentWriter` are bytes, not plain strings.
> 
>You can get the bytes of a string by using:
>````java
>String randomString = "test";
>byte[] randomStringBytes = randomString.getBytes();
>````
>or you can specify also a Charset (like UTF-8):
>````java
>byte[] randomStringBytes = randomString.getBytes(StandardCharsets.UTF_8);
>````

<h4 align="center">So, what we need in our response?</h4>

* Headers
    * Format of the response (like `HTTP/1.1 200 OK`)
    * The name of the server (`Server: yourserver`)
    * The date of the response (`Date: ` and add behind this an `+ new Date()`)
    * The content type of our response (`Content-Type: text/html` or `Content-Type: application/json`)
    * The content length of the response data we want to send (`Content-Length: 1000`)
* Data

<h4 align="center">So lets build a method for handle that all!</h4>

I will a method called `write` for that. As method parameters we need these:
````java
PrintWriter headerWriter
BufferedOutputStream contentWriter, 
int statusCode, 
String contentType, 
byte[] response, 
int responseLength
````

The `statusCode` is the status the request got at your server. <br>
So as example: If we call the `index.html` page on a web server, you should <i>EVERYTIME</i> get a 200 status,  because every website should have an index page. <b>BUT</b> if the page doesn't have this requested page, there will be sent a 404 status code back, that means that the page does not exist.

If you know that you will only send HTML or JSON you can remove this `contentType` and later add the response type where the `contentType` should be used. 

In the `response` we have the data that will be sent to the user. No string, raw bytes.

`````java
static void write(PrintWriter headerWriter, BufferedOutputStream contentWriter, int statusCode, String contentType, byte[] response, int responseLength) throws IOException {
    HttpStatusCode httpStatusCode = HttpStatusCode.getByResult(statusCode);
    
    headerWriter.println(String.format("HTTP/1.1 %d %s", statusCode, httpStatusCode == null ? "Unknown" : httpStatusCode.name()));
    headerWriter.println("Server: HTTP Server : 1.0");
    headerWriter.println("Date: " + new Date());
    headerWriter.println("Content-type: " + contentType);
    headerWriter.println("Content-length: " + responseLength);
    headerWriter.println();
    headerWriter.flush();

    contentWriter.write(response, 0, responseLength);
    contentWriter.flush();
}
`````

And as you might see here, I had implemented a `HttpStatusCode` enum. I will come to this soon.

So what we are doing is that:
* First send the headers, we defined [here](#so-what-we-need-in-our-response)
* Then we write the data and the length of the data through the BufferedOutputStream

<i>And that was the magic behind Java WebServers.</i>

<h4 align="center">The HttpStatusCode enum</h4>

So to implement this, you basically only need to copy the `HttpStatusCode.java` file of the `src/` directory in this repository. Then put this in your code and that was it.

In there are - <i>kinda</i> - all Http Status Codes, with their name and status code.

>If you know the status code but not the name, you can easily check for it:
> 
>````java
>String status = "unknown";
>int statusCode = 200;
>HttpStatusCode httpStatusCode = HttpStatusCode.getByResult(statusCode);
>
>if (httpStatusCode != null) {
>    status = httpStatusCode.name();
>}
>````

<h4 align="center">Helper methods are nice! Don't say anything else...</h4>

The `write()` method is good but what is, if we don't want to specify `text/html` as content type, everytime we call the method. We could make a method that do this automatically for us.

````java
static void sendHtml(PrintWriter headerWriter, BufferedOutputStream contentWriter, int statusCode, String content) throws IOException {
    write(headerWriter, contentWriter, statusCode, "text/html", content.getBytes(StandardCharsets.UTF_8), content.length());
}
````

The method is very similar to the first one, but has nice changes. We don't have to specify a content type, or the content length anymore. Also, we have now a string as data. The java compiler does now the work with transforming the string into bytes and the calculating of the length for you. It's easy! And you can do it for everything you want

## » Other things

<h4 align="center">JSON validation and sending</h4>

As I mentioned earlier I will use the `org.json.json` library for this project. Feel free to use some library like  `Jackson` or `json-simple`. If you want to download the library I use, look [here](#-what-you-need)

>#### Simple Introduction to `org.json.json`
>````java
>JSONObject json = new JSONObject("{\"test\": \"nice\", \"message\": [\"test\"]}");
>String test = json.getString("test");
>JSONArray messages = json.getJSONArray("message");
>List<String> messageList = messages.toList().stream().filter(String.class::isInstance).map(String.class::cast).collect(Collectors.toList());
>
>json.put("name", "Marius");
>
>String rawJSON = json.toString(4);
>```` 
>
>###### Java Streams... WTF?
>if you aren't familiar with Java Streams, that code might look very obvious to you.
>Basically, because the `messages.toList()` method returns objects, I check if the given object is a String and filter it out. Then I cast all other remaining string objects to strings


The nice thing is, you'll get an exception if you create an instance of a JSON Object, and the parsed json isn't valid.
So we can easily see, if there is valid json.

````java
public boolean isValidJson(String raw) [
    try {
        new JSONObject(raw);
        return true;
    } catch(JSONException e) {
        return false;    
    }
]
````

With this knowledge, we can create a method that sends json to the user:

````java
static void sendJson(PrintWriter headerWriter, BufferedOutputStream contentWriter, int statusCode, String json) throws IOException {
    try {
        new JSONObject(json); // the code will cancel here if the json is not valid
        write(headerWriter, contentWriter, statusCode, "application/json", json.getBytes(StandardCharsets.UTF_8), json.length());
    } catch(JSONException e) {
        throw new IOException(e.getMessage());
    }
}
````

## » The code

You can find the code of the whole project in the `src/` directory of the repository. Feel free to copy it

## » Contact

You can write me on Discord. My tag is `Marius#0686`

If you want to create a translated version of this, please link my repository as originally one