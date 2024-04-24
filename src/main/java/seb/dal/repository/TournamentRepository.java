package seb.dal.repository;

import seb.dal.DataAccessException;
import seb.dal.UnitOfWork;
import seb.model.Status;
import seb.model.Tournament;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
                        Status.valueOf(resultSet.getString("status").toUpperCase())
                );
                System.out.println(tournament.getStatusString());
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

    public List<Tournament> getTournamentsByUsername(String username) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                        select t.*
                        from users u
                        join tournament_participants tp on u.id = tp.user_id
                        join tournaments t on tp.tournament_id = t.id
                        where u.username = ?
                        order by case
                        when status = 'active' then 1
                        when status = 'completed' then 2
                        when status = 'pending' then 3
                        when status = 'cancelled' then 4
                        else 5
                        end"""))
        {

            preparedStatement.setString(1, username);


            ResultSet resultSet = preparedStatement.executeQuery();
            List<Tournament> tournaments = new ArrayList<>();
            if (!resultSet.next()) {
                return null;
            }
            while (resultSet.next()) {
                tournaments.add(new Tournament(
                        resultSet.getInt("id"),
                        resultSet.getTimestamp("start_time"),
                        Status.valueOf(resultSet.getString("status").toUpperCase())
                ));

            }
            return tournaments;
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
                        Status.valueOf(resultSet.getString("status").toUpperCase())
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
                             insert into tournaments default values
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
            LocalDateTime startTime = LocalDateTime.now();

            preparedStatement.setTimestamp(1, Timestamp.valueOf(startTime));
            preparedStatement.setString(2, Status.ACTIVE.getStatus());
            preparedStatement.setInt(3, tournament_id);
            preparedStatement.executeUpdate();
            unitOfWork.commitTransaction();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("update nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public void endTournament(int tournament_id) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        update tournaments set status = cast('completed' as tournament_status) where id = ?
                        """))
        {
            preparedStatement.setInt(1, tournament_id);
            preparedStatement.executeUpdate();
            this.unitOfWork.commitTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("update nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public LocalDateTime getStartTimeById(int id) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        select start_time from tournaments where id = ?
                        """))
        {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Timestamp startTime = resultSet.getTimestamp("start_time");
                return startTime.toLocalDateTime();
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }
}
