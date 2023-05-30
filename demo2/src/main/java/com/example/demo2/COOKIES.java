package com.example.demo2;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;
import java.util.*;

@WebServlet(name = "Cookie", value = "/cookies")
public class COOKIES extends HttpServlet {
    private static final String filePath = "C:\\Users\\Дмитрий\\IdeaProjects\\demo1\\visitors.txt";
    private static final String uniqID = "lastVisit";
    private static final String user = "user";
    private static final AtomicInteger counter = new AtomicInteger();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = req.getSession();
        UserDto user;
        user = (UserDto) session.getAttribute(COOKIES.user);
        if (user == null) {
            user = new UserDto(25L, "asaaa@gmail.com");
            session.setAttribute(COOKIES.user, user);
        }


        var browser = getBrowser(req.getHeader("User-Agent"));
        var ipAddress = req.getRemoteAddr();
        var lastVisitDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        var cookies = req.getCookies();
        var writer = resp.getWriter();
        resp.setContentType("text/html");

        if (Arrays.stream(cookies)
                .filter(cookie -> uniqID.equals(cookie.getName()))
                .findFirst()
                .isEmpty()) {
            var cookie = new Cookie(uniqID, lastVisitDate);
            cookie.setMaxAge(-1);
            resp.addCookie(cookie);
            counter.incrementAndGet();

            writer.write("<h1>Добро пожаловать</h1>");
            Files.writeString(Paths.get(filePath), lastVisitDate+" "+ipAddress+" "+browser+"\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        else {
            writer.write("<h1>С возвращением!</h1>");
            String cookieValue;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(uniqID)) {
                    cookieValue = cookie.getValue();
                    cookie.setValue(lastVisitDate);
                    writer.write("<p>Последнее посещение: " + cookieValue + ".</p>");
                    writer.write("<p>Браузер: " + browser + ".</p>");
                    break;
                }
            }
        }

        writer.write("<h1> Кол-во уникальных посетителей:" + counter.get() + "</h1>");

        System.out.println("SERVLET REQUEST ATTRIBUTES");
        printAttributes(req);
        System.out.println(" SERVLET CONTEXT ATTRIBUTES");
        printAttributes(req.getServletContext());
        System.out.println("HTTPSESSION ATTRIBUTES");
        printAttributes(req.getSession());
    }

    private static String getBrowser(String userAgent) {
        if (userAgent.contains("Edg")) return "Microsoft Edge";
        else if (userAgent.contains("Chrome")) return "Google Chrome";
        else return "Unknown";
    }

    private static void printAttributes(Object object) {
        if (object != null) {
            var context = object instanceof ServletContext ? (ServletContext) object : null;
            var session = object instanceof HttpSession ? (HttpSession) object : null;
            var request = object instanceof HttpServletRequest ? (HttpServletRequest) object : null;

            Enumeration<String> attributes = null;
            if (context != null) attributes = context.getAttributeNames();
            else if (session != null) attributes = session.getAttributeNames();
            else if (request != null) attributes = request.getAttributeNames();
            if (!attributes.hasMoreElements()) System.out.println("No attributes");
            while (attributes != null && attributes.hasMoreElements()) {
                String attributeName = attributes.nextElement();
                Object attributeValue = null;
                if (context != null) attributeValue = context.getAttribute(attributeName);
                else if (session != null) attributeValue = session.getAttribute(attributeName);
                else if (request != null) attributeValue = request.getAttribute(attributeName);
                System.out.println(attributeName + " = " + attributeValue);
            }
        }
    }
}