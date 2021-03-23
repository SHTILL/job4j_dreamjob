package ru.job4j.dream.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DownloadServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        File downloadFile = null;
        File[] files = new File("/home/vasya/Private/job4j/files/").listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (name.equals(file.getName())) {
                    downloadFile = file;
                    break;
                }
            }
            if (downloadFile != null) {
                resp.setContentType("application/octet-stream");
                resp.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFile.getName() + "\"");
                try (FileInputStream stream = new FileInputStream(downloadFile)) {
                    resp.getOutputStream().write(stream.readAllBytes());
                }
            }
        }
    }
}
