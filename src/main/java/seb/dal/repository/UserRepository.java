package seb.dal.repository;

import seb.dal.DataAccessException;
import seb.dal.UnitOfWork;
import seb.model.User;

import java.io.IOException;
import java.sql.*;
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
            e.printStackTrace();
            if (this.unitOfWork.getConnection() != null) {
                this.unitOfWork.rollback(); // Rollback transaction if error occurs
            }
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }

    public int getUserId(String username) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                select id from users where username = ?
                """))
        {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getInt("id");
            }
            throw new IOException("No user found");
        } catch (SQLException e) {
            e.printStackTrace();
            if (this.unitOfWork.getConnection() != null) {
                this.unitOfWork.rollback(); // Rollback transaction if error occurs
            }
            throw new DataAccessException("Select nicht erfolgreich", e);
        } catch (IOException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public int getElo(String username) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                select elo from users where username = ?
                """))
        {


            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getInt("elo");
            }
            throw new IOException("No user found");
        } catch (SQLException e) {
            e.printStackTrace();
            if (this.unitOfWork.getConnection() != null) {
                this.unitOfWork.rollback(); // Rollback transaction if error occurs
            }
            throw new DataAccessException("Select nicht erfolgreich", e);
        } catch (IOException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void addUser(User user) {
        try {
            PreparedStatement preparedStatement = this.unitOfWork.prepareStatement(
                    "INSERT INTO users (username, password, elo) VALUES (?, ?, ?)"
            );

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

            preparedStatement.executeUpdate();
            this.unitOfWork.commitTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
            if (this.unitOfWork.getConnection() != null) {
                this.unitOfWork.rollback(); // Rollback transaction if error occurs
            }
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
            e.printStackTrace();
            if (this.unitOfWork.getConnection() != null) {
                this.unitOfWork.rollback(); // Rollback transaction if error occurs
            }
            throw new DataAccessException("select nicht erfolgreich", e);
        } catch (IOException e) {
            e.printStackTrace();
            if (this.unitOfWork.getConnection() != null) {
                this.unitOfWork.rollback(); // Rollback transaction if error occurs
            }
            throw new DataAccessException("No user found");
        }
    }

    public User getUser(String username) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("SELECT * FROM users WHERE username = ?"))

        {
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
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
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            if (this.unitOfWork.getConnection() != null) {
                this.unitOfWork.rollback(); // Rollback transaction if error occurs
            }
            throw new DataAccessException("Select nicht erfolgreich: " + e.getMessage(), e);
        }
    }

    public List<User> getUser() {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                        select id, username, password from users
                        """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<User> userRows = new ArrayList<>();
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
            e.printStackTrace();
            if (this.unitOfWork.getConnection() != null) {
                this.unitOfWork.rollback(); // Rollback transaction if error occurs
            }
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }

    public List<User> getUsernameIdElo() {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                        select id, username, elo from users
                        """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<User> userRows = new ArrayList<>();
            while (resultSet.next()) {
                User user = new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getInt("elo")
                );
                userRows.add(user);
            }
            return userRows;
        } catch (SQLException e) {
            e.printStackTrace();
            if (this.unitOfWork.getConnection() != null) {
                this.unitOfWork.rollback(); // Rollback transaction if error occurs
            }
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }

    public String getUsername(int userId) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                select username from users where id = ?
                """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();
            String username = null;
            if (resultSet.next()) {
                username = resultSet.getString("username");
            }
            return username;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }

    public void changeEloByUserId(int user_id, int amountToChangeElo) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        update users set elo = elo + ? where id = ?
                        """))
        {
            preparedStatement.setInt(1, amountToChangeElo);
            preparedStatement.setInt(2, user_id);
            preparedStatement.executeUpdate();
            this.unitOfWork.commitTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
            if (this.unitOfWork.getConnection() != null) {
                this.unitOfWork.rollback(); // Rollback transaction if error occurs
            }
            throw new DataAccessException("Update nicht erfolgreich", e);
        }
    }
}
