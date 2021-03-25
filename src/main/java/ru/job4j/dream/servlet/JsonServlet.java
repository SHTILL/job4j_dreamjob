package ru.job4j.dream.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.job4j.dream.model.User;
import ru.job4j.dream.store.PsqlStore;
import ru.job4j.dream.store.Store;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("data");
        final Gson gson = new GsonBuilder().create();
        User found = PsqlStore.instOf().findUserByEmail(email);
        String userJson = "";
        if (found != null) {
            userJson = gson.toJson(found);
        }
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Methods","GET, OPTIONS, HEAD, PUT, POST");
        PrintWriter writer = new PrintWriter(resp.getOutputStream());
        writer.println(userJson);
        writer.flush();
    }
}
