package ru.job4j.dream.servlet;

import ru.job4j.dream.model.User;
import ru.job4j.dream.store.PsqlStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RegServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("reg.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        User existingUser = PsqlStore.instOf().findUserByEmail(email);
        if (existingUser == null) {
            User newUser = new User();
            newUser.setName(name);
            newUser.setPassword(password);
            newUser.setEmail(email);
            PsqlStore.instOf().save(newUser);
            resp.sendRedirect(req.getContextPath() + "/auth.do");
            return;
        }
        req.setAttribute("error", "Такой пользователь уже сущаствует");
        req.getRequestDispatcher("reg.jsp").forward(req, resp);
    }
}
