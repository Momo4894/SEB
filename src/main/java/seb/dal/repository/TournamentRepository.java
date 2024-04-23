package seb.dal.repository;

import seb.dal.DataAccessException;
import seb.dal.UnitOfWork;
import seb.model.Status;
import seb.model.Tournament;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TournamentRepository {
    private UnitOfWork unitOfWork;

    public TournamentRepository (UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    public Tournament getTournamentByStatusAndUsername(String username, Status status) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        select t.* 
                        from users u 
                        join tournament_participants tp on u.id = tp.user_id 
                        join tournaments t on tp.tournament_id = t.id 
                        where u.username = ? and t.status = cast(? AS tournament_status)"""))
        {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, status.getStatus());


            ResultSet resultSet = preparedStatement.executeQuery();


            if (resultSet.next()) {
                Tournament tournament = new Tournament(
                        resultSet.getInt("id"),
                        resultSet.getTimestamp("start_time"),
                        status
                );
                return tournament;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            if (this.unitOfWork.getConnection() != null) {
                this.unitOfWork.rollback(); // Rollback transaction if error occurs
            }
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public int getIdByStatus(Status status) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        select id from tournaments where status = cast(? as tournament_status)"""))
        {
            preparedStatement.setString(1, status.getStatus());



            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public Tournament getTournamentByStatus(Status status) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                        select * from tournaments where status = cast(? as tournament_status)"""))
        {
            preparedStatement.setString(1, status.getStatus());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new Tournament(
                        resultSet.getInt("id"),
                        resultSet.getTimestamp("start_time"),
                        status
                );
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public void addTournament() {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                             insert into tournaments
                             """))
        {
            preparedStatement.executeUpdate();
            unitOfWork.commitTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public void startTournament(int tournament_id) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        update tournaments set start_time = ?, status = cast(? as tournament_status) where id = ?
                        """))
        {
            long now = System.currentTimeMillis();
            preparedStatement.setTimestamp(1, new Timestamp(now));
            preparedStatement.setString(2, Status.ACTIVE.getStatus());
            preparedStatement.executeUpdate();
            unitOfWork.commitTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("update nicht erfolgreich" + e.getMessage(), e);
        }
    }
}
