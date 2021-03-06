package net.stzups.netty.http.handler.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import net.stzups.netty.http.HttpServerHandler;
import net.stzups.netty.http.MimeTypes;
import net.stzups.netty.http.exception.HttpException;
import net.stzups.netty.http.exception.exceptions.BadRequestException;
import net.stzups.netty.http.exception.exceptions.InternalServerException;
import net.stzups.netty.http.exception.exceptions.MethodNotAllowedException;
import net.stzups.netty.http.exception.exceptions.NotFoundException;
import net.stzups.netty.http.handler.HttpHandler;
import net.stzups.netty.http.objects.Route;
import net.stzups.netty.http.objects.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import static net.stzups.netty.http.HttpUtils.sendFile;
import static net.stzups.netty.http.HttpUtils.sendRedirect;

public class FileRequestHandler extends HttpHandler {
    public interface Config {
        String getHttpRoot();
        int getHttpCacheSeconds();
        String getMimeTypesFilePath();
        String getDebugJsRoot();
    }

    private static final String DEFAULT_FILE = "index.html"; // / -> /index.html
    private static final String DEFAULT_FILE_EXTENSION = ".html"; // /index -> index.html

    private final File jsRoot;
    private final File httpRoot;
    private final int httpCacheSeconds;
    private final MimeTypes mimeTypes = new MimeTypes();

    public FileRequestHandler(Config config) {
        super("/");
        httpRoot = new File(config.getHttpRoot());
        if (config.getDebugJsRoot().equals("")) {
            jsRoot = httpRoot;
        } else {
            jsRoot = new File(config.getDebugJsRoot());
        }
        httpCacheSeconds = config.getHttpCacheSeconds();
        String path = config.getMimeTypesFilePath();
        // check for mime types in working directory
        try (FileInputStream fileInputStream = new FileInputStream(path)) {
            mimeTypes.load(fileInputStream);
        } catch (IOException e) {
            // check for mime types in root of classpath
            InputStream inputStream = HttpServerHandler.class.getResourceAsStream(path.startsWith("/") ? "" : "/" + path);
            if (inputStream != null) {
                try {
                    mimeTypes.load(inputStream);
                } catch (IOException e1) {
                    e.printStackTrace();
                }
            } else {
                e.printStackTrace(); // non critical, server will just serve 404 responses
            }
        }
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        Route route = new Route(request.uri());

        if (!HttpMethod.GET.equals(request.method())) {
            throw new MethodNotAllowedException(request.method(), HttpMethod.GET);
        }

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        // redirects
        if (route.path().endsWith(DEFAULT_FILE)) { // /index.html -> /
            sendRedirect(ctx, request, response, HttpResponseStatus.PERMANENT_REDIRECT, route.path().substring(0, route.path().length() - DEFAULT_FILE.length()) + route.rawQuery());
            return true;
        } else if ((route.path() + DEFAULT_FILE_EXTENSION).endsWith(DEFAULT_FILE)) { // /index -> /
            response.setStatus(HttpResponseStatus.PERMANENT_REDIRECT);
            sendRedirect(ctx, request, response, HttpResponseStatus.PERMANENT_REDIRECT, route.path().substring(0, route.path().length() - (DEFAULT_FILE.length() - DEFAULT_FILE_EXTENSION.length())) + route.rawQuery());
            return true;
        } else if (route.path().endsWith(DEFAULT_FILE_EXTENSION)) { // /page.html -> /page
            response.setStatus(HttpResponseStatus.PERMANENT_REDIRECT);
            sendRedirect(ctx, request, response, HttpResponseStatus.PERMANENT_REDIRECT, route.path().substring(0, route.path().length() - DEFAULT_FILE_EXTENSION.length()) + route.rawQuery());
            return true;
        }

        // get filesystem filePath from provided filePath
        final String filePath;
        try {
            filePath = getFilePath(route.path());
        } catch (BadRequestException e) {
            throw new NotFoundException("Exception while getting file path for http request", e);
        }

        //todo this is mostly for debug
        File root;
        if (filePath.endsWith(".js")) {
            root = jsRoot;
        } else {
            root = httpRoot;
        }

        File file = new File(root, filePath);
        if (file.isHidden() || !file.exists() || file.isDirectory() || !file.isFile()) {
            if (new File(httpRoot, filePath.substring(0, filePath.length() - DEFAULT_FILE_EXTENSION.length())).isDirectory()) { // /test -> /test/ if test is a valid directory and /test.html does not exist
                sendRedirect(ctx, request, response, HttpResponseStatus.PERMANENT_REDIRECT, route.path() + "/" + route.rawQuery());
            } else {
                throw new NotFoundException("File at path " + filePath + " not found");
            }
            return true;
        }

        /*if (route.equals(AUTHENTICATE_PAGE)) {
            try {
                logIn(config, request, headers);
            } catch (FailedException e) {
                throw new InternalServerException("Exception while logging user in", e);
            }
        }*/
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "public,max-age=" + httpCacheSeconds);//cache but revalidate if stale todo set to private cache for resources behind authentication
        try {
            sendFile(ctx, request, response, file, mimeTypes.getMimeType(file));
            return true;
        } catch (IOException e) {
            throw new InternalServerException("Exception while sending file", e);
        }
    }



    private static final Pattern ALLOWED_PATH = Pattern.compile("^[\\\\" + File.separator + "." + Uri.FILE_NAME_REGEX + "]+$");

    /** Converts uri to filesystem path */
    private static String getFilePath(String path) throws BadRequestException {
        path = path.replace("/", File.separator);

        if (path.contains(File.separator + '.') // /.
                || path.contains('.' + File.separator) // ./
                || path.contains(File.separator + File.separator) // //
                || path.charAt(0) == '.' // .
                || path.charAt(path.length() - 1) == '.' // /page.
                || !ALLOWED_PATH.matcher(path).matches())
            throw new BadRequestException("File path contains illegal characters");

        if (path.endsWith(File.separator)) { // / -> /index.html
            path = path + DEFAULT_FILE;
        } else if (path.lastIndexOf(File.separator) > path.lastIndexOf(".")) { // /page -> /page.html
            path = path + DEFAULT_FILE_EXTENSION;
        }
        return path;
    }
}
