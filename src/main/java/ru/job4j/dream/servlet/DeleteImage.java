package ru.job4j.dream.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class DeleteImage extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fileName = req.getParameter("id");
        boolean found = false;
        File[] files = new File("/home/vasya/Private/job4j/files/").listFiles();
        if (files != null) {
            for (File name : files) {
                if (fileName.equals(name.getName())) {
                    name.delete();
                    RequestDispatcher dispatcher = req.getRequestDispatcher("candidates.do");
                    dispatcher.forward(req, resp);
                    found = true;
                }
            }
        }
        if (!found) {
            resp.sendError(404, "File is not Found");
        }
    }
}
