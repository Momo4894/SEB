package seb.dal.repository;

import seb.dal.DataAccessException;
import seb.dal.UnitOfWork;
import seb.model.User;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserRepository {
    private UnitOfWork unitOfWork;

    public UserRepository(UnitOfWork unitOfWork) { this.unitOfWork = unitOfWork; }

    public Collection<User> findAllUsers() {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                        select * from users
                        """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();
            Collection<User> userRows = new ArrayList<>();
            while(resultSet.next())
            {
                User user = new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password")
                );
                userRows.add(user);
            }
            return userRows;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }

    public void addUser(User user) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                        insert into users (username, password, elo) values (?, ?, ?)
                        """))
        {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setInt(3, user.getElo());
            preparedStatement.executeUpdate();
            this.unitOfWork.commitTransaction();

        } catch (SQLException e) {
            throw new DataAccessException("Insert nicht erfolgreich", e);
        }
    }

    public void addUserData(User user) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        update users set name = ?, bio = ?, image = ? where username = ?
                        """))
        {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getBio());
            preparedStatement.setString(3, user.getImage());
            preparedStatement.setString(4, user.getUsername());
            System.out.println(user.getName());
            System.out.println(user.getBio());
            System.out.println(user.getImage());
            System.out.println(user.getUsername());
            preparedStatement.executeUpdate();
            this.unitOfWork.commitTransaction();
        } catch (SQLException e) {
            throw new DataAccessException("Update nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public void loginUser(User user) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                        select * from users where username = ? and password = ?
                        """))
        {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new IOException("No user found");
            }

        } catch (SQLException e) {
            throw new DataAccessException("select nicht erfolgreich", e);
        } catch (IOException e) {
            throw new DataAccessException("No user found");
        }
    }

    public User getUser(String username) {
        System.out.println("in getUser");
        try (PreparedStatement preparedStatement =
                     /*this.unitOfWork.prepareStatement("""
                        select * from users where username = ?
                        """)*/this.unitOfWork.prepareStatement("SELECT * FROM users WHERE username = ?"))

        {
            preparedStatement.setString(1, username);
            System.out.println("before cor");
            System.out.println(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("after");
            if (resultSet.next()) {
                System.out.println("in resultset next");
                User user = new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getInt("elo"),
                        resultSet.getString("name"),
                        resultSet.getString("bio"),
                        resultSet.getString("image")
                );
                return user;
            }
            System.out.println("no resultset.next()");
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Select nicht erfolgreich: " + e.getMessage(), e);
        }
    }

    public List getUser() {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                        select * from users
                        """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();
            List userRows = new ArrayList<>();
            while (resultSet.next()) {
                User user = new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password")
                );
                userRows.add(user);
            }
            return userRows;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }
}
