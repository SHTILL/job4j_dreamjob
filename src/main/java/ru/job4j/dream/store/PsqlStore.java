package ru.job4j.dream.store;

import org.apache.commons.dbcp2.BasicDataSource;
import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.City;
import ru.job4j.dream.model.Post;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.dream.model.User;

public class PsqlStore implements Store {
    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());
    private final BasicDataSource pool = new BasicDataSource();

    private PsqlStore() {
        Properties cfg = new Properties();
        try (BufferedReader io = new BufferedReader(
                new FileReader("db.properties")
        )) {
            cfg.load(io);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        pool.setDriverClassName(cfg.getProperty("jdbc.driver"));
        pool.setUrl(cfg.getProperty("jdbc.url"));
        pool.setUsername(cfg.getProperty("jdbc.username"));
        pool.setPassword(cfg.getProperty("jdbc.password"));
        pool.setMinIdle(5);
        pool.setMaxIdle(10);
        pool.setMaxOpenPreparedStatements(100);
    }

    private static final class Lazy {
        private static final Store INST = new PsqlStore();
    }

    public static Store instOf() {
        return Lazy.INST;
    }

    @Override
    public Collection<Post> findAllPosts() {
        List<Post> posts = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("SELECT * FROM post")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    posts.add(new Post(it.getInt("id"), it.getString("name")));
                }
            }
        } catch (Exception e) {
            LOG.warn("Exception while retrieving posts", e);
        }
        return posts;
    }

    @Override
    public Collection<Candidate> findAllCandidates() {
        List<Candidate> posts = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("SELECT candidate.id as id," +
                     "candidate.name as name," +
                     "cities.name as city " +
                     "FROM candidate join cities on candidate.city_id = cities.id")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    posts.add(new Candidate(it.getInt("id"), it.getString("name"), it.getString("city")));
                }
            }
        } catch (Exception e) {
            LOG.warn("Exception while retrieving candidates", e);
        }
        return posts;
    }

    @Override
    public void save(User user) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("INSERT INTO admins(name, email, password) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    user.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.warn("Exception while creating a new user", e);
        }
    }

    @Override
    public void save(Post post) {
        if (post.getId() == 0) {
            create(post);
        } else {
            update(post);
        }
    }

    private Post create(Post post) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("INSERT INTO post(name) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, post.getName());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    post.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.warn("Exception while creating a new post", e);
        }
        return post;
    }

    private void update(Post post) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("UPDATE post set name = ? where id = ?;")
        ) {
            ps.setInt(1, post.getId());
            ps.setString(2, post.getName());
            ps.executeQuery();
        } catch (SQLException e) {
            LOG.warn("Exception while updating a post Id" + post.getId() + e);
        }
    }

    @Override
    public void save(Candidate candidate) {
        if (candidate.getId() == 0) {
            create(candidate);
        } else {
            update(candidate);
        }
    }

    private Candidate create(Candidate candidate) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("INSERT INTO candidate(name, city_id)" +
                     "VALUES (?, (SELECT id from public.cities where name = ?))", PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, candidate.getName());
            ps.setString(2, candidate.getCity());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    candidate.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.warn("Exception while creating a candidate", e);
        }
        return candidate;
    }

    private void update(Candidate candidate) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("UPDATE candidate set name = ?," +
                     "city_id = (SELECT id from public.cities where name = ?) where id = ?;")
        ) {
            ps.setString(1, candidate.getName());
            ps.setString(2, candidate.getCity());
            ps.setInt(3, candidate.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.warn("Exception while updating a candidate Id:" + candidate.getId() + e);
        }
    }

    @Override
    public User findUserByEmail(String email) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("select * from admins where email = ?;"))
        {
            ps.setString(1, email);
            ResultSet res = ps.executeQuery();
            if (res.next()) {
                User user = new User();
                user.setId(res.getInt("id"));
                user.setName(res.getString("name"));
                user.setPassword(res.getString("password"));
                return user;
            }
        } catch (SQLException e) {
            LOG.warn("Exception while retrieving a user with email:" + email + e);
        }
        return null;
    }

    @Override
    public Post findPostById(int id) {
        try (Connection cn = pool.getConnection();
            PreparedStatement ps = cn.prepareStatement("select * from post where id = ?;"))
        {
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();
            if (res.next()) {
                return new Post(id, res.getString("name"));
            }
        } catch (SQLException e) {
            LOG.warn("Exception while retrieving a post by Id:" + id + e);
        }
        return null;
    }

    @Override
    public Candidate findCandidateById(int id) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("SELECT candidate.id as id," +
                     "candidate.name as name," +
                     "cities.name as city FROM candidate join cities on candidate.city_id = cities.id"))
        {
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();
            if (res.next()) {
                return new Candidate(id, res.getString("name"), res.getString("city"));
            }
        } catch (SQLException e) {
            LOG.warn("Exception while retrieving a candidate by Id:" + id + e);
        }
        return null;
    }

    @Override
    public List<City> findAllCity() {
        List<City> cities = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("select * from cities"))
        {
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                City c = new City();
                c.setId(res.getInt("id"));
                c.setName(res.getString("name"));
                cities.add(c);
            }
        } catch (SQLException e) {
            LOG.warn("Exception while retrieving cities", e);
        }
        return cities;
    }
}