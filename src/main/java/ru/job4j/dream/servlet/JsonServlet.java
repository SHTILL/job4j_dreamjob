package ru.job4j.dream.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.job4j.dream.model.Email;
import ru.job4j.dream.model.User;
import ru.job4j.dream.store.PsqlStore;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final Gson gson = new GsonBuilder().create();
        String emailStr = req.getParameter("data");
        Email email = gson.fromJson(emailStr, Email.class);
        User found = PsqlStore.instOf().findUserByEmail(email.getEmail());
        String userJson = "";
        if (found != null) {
            userJson = gson.toJson(found);
        }
        PrintWriter writer = new PrintWriter(resp.getOutputStream());
        writer.println(userJson);
        writer.flush();
    }
}
